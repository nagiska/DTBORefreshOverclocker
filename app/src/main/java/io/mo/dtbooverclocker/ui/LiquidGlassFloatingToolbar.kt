package io.mo.dtbooverclocker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 带 Liquid Glass（液态玻璃）效果的 Miuix 风格悬浮底栏。
 *
 * - 外形对齐 Miuix [top.yukonga.miuix.kmp.basic.FloatingToolbar]：悬浮胶囊 + 投影；
 * - 通过 Backdrop 库对后方页面内容实时模糊 + 透镜折射，呈现液态玻璃质感；
 * - API < 31 时 Backdrop 的 blur/lens 效果自动降级为 no-op（仅半透明底色），不会崩溃。
 */
@Composable
fun LiquidGlassFloatingToolbar(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
    cornerRadius: Dp = 50.dp,
    outsidePadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .padding(outsidePadding)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 14.dp,
                    color = Color.Black,
                    alpha = 0.2f
                )
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    // 对后方页面内容做实时模糊与透镜折射（单位为 px，随密度缩放）
                    blur(18.dp.toPx())
                    lens(10.dp.toPx(), 22.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(color)
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
