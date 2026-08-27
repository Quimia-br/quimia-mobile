package theme

import androidx.compose.material3.Typography
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
    Font(R.font.aeonik_thin, FontWeight.Thin)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 57.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 45.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 44.sp
    ),

    headlineLarge = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp
    ),

    titleLarge = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    ),

    labelLarge = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = QuimiaFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp
    )
)
