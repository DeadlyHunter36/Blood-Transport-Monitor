package com.bloodtransport.iot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HighContrastMonochromeScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = NeutralGrayDark,
    onPrimaryContainer = PureWhite,
    secondary = PureWhite,
    onSecondary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = PureBlack,
    onSurface = PureWhite,
    surfaceVariant = NeutralGrayDark,
    onSurfaceVariant = PureWhite,
    outline = NeutralGrayBorder
)

@Composable
fun BloodTransportTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HighContrastMonochromeScheme,
        typography = BloodTransportTypography,
        content = content
    )
}
