package input

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.LightTokens
import theme.RadiusFull
import theme.RadiusSmall
import theme.Typography
import theme.quimiaColorTokens

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun QuimiaOtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.NumberPassword
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    itemModifier: Modifier = Modifier,
    itemWidth: Dp = 77.dp,
    itemHeight: Dp = 123.dp,
    itemSpacing: Dp = 4.dp,
    length: Int = 4,
    textStyle: TextStyle = Typography.titleLarge.copy(
        fontSize = 36.sp,
        lineHeight = TextUnit.Unspecified
    ),
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    containerColor: Color? = null,
    textColor: Color? = null,
    radius: Int = RadiusFull,
    textMargin: Int = 7,
    error: String? = null,
    errorColor: Color? = null,
    errorTextStyle: TextStyle = Typography.titleMedium.copy(
        lineHeight = TextUnit.Unspecified
    ),
) {
    val isFieldFocused by interactionSource.collectIsFocusedAsState()

    val isError = error != null

    val tokens = quimiaColorTokens()
    val actualContainer = containerColor ?: tokens.secondary
    val actualTextColor = textColor ?: tokens.foregroundPrimary
    val actualErrorColor = errorColor ?: tokens.error

    val resolvedTextStyle = textStyle.copy(
        color = actualTextColor,
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )
    val resolvedErrorTextStyle = errorTextStyle.copy(
        color = actualErrorColor,
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(textMargin.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = value,
            onValueChange = { new ->
                val digitsOnly = new.filter(Char::isDigit).take(length)
                onValueChange(digitsOnly)
            },
            modifier = Modifier
                .clearAndSetSemantics {
                    contentDescription = "One-time password, $length digits"
                    error?.let { error(it) }
                },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val aspectRatio = 1.618034f

                    val safeLength = if (length > 0) length else 1

                    val computedItemWidth = (this.maxWidth / safeLength - itemSpacing).coerceIn(24.dp, itemWidth)
                    val computedItemHeight = (computedItemWidth * aspectRatio).coerceAtMost(itemHeight)

                    Box(modifier = Modifier.size(0.dp)) {
                        innerTextField()
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        repeat(length) { index ->
                            val char = value.getOrNull(index)?.toString()
                            val isNextEmptySlot = index == value.length
                            val isLastSlotWhenFull = index == length - 1 && value.length == length
                            val isFocused = isFieldFocused && (isNextEmptySlot || isLastSlotWhenFull)

                            QuimiaOtpItem(
                                value = char,
                                modifier = itemModifier,
                                textStyle = resolvedTextStyle,
                                containerColor = actualContainer,
                                textColor = actualTextColor,
                                width = computedItemWidth,
                                height = computedItemHeight,
                                radius = radius,
                                isError = isError,
                                isFocused = isFocused,
                                errorColor = actualErrorColor
                            )
                        }
                    }
                }
            }
        )

        error?.let { description ->
            Text(text = description, style = resolvedErrorTextStyle)
        }
    }
}

@Composable
fun QuimiaOtpItem(
    value: String?,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = Typography.titleLarge.copy(
        fontSize = 36.sp,
        lineHeight = TextUnit.Unspecified
    ),
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    containerColor: Color? = null,
    textColor: Color? = null,
    width: Dp = 77.dp,
    height: Dp = 123.dp,
    radius: Int = RadiusFull,
    isFocused: Boolean = false,
    focusColor: Color? = null,
    isError: Boolean = false,
    errorColor: Color? = null
) {
    val text = value ?: ""

    val tokens = quimiaColorTokens()
    val actualContainer = containerColor ?: tokens.secondary
    val actualTextColor = textColor ?: tokens.foregroundPrimary
    val actualErrorColor = errorColor ?: tokens.error
    val actualFocusColor = focusColor ?: tokens.borderFocus

    val shape = RoundedCornerShape(radius.dp)
    val resolvedTextStyle = textStyle.copy(
        color = actualTextColor,
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )
    val borderColor by animateColorAsState(
        when {
            isError -> actualErrorColor
            isFocused -> actualFocusColor
            else -> Color.Transparent
        },
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(width)
            .height(height)
            .background(
                color = actualContainer,
                shape = shape
            )
            .border(
                width = 3.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        Text(
            text = text,
            style = resolvedTextStyle
        )
    }
}

@Preview
@Composable
private fun QuimiaOtpInputPreview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(
                color = LightTokens.surfaceBackground,
                shape = RoundedCornerShape(RadiusSmall.dp)
            )
            .height(493.dp)
            .padding(20.dp)
            .width(329.dp)
    ) {
        QuimiaOtpInput(value = "", onValueChange = {})
        QuimiaOtpInput(value = "1234", onValueChange = {})
        QuimiaOtpInput(value = "1234", onValueChange = {}, error = "Error description")
    }
}