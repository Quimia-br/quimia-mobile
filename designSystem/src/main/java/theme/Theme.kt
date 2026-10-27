package theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalQuimiaDarkTheme = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = ForegroundPrimaryLight,
    primaryContainer = Color(0xFFB3E5CE),
    onPrimaryContainer = PrimaryOnColorLight,
    
    secondary = SecondaryLight,
    onSecondary = SecondaryOnColorLight,
    secondaryContainer = Color(0xFFE8DAFF),
    onSecondaryContainer = SecondaryOnColorLight,
    
    surface = SurfaceBackgroundLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE3E3E3),
    onSurfaceVariant = ForegroundSubtleLight,
    
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    
    outline = BorderPrimaryLight,
    outlineVariant = BorderSecondaryLight,
    
    error = ErrorLight,
    onError = ForegroundOnColorLight,
    errorContainer = Color(0xFFFFDADA),
    onErrorContainer = Color(0xFF410000)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = ForegroundPrimaryLight,
    primaryContainer = Color(0xFF005A3F),
    onPrimaryContainer = PrimaryOnColorDark,
    
    secondary = SecondaryDark,
    onSecondary = SecondaryOnColorDark,
    secondaryContainer = Color(0xFF402E5C),
    onSecondaryContainer = SecondaryOnColorDark,
    
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF444444),
    onSurfaceVariant = ForegroundSubtleDark,
    
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    outline = BorderPrimaryDark,
    outlineVariant = BorderSecondaryDark,
    
    error = ErrorDark,
    onError = ForegroundOnColorDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDADA)
)

@Composable
fun QuimiaTheme(
    darkTheme: Boolean = false,
    highContrast: Boolean = false,
    useDyslexicFont: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> DarkColorScheme.copy(
            primary = HighContrastTokens.primary, onPrimary = HighContrastTokens.onPrimary,
            secondary = HighContrastTokens.secondary, onSecondary = Color.White,
            surface = HighContrastTokens.surfaceBase, onSurface = Color.White,
            background = HighContrastTokens.surfaceBackground, onBackground = Color.White,
            surfaceVariant = HighContrastTokens.secondary, onSurfaceVariant = Color.White,
            outline = HighContrastTokens.borderFocus, outlineVariant = HighContrastTokens.borderPrimary,
            error = HighContrastTokens.error, onError = Color.White,
        )
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalQuimiaDarkTheme provides (darkTheme || highContrast),
        LocalQuimiaHighContrast provides highContrast,
        LocalQuimiaFontFamily provides if (useDyslexicFont) DyslexicFontFamily else AeonikFontFamily,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
