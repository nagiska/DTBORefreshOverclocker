package io.mo.dtbooverclocker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import io.mo.dtbooverclocker.core.ActivePanelDetector
import io.mo.dtbooverclocker.model.TimingCandidate
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class PanelFilterScope(val label: String) {
    DEVICE_ONLY("机型专属"),
    ALL("全部面板"),
    REFERENCE("公版/仿真")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimingCandidateSelector(
    candidates: List<TimingCandidate>,
    selectedCandidateId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    activePanelIdentifier: String? = null,
    activePanelDisplayName: String? = null,
    activePanelSource: String? = null
) {
    val groups = remember(candidates) {
        TimingUtils.groupCandidates(candidates)
    }

    if (groups.isEmpty()) {
        Text("未识别到可调整的 DSI 时序候选", style = MiuixTheme.textStyles.body2)
        return
    }

    val deviceSpecificCount = remember(groups) {
        groups.keys.count { it.isDeviceSpecific }
    }
    val hasDeviceSpecific = deviceSpecificCount > 0

    var filterScope by remember(hasDeviceSpecific) {
        mutableStateOf(if (hasDeviceSpecific) PanelFilterScope.DEVICE_ONLY else PanelFilterScope.ALL)
    }
    var searchQuery by remember { mutableStateOf("") }

    val filteredGroups = remember(groups, filterScope, searchQuery, activePanelIdentifier) {
        val baseFiltered = groups.filter { (key, list) ->
            val scopeMatch = when (filterScope) {
                PanelFilterScope.DEVICE_ONLY -> key.isDeviceSpecific
                PanelFilterScope.REFERENCE -> !key.isDeviceSpecific
                PanelFilterScope.ALL -> true
            }
            val queryMatch = searchQuery.isBlank() ||
                key.panelDisplayName.contains(searchQuery, ignoreCase = true) ||
                key.panelIdentifier.contains(searchQuery, ignoreCase = true) ||
                list.any { cand ->
                    cand.currentHz.toString().contains(searchQuery) ||
                        cand.nodePath.contains(searchQuery, ignoreCase = true)
                }
            scopeMatch && queryMatch
        }

        if (activePanelIdentifier != null) {
            baseFiltered.toList().sortedByDescending { (key, _) ->
                ActivePanelDetector.matchPanel(key.panelIdentifier, activePanelIdentifier)
            }.toMap()
        } else {
            baseFiltered
        }
    }

    // 默认选中的分组（若已有选中的 candidate，则对应其所在分组；否则选在用面板或首个分组）
    val initialKey = remember(candidates, selectedCandidateId, filteredGroups, activePanelIdentifier) {
        val found = candidates.firstOrNull { it.id == selectedCandidateId }
        if (found != null) {
            filteredGroups.keys.firstOrNull { it.entryIndex == found.entryIndex && it.panelIdentifier == TimingUtils.parsePanelIdentifier(found.nodePath) }
                ?: groups.keys.firstOrNull { it.entryIndex == found.entryIndex && it.panelIdentifier == TimingUtils.parsePanelIdentifier(found.nodePath) }
                ?: filteredGroups.keys.firstOrNull()
                ?: groups.keys.first()
        } else if (activePanelIdentifier != null) {
            filteredGroups.keys.firstOrNull { ActivePanelDetector.matchPanel(it.panelIdentifier, activePanelIdentifier) }
                ?: filteredGroups.keys.firstOrNull()
                ?: groups.keys.first()
        } else {
            filteredGroups.keys.firstOrNull() ?: groups.keys.first()
        }
    }

    var activeGroupKey by remember(groups.keys) {
        mutableStateOf(initialKey)
    }

    // 确保 activeGroupKey 始终有效
    if (activeGroupKey !in groups.keys) {
        activeGroupKey = filteredGroups.keys.firstOrNull() ?: groups.keys.first()
    }

    // 如果当前选中的候选不在 activeGroupKey 列表中，允许自动联动
    val currentGroupCandidates = groups[activeGroupKey] ?: candidates
    val activeCandidate = candidates.firstOrNull { it.id == selectedCandidateId }
        ?: currentGroupCandidates.firstOrNull()

    var showRawDetails by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 推荐在用屏幕提示条
        if (activePanelDisplayName != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MiuixTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            "本机正在使用的屏幕：$activePanelDisplayName",
                            style = MiuixTheme.textStyles.subtitle,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.primary
                        )
                        if (activePanelSource != null) {
                            Text(
                                "检测来源：$activePanelSource · ",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary
                            )
                        }
                    }
                }
            }
        }
        // 面板选择区（若存在多个屏幕/DTB 分组时展示切换与过滤）
        if (groups.size > 1) {
            // 过滤维度切换（如果有专属面板，默认仅显示机型专属）
            if (hasDeviceSpecific) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiuixSelectableChip(
                        text = "机型专属 ($deviceSpecificCount)",
                        selected = filterScope == PanelFilterScope.DEVICE_ONLY,
                        onClick = {
                            filterScope = PanelFilterScope.DEVICE_ONLY
                            filteredGroups.keys.firstOrNull()?.let { k ->
                                activeGroupKey = k
                                groups[k]?.firstOrNull()?.let { onSelect(it.id) }
                            }
                        },
                        leadingIcon = Icons.Default.Star
                    )

                    MiuixSelectableChip(
                        text = "全部 (${groups.size})",
                        selected = filterScope == PanelFilterScope.ALL,
                        onClick = { filterScope = PanelFilterScope.ALL }
                    )

                    MiuixSelectableChip(
                        text = "公版/仿真 (${groups.size - deviceSpecificCount})",
                        selected = filterScope == PanelFilterScope.REFERENCE,
                        onClick = {
                            filterScope = PanelFilterScope.REFERENCE
                            filteredGroups.keys.firstOrNull()?.let { k ->
                                activeGroupKey = k
                                groups[k]?.firstOrNull()?.let { onSelect(it.id) }
                            }
                        }
                    )
                }
            }

            // 搜索框（支持搜索 o1, 38, 42, 144 等）
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "搜索屏幕或时序",
                useLabelAsPlaceholder = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "清空")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 过滤后的面板切换芯片（LazyRow 只合成可见芯片，避免 38 个面板一次性全部组合）
            if (filteredGroups.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredGroups.entries.toList(),
                        key = { entry -> "${entry.key.entryIndex}_${entry.key.panelIdentifier}" }
                    ) { entry ->
                        val key = entry.key
                        val groupCandidates = entry.value
                        val isGroupActive = key == activeGroupKey
                        val isDetectedActive = activePanelIdentifier != null &&
                            ActivePanelDetector.matchPanel(key.panelIdentifier, activePanelIdentifier)
                        MiuixChip(
                            selected = isGroupActive,
                            onClick = {
                                activeGroupKey = key
                                // 切换面板时自动将选中项设为该面板首个候选
                                groupCandidates.firstOrNull()?.let { onSelect(it.id) }
                            },
                            selectedContainerColor = if (isDetectedActive)
                                MiuixTheme.colorScheme.primaryContainer
                            else MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isDetectedActive) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "本机在用",
                                        modifier = Modifier.size(15.dp),
                                        tint = Color(0xFF2E7D32)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                } else if (key.isDeviceSpecific) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "机型专属",
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isGroupActive) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceSecondary
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                val prefix = if (isDetectedActive) "在用·" else ""
                                Text("${key.panelDisplayName} ($prefix${groupCandidates.size}档)")
                            }
                        }
                    }
                }
            } else {
                Text(
                    "未搜索到匹配的面板或时序",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else {
            // 单面板时展示面板名称卡片
            val singleKey = groups.keys.first()
            val sample = currentGroupCandidates.firstOrNull()
            Surface(
                color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            singleKey.panelDisplayName,
                            style = MiuixTheme.textStyles.title3,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "DTB[${singleKey.entryIndex}]",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary
                            )
                            if (sample?.hActive != null && sample.vActive != null) {
                                Text("·", style = MiuixTheme.textStyles.footnote2)
                                Text(
                                    "${sample.hActive} × ${sample.vActive} 像素",
                                    style = MiuixTheme.textStyles.footnote2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                            }
                            Text("·", style = MiuixTheme.textStyles.footnote2)
                            Text(
                                "${currentGroupCandidates.size} 个时序档位",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 刷新率档位卡片列表
        Text(
            "选择待超频的原始时序档位：",
            style = MiuixTheme.textStyles.footnote1,
            fontWeight = FontWeight.Medium
        )

        // 刷新率档位卡片列表（分页渲染：首屏只组合有限数量，避免上百个候选卡顿）
        var visibleCount by remember(activeGroupKey) { mutableIntStateOf(12) }
        val visibleCandidates = remember(currentGroupCandidates, visibleCount) {
            currentGroupCandidates.take(visibleCount)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            visibleCandidates.forEach { candidate ->
                val isSelected = candidate.id == (activeCandidate?.id ?: selectedCandidateId)
                TimingCandidateCard(
                    candidate = candidate,
                    selected = isSelected,
                    onClick = { onSelect(candidate.id) }
                )
            }

            val remaining = currentGroupCandidates.size - visibleCandidates.size
            if (remaining > 0) {
                Button(
                    onClick = { visibleCount += 24 },
                    colors = ButtonDefaults.buttonColors(),
                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    minHeight = 34.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("显示更多（剩余 $remaining 个档位）", style = MiuixTheme.textStyles.footnote2)
                }
            }
        }

        // 选定时序的技术详情折叠区（解决 raw 路径过长遮蔽视线的问题）
        activeCandidate?.let { cand ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 10.dp,
                colors = CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRawDetails = !showRawDetails },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SettingsEthernet,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MiuixTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "底层设备树 (DTS) 节点详情",
                                style = MiuixTheme.textStyles.footnote1,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            if (showRawDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.outline
                        )
                    }

                    AnimatedVisibility(
                        visible = showRawDetails,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "节点路径：",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary
                            )
                            Surface(
                                color = MiuixTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, MiuixTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        cand.nodePath,
                                        modifier = Modifier.weight(1f),
                                        fontFamily = FontFamily.Monospace,
                                        style = MiuixTheme.textStyles.footnote2
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(cand.nodePath))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "复制路径",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "DTS 范围: [${cand.nodeStart}..${cand.nodeEndExclusive}]",
                                    fontFamily = FontFamily.Monospace,
                                    style = MiuixTheme.textStyles.footnote2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                                Text(
                                    "来源文件: ${cand.dtsFile.name}",
                                    fontFamily = FontFamily.Monospace,
                                    style = MiuixTheme.textStyles.footnote2,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimingCandidateCard(
    candidate: TimingCandidate,
    selected: Boolean,
    onClick: () -> Unit
) {
    val nodeName = TimingUtils.parseTimingNodeName(candidate.nodePath)
    val clockStr = TimingUtils.formatClockCompact(candidate.pixelClockHz)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MiuixTheme.colorScheme.primaryContainer
        } else {
            MiuixTheme.colorScheme.surfaceContainer
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MiuixTheme.colorScheme.primary
            } else {
                MiuixTheme.colorScheme.outline.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选中指示
            Icon(
                if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(12.dp))

            // 主标题：刷新率
            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "${candidate.currentHz} Hz",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = MiuixTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            nodeName,
                            style = MiuixTheme.textStyles.footnote2,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    if (candidate.hasVendorDynamicMode) {
                        Surface(
                            color = MiuixTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "自动变频 / idle",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (candidate.hasOpaquePanelTimings) {
                        Surface(
                            color = MiuixTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "PHY Blob",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (candidate.hasVendorDynamicMode) {
                    Text(
                        "不建议修改或作为新增模板，请选择同面板的 normal 普通档位。",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(3.dp))

                // 副信息：时钟与分辨率
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "Clock: $clockStr",
                        style = MiuixTheme.textStyles.footnote2,
                        fontFamily = FontFamily.Monospace,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )

                    if (candidate.hActive != null && candidate.vActive != null) {
                        Text(
                            "${candidate.hActive}×${candidate.vActive}",
                            style = MiuixTheme.textStyles.footnote2,
                            fontFamily = FontFamily.Monospace,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }

                    Text(
                        if (candidate.hasFullGeometry) "时序完整" else "时序缺省",
                        style = MiuixTheme.textStyles.footnote2,
                        color = if (candidate.hasFullGeometry) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            MiuixTheme.colorScheme.outline
                        }
                    )
                }
            }
        }
    }
}
