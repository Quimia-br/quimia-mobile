package theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tokens moved from Color.kt — top-level vals for backward compatibility

// Black and White
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// Border
val BorderPrimaryLight = Color(0xFFD9D9D9)
val BorderPrimaryDark = Color(0xFFD9D9D9)

val BorderSecondaryLight = Color(0xFF404040)
val BorderSecondaryDark = Color(0xFF404040)

val BorderFocusLight = Color(0xFF00DC9D)
val BorderFocusDark = Color(0xFF00DC9D)

// Brand
val BrandPrimary = Color(0xFF1EEE97)
val BrandSecondary = Color(0xFF5ADFEA)
val BrandAccent = Color(0xFF00DC9D)

// FeedBack
val FeedbackSuccess = Color(0xFF1EEE97)
val FeedbackWarning = Color(0xFF5ADFEA)
val FeedbackError = Color(0xFF00DC9D)

// Foreground
val ForegroundPrimaryLight = Color(0xFF404040)
val ForegroundPrimaryDark = Color(0xFFFFFFFF)

val ForegroundSecondaryLight = Color(0xFF404040)
val ForegroundSecondaryDark = Color(0xFFFFFFFF)

val ForegroundDisabledLight = Color(0xFFB2B2B2)
val ForegroundDisabledDark = Color(0xFFE5E5E5)

val ForegroundErrorLight = Color(0xFFFFFFFF)
val ForegroundErrorDark = Color(0xFFFFFFFF)

val ForegroundOnColorLight = Color(0xFFFFFFFF)
val ForegroundOnColorDark = Color(0xFF000000)

val ForegroundSubtleLight = Color(0xFF949494)
val ForegroundSubtleDark = Color(0xFF949494)

// Padding
val PaddingNone = 0
val PaddingSmall = 12
val PaddingMedium = 16
val PaddingLarge = 24

// Primary
val PrimaryLight = Color(0xFF5ADFEA)
val PrimaryDark = Color(0xFF5ADFEA)

val PrimaryPressedLight = Color(0xFF02C1D6)
val PrimaryPressedDark = Color(0xFF02C1D6)

val PrimaryOnColorLight = Color(0xFFFFFFFF)
val PrimaryOnColorDark = Color(0xFFFFFFFF)

// Radius
val RadiusNone = 0
val RadiusSmall = 8
val RadiusMedium = 16
val RadiusLarge = 24
val RadiusFull = 1000

// Secondary
val SecondaryLight = Color(0xFFECECEC)
val SecondaryDark = Color(0xFF646464)

val SecondaryPressedLight = Color(0xFFC2C2C2)
val SecondaryPressedDark = Color(0xFF383838)

val SecondaryOnColorLight = Color(0xFFB0B0B0)
val SecondaryOnColorDark = Color(0xFFFFFFFF)

// Surface
val SurfaceBackgroundLight = Color(0xFFF7F6F5)
val SurfaceBackgroundDark = Color(0xFF2E2E2E)

val SurfaceBaseLight = Color(0xFFFFFFFF)
val SurfaceBaseDark = Color(0xFF494949)

val SurfaceDisabledLight = Color(0xFFE3E3E3)
val SurfaceDisabledDark = Color(0xFF848484)

// Text
val TextSmall = 14
val TextMedium = 16
val TextBig = 20
val TextHuge = 36

// TextColor
val TextColorLight = Color(0xFF3F3F3F)
val TextColorDark = Color(0xFFFFFFFF)

val TextColorSecondaryLight = Color(0xFFACACAC)
val TextColorSecondaryDark = Color(0xFFFFFFFF)

// Aliases for backward compatibility with Theme/ColorTokens
val SurfaceLight = SurfaceBackgroundLight
val SurfaceDark = SurfaceBackgroundDark

val BackgroundLight = SurfaceBackgroundLight
val BackgroundDark = SurfaceBackgroundDark

val TextPrimaryLight = TextColorLight
val TextPrimaryDark = TextColorDark

val TextSecondaryLight = TextColorSecondaryLight
val TextSecondaryDark = TextColorSecondaryDark

// Feedback tokens (common names used by Theme/ColorTokens)
val ErrorLight = Color(0xFFFF2B7C)
val ErrorDark = Color(0xFFFF2B7C)

val SuccessLight = Color(0xFF1EEE97)
val SuccessDark = Color(0xFF1EEE97)

val WarningLight = Color(0xFFFCC368)
val WarningDark = Color(0xFFFCC368)

val InfoLight = Color(0xFF0288D1)
val InfoDark = Color(0xFF42A5F5)

// Helpers for Compose usage
fun Int.toDp(): Dp = this.dp
fun Int.toSp(): TextUnit = this.sp
