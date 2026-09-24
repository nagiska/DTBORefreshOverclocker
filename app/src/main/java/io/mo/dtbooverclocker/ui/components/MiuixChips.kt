package io.mo.dtbooverclocker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Miuix 暂不提供 Chip 组件，这里基于 [Surface] 实现一个等价的小芯片，
 * 用于替代 Material3 的 FilterChip / AssistChip。
 */
@Composable
fun MiuixChip(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MiuixTheme.colorScheme.surfaceVariant,
    contentColor: Color = MiuixTheme.colorScheme.onSurface,
    selectedContainerColor: Color = MiuixTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MiuixTheme.colorScheme.onPrimaryContainer,
    leadingIcon: ImageVector? = null,
    label: @Composable RowScope.() -> Unit
) {
    val bg = if (selected) selectedContainerColor else containerColor
    val fg = if (selected) selectedContentColor else contentColor
    val shape = RoundedCornerShape(8.dp)
    val border = if (selected) null else BorderStroke(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.35f))

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.size(4.dp))
            }
            label()
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            color = bg,
            contentColor = fg,
            border = border
        ) { content() }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = bg,
            contentColor = fg,
            border = border
        ) { content() }
    }
}

/** 简化版：仅文本标签的不可交互芯片（替代 AssistChip）。 */
@Composable
fun MiuixInfoChip(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    containerColor: Color = MiuixTheme.colorScheme.surfaceVariant,
    contentColor: Color = MiuixTheme.colorScheme.onSurface
) {
    MiuixChip(
        modifier = modifier,
        onClick = null,
        containerColor = containerColor,
        contentColor = contentColor,
        leadingIcon = leadingIcon
    ) {
        Text(text = text, style = MiuixTheme.textStyles.footnote2)
    }
}

/** 带勾选图标的选中态芯片（替代 FilterChip 的 selected 样式）。 */
@Composable
fun MiuixSelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    selectedContainerColor: Color = MiuixTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MiuixTheme.colorScheme.onPrimaryContainer
) {
    MiuixChip(
        modifier = modifier,
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        leadingIcon = leadingIcon,
        selectedContainerColor = selectedContainerColor,
        selectedContentColor = selectedContentColor
    ) {
        Text(text = text, style = MiuixTheme.textStyles.footnote2)
        if (selected) {
            Spacer(Modifier.size(3.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
