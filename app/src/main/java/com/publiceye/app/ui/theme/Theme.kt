package com.publiceye.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Light-only theme — dark mode is post-MVP
private val LightColorScheme = lightColorScheme(
    primary             = Blue40,
    onPrimary           = Grey99,
    primaryContainer    = Blue90,
    onPrimaryContainer  = Blue10,

    secondary           = Amber40,
    onSecondary         = Grey99,
    secondaryContainer  = Amber90,
    onSecondaryContainer = Amber10,

    background          = Grey99,
    onBackground        = Grey10,

    surface             = Grey99,
    onSurface           = Grey10,
    surfaceVariant      = Grey95,
    onSurfaceVariant    = Grey20,

    error               = Red40,
    onError             = Grey99,
    errorContainer      = Red90,
    onErrorContainer    = Red10,

    outline             = Grey20,
    outlineVariant      = Grey90,
)

@Composable
fun PublicEyeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content,
    )
}
