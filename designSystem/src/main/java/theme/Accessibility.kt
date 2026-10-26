package theme

import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import com.quimia.designsystem.R

internal val LocalQuimiaHighContrast = staticCompositionLocalOf { false }

val HighContrastTokens = DarkTokens.copy(
    onPrimary = LightTokens.foregroundPrimary,
    textPrimary = Color.White, textSecondary = Color.White, foregroundPrimary = Color.White,
    foregroundSecondary = Color.White, foregroundSubtle = Color.White, foregroundDisabled = Color(0xFFCCCCCC),
)

@Composable
fun quimiaHighContrastEnabled(): Boolean = LocalQuimiaHighContrast.current

fun resolveQuimiaContrastBorder(highContrast: Boolean, tokens: QuimiaColorTokens): BorderStroke? =
    if (highContrast) BorderStroke(3.dp, tokens.borderFocus) else null

@Composable
fun quimiaContrastBorder(enabled: Boolean = quimiaHighContrastEnabled()): BorderStroke? =
    resolveQuimiaContrastBorder(enabled, quimiaColorTokens())

@Composable
fun Modifier.quimiaContrastOutline(shape: Shape, enabled: Boolean = quimiaHighContrastEnabled()): Modifier =
    quimiaContrastBorder(enabled)?.let { border(it, shape) } ?: this

internal val DyslexicFontFamily = FontFamily(
    Font(R.font.opendyslexic_regular, FontWeight.Normal),
    Font(R.font.opendyslexic_bold, FontWeight.Bold),
)

internal val LocalQuimiaFontFamily = staticCompositionLocalOf { AeonikFontFamily }

@Composable
fun quimiaDyslexicFontEnabled(): Boolean = LocalQuimiaFontFamily.current == DyslexicFontFamily
