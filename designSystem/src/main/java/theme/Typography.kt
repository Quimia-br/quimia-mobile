package theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quimia.designsystem.R

val QuimiaFontFamily = FontFamily(
    Font(R.font.aeonik_regular, FontWeight.Normal),
    Font(R.font.aeonik_light, FontWeight.Light),
    Font(R.font.aeonik_medium, FontWeight.Medium),
    Font(R.font.aeonik_bold, FontWeight.Bold),
    Font(R.font.aeonik_thin, FontWeight.Thin),
)

object QuimiaTextStyles {
    private fun medium(size: Int, lineHeight: Int) = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

    val medium11 = medium(11, 13)
    val medium12 = medium(12, 14)
    val medium14 = medium(14, 17)
    val medium15 = medium(15, 18)
    val medium16 = medium(16, 19)
    val medium20 = medium(20, 24)
    val medium22 = medium(22, 26)
    val medium24 = medium(24, 29)
    val medium28 = medium(28, 34)
    val medium32 = medium(32, 38)
    val medium36 = medium(36, 43)
    val medium45 = medium(45, 54)
    val medium57 = medium(57, 68)

    fun tagForDiameter(diameter: Float) = medium16.copy(
        fontSize = (diameter * 0.50f).sp,
        lineHeight = (diameter * 0.50f).sp,
    )
}

val Typography = Typography(
    displayLarge = QuimiaTextStyles.medium57,
    displayMedium = QuimiaTextStyles.medium45,
    displaySmall = QuimiaTextStyles.medium36,
    headlineLarge = QuimiaTextStyles.medium32,
    headlineMedium = QuimiaTextStyles.medium28,
    headlineSmall = QuimiaTextStyles.medium24,
    titleLarge = QuimiaTextStyles.medium22,
    titleMedium = QuimiaTextStyles.medium16,
    titleSmall = QuimiaTextStyles.medium14,
    bodyLarge = QuimiaTextStyles.medium16,
    bodyMedium = QuimiaTextStyles.medium14,
    bodySmall = QuimiaTextStyles.medium12,
    labelLarge = QuimiaTextStyles.medium15,
    labelMedium = QuimiaTextStyles.medium12,
    labelSmall = QuimiaTextStyles.medium11,
)
