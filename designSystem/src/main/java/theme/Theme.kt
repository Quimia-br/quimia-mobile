package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryOnColorLight,
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
    onPrimary = PrimaryOnColorDark,
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
