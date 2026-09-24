package io.mo.dtbooverclocker.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.mo.dtbooverclocker.ui.scaleLineHeight
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun DisclaimerDialog(
    isFirstLaunch: Boolean = true,
    onConfirm: () -> Unit,
    onExit: () -> Unit,
    onDismiss: () -> Unit = onExit
) {
    var seconds by remember { mutableIntStateOf(if (isFirstLaunch) 5 else 0) }
    var isChecked by remember { mutableStateOf(false) }

    if (isFirstLaunch) {
        BackHandler(onBack = onExit)
        LaunchedEffect(Unit) {
            while (seconds > 0) {
                delay(1000)
                seconds--
            }
        }
    }

    WindowDialog(
        show = true,
        title = "风险提示与使用须知",
        summary = "Risk & Disclaimer",
        onDismissRequest = {
            if (!isFirstLaunch) {
                onDismiss()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 欢迎语
            Text(
                text = "欢迎使用 DTBO Refresh Overclocker。在继续使用并授予 Root 权限前，请务必仔细阅读以下内容：",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
                lineHeight = MiuixTheme.textStyles.body2.lineHeight.scaleLineHeight(1.2f)
            )

            // 1. 高危操作声明
            DisclaimerSection(
                icon = Icons.Default.Dangerous,
                title = "1. 高危操作声明",
                color = MiuixTheme.colorScheme.error,
                containerColor = MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                content = "本工具属于 Android 底层硬件调试与调校工具。使用本软件将会请求 Root 超级用户权限，并直接对设备的底层物理分区（dtbo）执行解包、修改并重写内核设备树（Device Tree Blob）操作。"
            )

            // 2. 潜在严重风险
            DisclaimerSection(
                icon = Icons.Default.Warning,
                title = "2. 潜在严重风险",
                color = MiuixTheme.colorScheme.error,
                containerColor = MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                content = "屏幕刷新率超频受限于您的屏幕面板品质与显示驱动 IC（DDIC）硬件体质。任何不当的时序或频率参数可能导致：\n\n" +
                        "• 屏幕黑屏 / 花屏：开机后屏幕无法点亮或严重偏色、残影；\n" +
                        "• 系统无法启动（Bootloop）：内核加载异常导致卡开机 LOGO 或反复重启；\n" +
                        "• 硬件潜在损耗：长期超出标称频率运行可能导致发热加剧、器件加速老化或不可逆的物理损坏。"
            )

            // 3. 使用前提条件
            DisclaimerSection(
                icon = Icons.Default.Shield,
                title = "3. 使用前提条件",
                color = MiuixTheme.colorScheme.primary,
                containerColor = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                content = "若要使用本软件，您必须拥有救砖的能力。"
            )

            // 4. 免责条款
            DisclaimerSection(
                icon = Icons.Default.Info,
                title = "4. 免责条款",
                color = MiuixTheme.colorScheme.secondary,
                containerColor = MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                content = "本软件仅供设备所有者用于个人学习、显示技术研究与性能测试。开发者已尽可能提供单槽保护与校验机制，但无法担保本软件在所有设备、内核及系统版本下的兼容性与安全性。因使用本软件导致的任何设备损坏、数据丢失、保修失效或硬件故障，均由使用者自行承担全部责任。"
            )

            if (isFirstLaunch) {
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()

                // 勾选确认行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isChecked = !isChecked }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        state = if (isChecked) ToggleableState.On else ToggleableState.Off,
                        onClick = { isChecked = !isChecked }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "我已完整阅读并充分理解上述风险，确认具备独立救砖能力并自愿承担全部后果。",
                        style = MiuixTheme.textStyles.footnote1,
                        fontWeight = FontWeight.Medium,
                        color = if (isChecked) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 操作按钮
        if (isFirstLaunch) {
            val isButtonEnabled = isChecked && seconds == 0
            Button(
                onClick = onConfirm,
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColorsPrimary(
                    color = MiuixTheme.colorScheme.error,
                    contentColor = MiuixTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (seconds > 0) "同意并继续 (${seconds}s)" else "同意并继续"
                )
            }
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("退出应用")
            }
        } else {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColorsPrimary(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("我知道了")
            }
        }
    }
}

@Composable
private fun DisclaimerSection(
    icon: ImageVector,
    title: String,
    content: String,
    color: Color,
    containerColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = containerColor),
        cornerRadius = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = content,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurface,
                lineHeight = MiuixTheme.textStyles.footnote1.lineHeight.scaleLineHeight(1.25f)
            )
        }
    }
}
