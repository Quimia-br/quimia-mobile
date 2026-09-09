package theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tokens de Cores Centralizados
// Use estes em vez de cores hardcoded nos componentes

data class QuimiaColorTokens(
    // Black White
    val black: Color,
    val white: Color,

    // Primary
    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primaryDisabled: Color,
    
    // Secondary
    val secondary: Color,
    val secondaryPressed: Color,
    val onSecondary: Color,
    
    // Surface
    val surfaceBackground: Color,
    val surfaceBase: Color,
    val surfaceDisabled: Color,
    
    // Border
    val borderPrimary: Color,
    val borderSecondary: Color,
    val borderFocus: Color,

    // Brand
    val brandPrimary: Color,
    val brandSecondary: Color,
    val brandAccent: Color,
    
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
    val foregroundDisabled: Color,
    val foregroundError: Color,
    val onForeground: Color,
    val foregroundSubtle: Color
)

val LightTokens = QuimiaColorTokens(
    // Black and White
    black = Black,
    white = White,

    // Primary
    primary = PrimaryLight,
    primaryPressed = PrimaryPressedLight,
    onPrimary = PrimaryOnColorLight,
    primaryDisabled = PrimaryDisabledLight,

    // Secondary
    secondary = SecondaryLight,
    secondaryPressed = SecondaryPressedLight,
    onSecondary = SecondaryOnColorLight,

    // Surface
    surfaceBackground = SurfaceBackgroundLight,
    surfaceBase = SurfaceBaseLight,
    surfaceDisabled = SurfaceDisabledLight,

    // Border
    borderPrimary = BorderPrimaryLight,
    borderSecondary = BorderSecondaryLight,
    borderFocus = BorderFocusLight,

    // Brand
    brandPrimary = BrandPrimary,
    brandSecondary = BrandSecondary,
    brandAccent = BrandAccent,

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
    foregroundDisabled = ForegroundDisabledLight,
    foregroundError = ForegroundErrorLight,
    onForeground = ForegroundOnColorLight,
    foregroundSubtle = ForegroundSubtleLight
)

val DarkTokens = QuimiaColorTokens(
    // Black White
    black = Black,
    white = White,

    // Primary
    primary = PrimaryDark,
    primaryPressed = PrimaryPressedDark,
    onPrimary = PrimaryOnColorDark,
    primaryDisabled = PrimaryDisabledDark,

    // Secondary
    secondary = SecondaryDark,
    secondaryPressed = SecondaryPressedDark,
    onSecondary = SecondaryOnColorDark,

    // Surface
    surfaceBackground = SurfaceBackgroundDark,
    surfaceBase = SurfaceBaseDark,
    surfaceDisabled = SurfaceDisabledDark,

    // Border
    borderPrimary = BorderPrimaryDark,
    borderSecondary = BorderSecondaryDark,
    borderFocus = BorderFocusDark,

    // Brand
    brandPrimary = BrandPrimary,
    brandSecondary = BrandSecondary,
    brandAccent = BrandAccent,

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
    foregroundDisabled = ForegroundDisabledDark,
    foregroundError = ForegroundErrorDark,
    onForeground = ForegroundOnColorDark,
    foregroundSubtle = ForegroundSubtleDark,
)

// Função para acessar os tokens do tema atual
@Composable
fun quimiaColorTokens(forceDark: Boolean? = null): QuimiaColorTokens {
    // Prefer explicit override, otherwise fall back to system dark flag.
    // Comparing Color objects (previous implementation) can be unreliable because
    // colorScheme values may be mutated or created at runtime. Using isSystemInDarkTheme
    // aligns token selection with the active UI mode.
    val useDark = forceDark ?: androidx.compose.foundation.isSystemInDarkTheme()
    return if (!useDark) LightTokens else DarkTokens
}
