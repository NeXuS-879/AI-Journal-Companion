package com.example.aijournalcompanion.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF365D91), onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EEF8), onPrimaryContainer = Color(0xFF243247),
    secondary = Color(0xFF52647C),
    secondaryContainer = Color(0xFFDCE6F4), onSecondaryContainer = Color(0xFF243247),
    tertiary = Color(0xFF45664C),
    tertiaryContainer = Color(0xFFE8F3EA), onTertiaryContainer = Color(0xFF213D28),
    background = Color(0xFFF8FAFC), surface = Color(0xFFF8FAFC),
    onBackground = Color(0xFF243247), onSurface = Color(0xFF243247),
    surfaceContainer = Color(0xFFE8EEF8), surfaceContainerLow = Color.White,
    outline = Color(0xFF747F8D),
)

@Composable
fun AIJournalCompanionTheme(
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
