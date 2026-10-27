package theme

import androidx.compose.ui.graphics.Color

fun actionContentColor(background: Color, requested: Color?, tokens: QuimiaColorTokens): Color {
    val opaque = background.copy(alpha = 1f)
    val brandSurface = listOf(tokens.primary, tokens.primaryPressed, tokens.primaryDisabled, tokens.brandSecondary)
        .any { it.copy(alpha = 1f) == opaque }
    return if (brandSurface) LightTokens.foregroundPrimary else requested ?: tokens.foregroundPrimary
}
