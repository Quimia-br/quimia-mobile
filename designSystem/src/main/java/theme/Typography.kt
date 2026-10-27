package theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quimia.designsystem.R

internal val AeonikFontFamily = FontFamily(
    Font(R.font.aeonik_regular, FontWeight.Normal),
    Font(R.font.aeonik_light, FontWeight.Light),
    Font(R.font.aeonik_medium, FontWeight.Medium),
    Font(R.font.aeonik_bold, FontWeight.Bold),
    Font(R.font.aeonik_thin, FontWeight.Thin),
)

val QuimiaFontFamily: FontFamily
    @Composable get() = LocalQuimiaFontFamily.current

object QuimiaTextStyles {
    @Composable
    private fun medium(size: Int, lineHeight: Int) = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

    val medium11: TextStyle
        @Composable get() = medium(11, 13)
    val medium12: TextStyle
        @Composable get() = medium(12, 14)
    val medium14: TextStyle
        @Composable get() = medium(14, 17)
    val medium15: TextStyle
        @Composable get() = medium(15, 18)
    val medium16: TextStyle
        @Composable get() = medium(16, 19)
    val medium20: TextStyle
        @Composable get() = medium(20, 24)
    val medium22: TextStyle
        @Composable get() = medium(22, 26)
    val medium24: TextStyle
        @Composable get() = medium(24, 29)
    val medium28: TextStyle
        @Composable get() = medium(28, 34)
    val medium32: TextStyle
        @Composable get() = medium(32, 38)
    val medium36: TextStyle
        @Composable get() = medium(36, 43)
    val medium45: TextStyle
        @Composable get() = medium(45, 54)
    val medium57: TextStyle
        @Composable get() = medium(57, 68)

    @Composable
    fun tagForDiameter(diameter: Float) = medium16.copy(
        fontSize = (diameter * 0.50f).sp,
        lineHeight = (diameter * 0.50f).sp,
    )
}

val Typography: Typography
    @Composable get() = Typography(
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
