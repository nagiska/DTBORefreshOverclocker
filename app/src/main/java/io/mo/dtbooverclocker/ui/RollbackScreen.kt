package io.mo.dtbooverclocker.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.mo.dtbooverclocker.model.BackupRecord
import io.mo.dtbooverclocker.model.BackupType
import io.mo.dtbooverclocker.model.BackupVerificationState
import io.mo.dtbooverclocker.model.BackupVerificationStatus
import io.mo.dtbooverclocker.util.StorageUtils
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun RollbackScreen(
    state: MainUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onManualBackup: (description: String) -> Unit,
    onVerifyMd5: (record: BackupRecord) -> Unit,
    onExportBackup: (record: BackupRecord) -> Unit,
    onFlashBackup: (record: BackupRecord) -> Unit,
    onDeleteBackup: (record: BackupRecord) -> Unit
) {
    BackHandler(onBack = onNavigateBack)
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var showManualBackupDialog by remember { mutableStateOf(false) }
    var pendingFlashRecord by remember { mutableStateOf<BackupRecord?>(null) }
    var pendingDeleteRecord by remember { mutableStateOf<BackupRecord?>(null) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "镜像回滚",
                subtitle = "DTBO 分区备份时间轴与还原",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回主页"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showManualBackupDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "手动备份当前分区")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新备份列表")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.backups.isEmpty()) {
            EmptyRollbackState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                onManualBackupClick = { showManualBackupDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    RollbackHeaderCard(
                        totalCount = state.backups.size,
                        slotLabel = state.slotInfo?.label ?: "未知槽位",
                        blockDevice = state.slotInfo?.blockDevice ?: "未知分区",
                        onManualBackupClick = { showManualBackupDialog = true }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                itemsIndexed(state.backups, key = { _, item -> item.id }) { index, record ->
                    val isLast = index == state.backups.lastIndex
                    val verificationState = state.backupVerificationStates[record.id]
                        ?: BackupVerificationState()

                    TimelineBackupItem(
                        record = record,
                        isLast = isLast,
                        verificationState = verificationState,
                        onVerifyMd5 = { onVerifyMd5(record) },
                        onCopyMd5 = {
                            clipboard.setText(AnnotatedString(record.recordedMd5))
                            Toast.makeText(context, "MD5 已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        },
                        onExport = { onExportBackup(record) },
                        onFlash = { pendingFlashRecord = record },
                        onDelete = { pendingDeleteRecord = record }
                    )
                }
            }
        }
    }

    // Manual Backup Dialog
    if (showManualBackupDialog) {
        var manualDesc by remember { mutableStateOf("") }
        WindowDialog(
            show = true,
            title = "手动备份当前 DTBO 镜像",
            onDismissRequest = { showManualBackupDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "将通过 Root 读取当前活跃分区 (${state.slotInfo?.blockDevice ?: "未检测到槽位"}) 并保存为回滚镜像。",
                    style = MiuixTheme.textStyles.body2
                )
                TextField(
                    value = manualDesc,
                    onValueChange = { manualDesc = it },
                    label = "备份说明备注（可选）",
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        showManualBackupDialog = false
                        onManualBackup(manualDesc)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    Text("立即备份")
                }
                Button(
                    onClick = { showManualBackupDialog = false },
                    colors = ButtonDefaults.buttonColors(color = Color.Transparent, contentColor = MiuixTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }

    // Dangerous Flash Rollback Confirmation Dialog
    pendingFlashRecord?.let { record ->
        WindowDialog(
            show = true,
            title = "确认回滚刷入 DTBO 镜像？",
            onDismissRequest = { pendingFlashRecord = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "高风险警示",
                        tint = MiuixTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "此操作将覆盖当前的 DTBO 分区",
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.error
                    )
                }
                Text(
                    "您即将把选定的备份镜像物理写入设备分区，此操作将覆盖当前的 DTBO 分区！",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("• 目标分区：${state.slotInfo?.blockDevice ?: record.blockDevice}", style = MiuixTheme.textStyles.footnote1)
                        Text("• 备份文件：${record.fileName}", style = MiuixTheme.textStyles.footnote1)
                        Text("• 备份时间：${record.formattedTime}", style = MiuixTheme.textStyles.footnote1)
                        Text("• 备份系统：${record.androidVersion}", style = MiuixTheme.textStyles.footnote1)
                        Text("• 系统版本：${record.buildDisplay}", style = MiuixTheme.textStyles.footnote1)
                        Text("• 记录 MD5：${record.recordedMd5}", style = MiuixTheme.textStyles.footnote1, fontFamily = FontFamily.Monospace)
                    }
                }
                Text(
                    "写入后系统将自动进行写后回读 MD5 校验以确保完整性。请确保电量充足，刷写过程中请勿断电或重启手机。",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
                Button(
                    onClick = {
                        val target = record
                        pendingFlashRecord = null
                        onFlashBackup(target)
                    },
                    colors = ButtonDefaults.buttonColorsPrimary(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确认回滚刷入")
                }
                Button(
                    onClick = { pendingFlashRecord = null },
                    colors = ButtonDefaults.buttonColors(color = Color.Transparent, contentColor = MiuixTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }

    // Delete Confirmation Dialog
    pendingDeleteRecord?.let { record ->
        WindowDialog(
            show = true,
            title = "删除此备份？",
            onDismissRequest = { pendingDeleteRecord = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "删除确认",
                        tint = MiuixTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("本地文件与元数据将永久移除", style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
                }
                Text("确定要删除镜像 ${record.fileName} 吗？删除后本地文件与元数据将永久移除，无法再用于一键回滚。")
                Button(
                    onClick = {
                        val target = record
                        pendingDeleteRecord = null
                        onDeleteBackup(target)
                    },
                    colors = ButtonDefaults.buttonColorsPrimary(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确认删除")
                }
                Button(
                    onClick = { pendingDeleteRecord = null },
                    colors = ButtonDefaults.buttonColors(color = Color.Transparent, contentColor = MiuixTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }
}

@Composable
private fun RollbackHeaderCard(
    totalCount: Int,
    slotLabel: String,
    blockDevice: String,
    onManualBackupClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("备份镜像库", style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.Bold)
                    Text(
                        "当前槽位: $slotLabel ($blockDevice)",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MiuixTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "共 $totalCount 个备份",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MiuixTheme.textStyles.footnote2,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onManualBackupClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("手动备份当前手机 DTBO 镜像")
            }
        }
    }
}

@Composable
private fun TimelineBackupItem(
    record: BackupRecord,
    isLast: Boolean,
    verificationState: BackupVerificationState,
    onVerifyMd5: () -> Unit,
    onCopyMd5: () -> Unit,
    onExport: () -> Unit,
    onFlash: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left timeline track (Icon node + Connecting line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight()
        ) {
            // Milestone node
            val isAuto = record.backupType == BackupType.AUTO
            val nodeColor = if (isAuto) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.secondary
            val nodeIcon = if (isAuto) Icons.Default.AutoAwesome else Icons.Default.TouchApp

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(nodeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    nodeIcon,
                    contentDescription = record.backupType.displayName,
                    tint = MiuixTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Connecting line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MiuixTheme.colorScheme.outline)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Right content card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 12.dp else 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header: Tag + Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (record.backupType == BackupType.AUTO) {
                                MiuixTheme.colorScheme.primaryContainer
                            } else {
                                MiuixTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            Text(
                                text = record.backupType.displayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MiuixTheme.textStyles.footnote2,
                                fontWeight = FontWeight.Bold,
                                color = if (record.backupType == BackupType.AUTO) {
                                    MiuixTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MiuixTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }

                        Text(
                            text = record.formattedTime,
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }

                    // System info
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = record.fileName,
                            style = MiuixTheme.textStyles.body2,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (record.description.isNotBlank()) {
                            Text(
                                text = record.description,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary
                            )
                        }
                    }

                    // Metadata details box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            InfoRow(label = "系统版本", value = record.androidVersion)
                            InfoRow(label = "系统固件", value = record.buildDisplay)
                            InfoRow(label = "设备机型", value = record.deviceModel)
                            InfoRow(label = "备份槽位", value = "${record.slot} (${record.blockDevice})")
                            InfoRow(label = "文件大小", value = StorageUtils.formatFileSize(record.fileSizeBytes))
                        }
                    }

                    // MD5 Verification Section
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "MD5: ",
                                        style = MiuixTheme.textStyles.footnote2,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = record.recordedMd5,
                                        style = MiuixTheme.textStyles.footnote2,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                IconButton(
                                    onClick = onCopyMd5,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "复制 MD5",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // MD5 Status Badge & Verification Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (verificationState.status) {
                                    BackupVerificationStatus.UNCHECKED -> {
                                        Text(
                                            text = "未校验完整性",
                                            style = MiuixTheme.textStyles.footnote2,
                                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                                        )
                                    }
                                    BackupVerificationStatus.VERIFYING -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                            Text(
                                                text = "正在校验 MD5…",
                                                style = MiuixTheme.textStyles.footnote2,
                                                color = MiuixTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    BackupVerificationStatus.MATCHED -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "MD5 校验通过 (一致)",
                                                style = MiuixTheme.textStyles.footnote2,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                    BackupVerificationStatus.MISMATCH -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Error,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = verificationState.message ?: "MD5 不一致",
                                                style = MiuixTheme.textStyles.footnote2,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MiuixTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    BackupVerificationStatus.FILE_MISSING -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "备份镜像文件已丢失",
                                                style = MiuixTheme.textStyles.footnote2,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MiuixTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = onVerifyMd5,
                                    enabled = verificationState.status != BackupVerificationStatus.VERIFYING,
                                    insideMargin = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    minHeight = 30.dp,
                                    colors = ButtonDefaults.buttonColors()
                                ) {
                                    Text("验证 MD5", style = MiuixTheme.textStyles.footnote2)
                                }
                            }
                        }
                    }

                    // 3 Actions: Export, Flash, Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onExport,
                            modifier = Modifier.weight(1f),
                            insideMargin = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("导出", style = MiuixTheme.textStyles.footnote2)
                        }

                        Button(
                            onClick = onFlash,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColorsPrimary(
                                color = MiuixTheme.colorScheme.error,
                                contentColor = MiuixTheme.colorScheme.onError
                            ),
                            insideMargin = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("刷入", style = MiuixTheme.textStyles.footnote2, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                color = Color.Transparent,
                                contentColor = MiuixTheme.colorScheme.error
                            ),
                            insideMargin = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("删除", style = MiuixTheme.textStyles.footnote2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceSecondary
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.footnote2,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyRollbackState(
    modifier: Modifier = Modifier,
    onManualBackupClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.HistoryEdu,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "暂无备份镜像",
            style = MiuixTheme.textStyles.title2,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "在直接刷入超频镜像前，系统会自动备份当前活跃槽位分区；您也可以随时手动备份当前手机 DTBO 分区以便日后回滚。",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onManualBackupClick,
            colors = ButtonDefaults.buttonColorsPrimary()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("立即手动备份当前镜像")
        }
    }
}
