package theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import com.quimia.designsystem.R

internal val LocalQuimiaHighContrast = staticCompositionLocalOf { false }

val HighContrastTokens = DarkTokens.copy(
    onPrimary = LightTokens.foregroundPrimary,
    textPrimary = Color.White, textSecondary = Color.White, foregroundPrimary = Color.White,
    foregroundSecondary = Color.White, foregroundSubtle = Color.White, foregroundDisabled = Color(0xFFCCCCCC),
)

@Composable
fun quimiaHighContrastEnabled(): Boolean = LocalQuimiaHighContrast.current

internal val DyslexicFontFamily = FontFamily(
    Font(R.font.opendyslexic_regular, FontWeight.Normal),
    Font(R.font.opendyslexic_bold, FontWeight.Bold),
)

internal val LocalQuimiaFontFamily = staticCompositionLocalOf { AeonikFontFamily }

@Composable
fun quimiaDyslexicFontEnabled(): Boolean = LocalQuimiaFontFamily.current == DyslexicFontFamily
