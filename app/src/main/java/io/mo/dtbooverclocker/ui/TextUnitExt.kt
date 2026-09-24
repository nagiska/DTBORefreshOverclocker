package io.mo.dtbooverclocker.ui

import androidx.compose.ui.unit.TextUnit

/**
 * Miuix 的默认文字样式（footnote1 / footnote2 / body2 等）未显式设置 lineHeight，
 * 其值为 [TextUnit.Unspecified]，直接对其执行乘法会抛出
 * `IllegalArgumentException: Cannot perform operation for Unspecified type` 导致闪退
 * （该崩溃由 CI 模拟器冒烟测试实际捕获：免责声明对话框 / 关于页必崩）。
 *
 * 此扩展安全地按比例缩放 lineHeight：无法缩放时原样返回。
 */
internal fun TextUnit.scaleLineHeight(factor: Float): TextUnit =
    try {
        this * factor
    } catch (_: IllegalArgumentException) {
        // TextUnit.Unspecified 不支持算术运算，保持原值
        this
    }
