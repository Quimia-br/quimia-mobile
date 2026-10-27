package theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


data class QuimiaColorTokens(
    val black: Color,
    val white: Color,

    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primaryDisabled: Color,
    
    val secondary: Color,
    val secondaryPressed: Color,
    val onSecondary: Color,
    
    val surfaceBackground: Color,
    val surfaceBase: Color,
    val surfaceDisabled: Color,
    
    val borderPrimary: Color,
    val borderSecondary: Color,
    val borderFocus: Color,

    val brandPrimary: Color,
    val brandSecondary: Color,
    val brandAccent: Color,
    
    val textPrimary: Color,
    val textSecondary: Color,
    
    val error: Color,
    val success: Color,
    val warning: Color,
    val info: Color,

    val foregroundPrimary: Color,
    val foregroundSecondary: Color,
    val foregroundDisabled: Color,
    val foregroundError: Color,
    val onForeground: Color,
    val foregroundSubtle: Color
)

val LightTokens = QuimiaColorTokens(
    black = Black,
    white = White,

    primary = PrimaryLight,
    primaryPressed = PrimaryPressedLight,
    onPrimary = PrimaryOnColorLight,
    primaryDisabled = PrimaryDisabledLight,

    secondary = SecondaryLight,
    secondaryPressed = SecondaryPressedLight,
    onSecondary = SecondaryOnColorLight,

    surfaceBackground = SurfaceBackgroundLight,
    surfaceBase = SurfaceBaseLight,
    surfaceDisabled = SurfaceDisabledLight,

    borderPrimary = BorderPrimaryLight,
    borderSecondary = BorderSecondaryLight,
    borderFocus = BorderFocusLight,

    brandPrimary = BrandPrimary,
    brandSecondary = BrandSecondary,
    brandAccent = BrandAccent,

    textPrimary = TextColorLight,
    textSecondary = TextColorSecondaryLight,

    error = ErrorLight,
    success = SuccessLight,
    warning = WarningLight,
    info = InfoLight,

    foregroundPrimary = ForegroundPrimaryLight,
    foregroundSecondary = ForegroundSecondaryLight,
    foregroundDisabled = ForegroundDisabledLight,
    foregroundError = ForegroundErrorLight,
    onForeground = ForegroundOnColorLight,
    foregroundSubtle = ForegroundSubtleLight
)

val DarkTokens = QuimiaColorTokens(
    black = Black,
    white = White,

    primary = PrimaryDark,
    primaryPressed = PrimaryPressedDark,
    onPrimary = PrimaryOnColorDark,
    primaryDisabled = PrimaryDisabledDark,

    secondary = SecondaryDark,
    secondaryPressed = SecondaryPressedDark,
    onSecondary = SecondaryOnColorDark,

    surfaceBackground = SurfaceBackgroundDark,
    surfaceBase = SurfaceBaseDark,
    surfaceDisabled = SurfaceDisabledDark,

    borderPrimary = BorderPrimaryDark,
    borderSecondary = BorderSecondaryDark,
    borderFocus = BorderFocusDark,

    brandPrimary = BrandPrimary,
    brandSecondary = BrandSecondary,
    brandAccent = BrandAccent,

    textPrimary = TextColorDark,
    textSecondary = TextColorSecondaryDark,

    error = ErrorDark,
    success = SuccessDark,
    warning = WarningDark,
    info = InfoDark,

    foregroundPrimary = ForegroundPrimaryDark,
    foregroundSecondary = ForegroundSecondaryDark,
    foregroundDisabled = ForegroundDisabledDark,
    foregroundError = ForegroundErrorDark,
    onForeground = ForegroundOnColorDark,
    foregroundSubtle = ForegroundSubtleDark,
)

@Composable
fun quimiaColorTokens(forceDark: Boolean? = null): QuimiaColorTokens {
    if (LocalQuimiaHighContrast.current) return HighContrastTokens
    val useDark = forceDark ?: LocalQuimiaDarkTheme.current
    return if (!useDark) LightTokens else DarkTokens
}
