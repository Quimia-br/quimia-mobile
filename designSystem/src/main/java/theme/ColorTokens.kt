package theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tokens de Cores Centralizados
// Use estes em vez de cores hardcoded nos componentes

data class QuimiaColorTokens(
    // Primary
    val primary: Color,
    val onPrimary: Color,
    
    // Secondary
    val secondary: Color,
    val onSecondary: Color,
    
    // Surface
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    
    // Background
    val background: Color,
    val onBackground: Color,
    
    // Border
    val borderPrimary: Color,
    val borderSecondary: Color,
    val borderFocus: Color,
    
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    
    // Feedback
    val error: Color,
    val success: Color,
    val warning: Color,
    val info: Color,

    // Foreground
    val foregroundPrimary: Color,
    val foregroundSecondary: Color,
    val foregroundSubtle: Color,

    // Black White
    val black: Color,
    val white: Color
)

val LightTokens = QuimiaColorTokens(
    // Black and White
    black = Black,
    white = White,

    // Primary
    primary = PrimaryLight,
    onPrimary = PrimaryOnColorLight,

    // Secondary
    secondary = SecondaryLight,
    onSecondary = SecondaryOnColorLight,

    // Surface
    surface = SurfaceBaseLight,
    onSurface = ForegroundPrimaryLight,
    surfaceVariant = SurfaceDisabledLight,
    onSurfaceVariant = ForegroundSubtleLight,

    // Background
    background = SurfaceBackgroundLight,
    onBackground = ForegroundPrimaryLight,

    // Border
    borderPrimary = BorderPrimaryLight,
    borderSecondary = BorderSecondaryLight,
    borderFocus = BorderFocusLight,

    // Text
    textPrimary = TextColorLight,
    textSecondary = TextColorSecondaryLight,

    // Feedback
    error = ErrorLight,
    success = SuccessLight,
    warning = WarningLight,
    info = InfoLight,

    // Foreground
    foregroundPrimary = ForegroundPrimaryLight,
    foregroundSecondary = ForegroundSecondaryLight,
    foregroundSubtle = ForegroundSubtleLight
)

val DarkTokens = QuimiaColorTokens(
    // Primary
    primary = PrimaryDark,
    onPrimary = PrimaryOnColorDark,

    // Secondary
    secondary = SecondaryDark,
    onSecondary = SecondaryOnColorDark,

    // Surface
    surface = SurfaceBaseDark,
    onSurface = ForegroundPrimaryDark,
    surfaceVariant = SurfaceDisabledDark,
    onSurfaceVariant = ForegroundSubtleDark,

    // Background
    background = SurfaceBackgroundDark,
    onBackground = ForegroundPrimaryDark,

    // Border
    borderPrimary = BorderPrimaryDark,
    borderSecondary = BorderSecondaryDark,
    borderFocus = BorderFocusDark,

    // Text
    textPrimary = TextColorDark,
    textSecondary = TextColorSecondaryDark,

    // Feedback
    error = ErrorDark,
    success = SuccessDark,
    warning = WarningDark,
    info = InfoDark,

    // Foreground
    foregroundPrimary = ForegroundPrimaryDark,
    foregroundSecondary = ForegroundSecondaryDark,
    foregroundSubtle = ForegroundSubtleDark,

    // Black White
    black = Black,
    white = White
)

// Função para acessar os tokens do tema atual
@Composable
fun quimiaColorTokens(): QuimiaColorTokens {
    val colors = MaterialTheme.colorScheme
    return if (colors.surface == SurfaceLight) LightTokens else DarkTokens
}
