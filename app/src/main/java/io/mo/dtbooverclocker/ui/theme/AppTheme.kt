package io.mo.dtbooverclocker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.defaultTextStyles
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * App-wide Miuix (HyperOS style) theme.
 *
 * Replaces the previous Material3 [androidx.compose.material3.MaterialTheme] wrapper:
 * all Miuix components read colors/text styles from [MiuixTheme].
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()
    MiuixTheme(
        colors = colors,
        textStyles = defaultTextStyles()
    ) {
        content()
    }
}
