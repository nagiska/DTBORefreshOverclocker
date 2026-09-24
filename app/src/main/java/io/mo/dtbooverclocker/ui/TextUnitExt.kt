package io.mo.dtbooverclocker.ui

import androidx.compose.ui.unit.TextUnit

/**
 * Miuix 的默认文字样式（footnote1 / footnote2 / body2 等）未显式设置 lineHeight，
 * 其值为 [TextUnit.Unspecified]，直接对其执行乘法会抛出
 * `IllegalArgumentException: Cannot perform operation for Unspecified type` 导致闪退。
 *
 * 此扩展仅在 lineHeight 已指定时按比例缩放，否则原样返回，保证安全。
 */
internal fun TextUnit.scaleLineHeight(factor: Float): TextUnit =
    if (isSpecified) this * factor else this
