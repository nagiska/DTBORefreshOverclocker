package io.mo.dtbooverclocker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 设置页面统一卡片：Miuix Surface + 原生 border 参数描边
 * （对齐 Miuix Surface 文档用法；border 作为 surface 一部分绘制，
 * 不会被不透明背景盖住——squircleBorder 是 onDrawBehind 会被 Card 背景遮住）。
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val border = BorderStroke(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.5f))
    val body: @Composable () -> Unit = { Column { content() } }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MiuixTheme.colorScheme.surfaceContainer,
            border = border,
            content = body
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MiuixTheme.colorScheme.surfaceContainer,
            border = border,
            content = body
        )
    }
}
