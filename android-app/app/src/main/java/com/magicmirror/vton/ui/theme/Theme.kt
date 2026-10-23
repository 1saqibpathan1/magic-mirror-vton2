package com.magicmirror.vton.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MirrorColorScheme = darkColorScheme(
    background = MirrorBackground,
    surface = MirrorSurface,
    surfaceVariant = MirrorSurfaceVariant,
    primary = MirrorAccent,
    onPrimary = MirrorOnAccent,
    onBackground = MirrorOnBackground,
    onSurface = MirrorOnBackground,
    onSurfaceVariant = MirrorOnSurfaceMuted,
    error = MirrorError,
)

@Composable
fun MagicMirrorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MirrorColorScheme,
        typography = MirrorTypography,
        content = content,
    )
}
