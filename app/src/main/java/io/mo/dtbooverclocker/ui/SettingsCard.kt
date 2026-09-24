package io.mo.dtbooverclocker.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * 设置页面统一卡片：Miuix Card + 与 squircle 圆角匹配的细边框
 * （对齐 Miuix Surface 文档的描边风格），可点击时带 Sink 按压反馈。
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .squircleBorder(
                width = 1.dp,
                color = MiuixTheme.colorScheme.outline.copy(alpha = 0.35f),
                cornerRadius = 16.dp
            ),
        cornerRadius = 16.dp,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface),
        pressFeedbackType = if (onClick != null) PressFeedbackType.Sink else PressFeedbackType.None,
        onClick = onClick,
        content = content
    )
}
