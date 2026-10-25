package theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import com.quimia.designsystem.R

internal val LocalQuimiaHighContrast = staticCompositionLocalOf { false }

val HighContrastTokens = DarkTokens.copy(
    primary = Color(0xFF0057B7), primaryPressed = Color(0xFF00438C), onPrimary = Color.White,
    primaryDisabled = Color(0xFF333333), secondary = Color(0xFF222222), secondaryPressed = Color(0xFF444444),
    onSecondary = Color.White, surfaceBackground = Color.Black, surfaceBase = Color.Black,
    surfaceDisabled = Color(0xFF222222), borderPrimary = Color.White, borderSecondary = Color.White,
    borderFocus = Color.White, brandPrimary = Color(0xFF006A44), brandSecondary = Color.Black,
    textPrimary = Color.White, textSecondary = Color.White, foregroundPrimary = Color.White,
    foregroundSecondary = Color.White, foregroundSubtle = Color.White, foregroundDisabled = Color(0xFFCCCCCC),
    foregroundError = Color.White, onForeground = Color.Black,
    error = Color(0xFFC00036), success = Color(0xFF006A44), warning = Color(0xFF8A5A00), info = Color(0xFF0057B7),
)

internal val DyslexicFontFamily = FontFamily(
    Font(R.font.opendyslexic_regular, FontWeight.Normal),
    Font(R.font.opendyslexic_bold, FontWeight.Bold),
)

internal val LocalQuimiaFontFamily = staticCompositionLocalOf { AeonikFontFamily }

@Composable
fun quimiaDyslexicFontEnabled(): Boolean = LocalQuimiaFontFamily.current == DyslexicFontFamily
