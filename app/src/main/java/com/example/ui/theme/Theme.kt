package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val RedMagicColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = Color.White,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = TitaniumWhite,
    secondary = CyberCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = CyberCyanDark,
    onSecondaryContainer = TitaniumWhite,
    tertiary = MoltenAmber,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TitaniumWhite,
    surface = ObsidianSurface,
    onSurface = TitaniumWhite,
    surfaceVariant = GunmetalCard,
    onSurfaceVariant = SilverMist,
    outline = CarbonBorder,
    error = Color(0xFFFF5252)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = RedMagicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
