package io.mo.dtbooverclocker.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.mo.dtbooverclocker.BuildConfig
import io.mo.dtbooverclocker.ui.components.MiuixInfoChip
import io.mo.dtbooverclocker.util.StorageUtils
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: MainUiState,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToRollback: () -> Unit = {},
    onRequestRoot: () -> Unit,
    onRefreshEnvironment: () -> Unit,
    onRefreshCacheSize: () -> Unit,
    onClearAllCache: (onCleared: (Long) -> Unit) -> Unit,
    onRefreshLogStats: () -> Unit,
    onExportLogs: () -> Unit,
    onClearAllLogs: (onCleared: () -> Unit) -> Unit
) {
    BackHandler(onBack = onNavigateBack)
    val context = LocalContext.current
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearLogsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRefreshCacheSize()
        onRefreshLogStats()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "设置",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回主页"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshEnvironment) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新环境探测")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // 1. Environment Status Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary
                            )
                            Text(
                                "环境状态",
                                style = MiuixTheme.textStyles.title2,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MiuixInfoChip(
                                text = if (state.rootState.granted) "Root 已授权 (自动保持)" else state.rootState.detail,
                                leadingIcon = Icons.Default.Lock
                            )
                            state.slotInfo?.let { slot ->
                                MiuixInfoChip(text = slot.label)
                            }
                        }

                        Text(
                            text = state.slotInfo?.blockDevice ?: "分区路径：检测中",
                            style = MiuixTheme.textStyles.footnote1,
                            fontFamily = FontFamily.Monospace,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!state.rootState.granted) {
                                Button(
                                    onClick = onRequestRoot,
                                    enabled = state.rootState.suPresent,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors()
                                ) {
                                    Text("请求 Root 授权")
                                }
                            }

                            Button(
                                onClick = onRefreshEnvironment,
                                modifier = if (!state.rootState.granted) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("重新探测环境")
                            }
                        }
                    }
                }
            }

            // 1.5 Rollback & Backup Management Entry
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToRollback),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MiuixTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "镜像备份与回滚管理",
                                    style = MiuixTheme.textStyles.title2,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "已存储 ${state.backups.size} 个备份 · 查看时间轴与一键还原",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                            }
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "查看备份",
                            tint = MiuixTheme.colorScheme.onSurfaceSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2. Cache Management Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.primary
                                )
                                Text(
                                    "应用缓存",
                                    style = MiuixTheme.textStyles.title2,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = StorageUtils.formatFileSize(state.cacheSizeBytes),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MiuixTheme.textStyles.footnote1,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Text(
                            text = "包含导入的 DTBO 镜像缓存、反编译 DTS 临时工作区及刷写校验临时文件。",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showClearCacheDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    color = Color.Transparent,
                                    contentColor = MiuixTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("清除所有缓存")
                            }
                        }
                    }
                }
            }

            // 3. Log Management Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Article,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.primary
                                )
                                Text(
                                    "运行日志",
                                    style = MiuixTheme.textStyles.title2,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${state.logFilesCount} 个文件 · ${StorageUtils.formatFileSize(state.logFilesSizeBytes)}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MiuixTheme.textStyles.footnote1,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onExportLogs,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColorsPrimary()
                            ) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("导出完整日志")
                            }

                            Button(
                                onClick = { showClearLogsDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    color = Color.Transparent,
                                    contentColor = MiuixTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("清空日志")
                            }
                        }
                    }
                }
            }

            // 4. About Section Entry
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToAbout),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MiuixTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "关于应用",
                                    style = MiuixTheme.textStyles.title2,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "DTBO Refresh Overclocker v${BuildConfig.VERSION_NAME}",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                            }
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "查看详情",
                            tint = MiuixTheme.colorScheme.onSurfaceSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Clear Cache Confirmation Dialog
    if (showClearCacheDialog) {
        WindowDialog(
            show = true,
            title = "确认清空应用缓存？",
            onDismissRequest = { showClearCacheDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "将清除当前应用内的所有导入镜像缓存与反编译工作目录（当前占用：${StorageUtils.formatFileSize(state.cacheSizeBytes)}）。\n\n" +
                            "若当前有正在编辑但尚未导出的 DTBO 工作区，清理后工作区将被重置。"
                )
                Button(
                    onClick = {
                        showClearCacheDialog = false
                        onClearAllCache { freedBytes ->
                            val message = if (freedBytes > 0) {
                                "已成功清空缓存，释放 ${StorageUtils.formatFileSize(freedBytes)}"
                            } else {
                                "缓存已清空"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColorsPrimary(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清空")
                }
                Button(
                    onClick = { showClearCacheDialog = false },
                    colors = ButtonDefaults.buttonColors(color = Color.Transparent, contentColor = MiuixTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }

    // Clear Logs Confirmation Dialog
    if (showClearLogsDialog) {
        WindowDialog(
            show = true,
            title = "确认清空所有运行日志？",
            onDismissRequest = { showClearLogsDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "将删除设备中保存的历史会话日志（当前：${state.logFilesCount} 个文件，共 ${StorageUtils.formatFileSize(state.logFilesSizeBytes)}）。\n\n" +
                            "清空后将自动开启新的空白会话。"
                )
                Button(
                    onClick = {
                        showClearLogsDialog = false
                        onClearAllLogs {
                            Toast.makeText(context, "日志文件已清空", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColorsPrimary(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清空")
                }
                Button(
                    onClick = { showClearLogsDialog = false },
                    colors = ButtonDefaults.buttonColors(color = Color.Transparent, contentColor = MiuixTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }
}
