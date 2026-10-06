package com.example.focus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TomatoRedDark,
    onPrimary = OnTomatoRedDark,
    primaryContainer = TomatoRedContainerDark,
    onPrimaryContainer = OnTomatoRedContainerDark,
    secondary = SecondaryCoralDark,
    onSecondary = OnSecondaryCoralDark,
    secondaryContainer = SecondaryCoralContainerDark,
    onSecondaryContainer = OnSecondaryCoralContainerDark,
    tertiary = TertiaryAmberDark,
    onTertiary = OnTertiaryAmberDark,
    tertiaryContainer = TertiaryAmberContainerDark,
    onTertiaryContainer = OnTertiaryAmberContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = TomatoRedLight,
    onPrimary = OnTomatoRedLight,
    primaryContainer = TomatoRedContainerLight,
    onPrimaryContainer = OnTomatoRedContainerLight,
    secondary = SecondaryCoralLight,
    onSecondary = OnSecondaryCoralLight,
    secondaryContainer = SecondaryCoralContainerLight,
    onSecondaryContainer = OnSecondaryCoralContainerLight,
    tertiary = TertiaryAmberLight,
    onTertiary = OnTertiaryAmberLight,
    tertiaryContainer = TertiaryAmberContainerLight,
    onTertiaryContainer = OnTertiaryAmberContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

@Composable
fun FocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
