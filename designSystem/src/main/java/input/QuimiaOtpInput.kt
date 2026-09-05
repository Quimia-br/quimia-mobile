package input

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.LightTokens
import theme.RadiusFull
import theme.RadiusMedium
import theme.RadiusSmall
import theme.Typography
import theme.quimiaColorTokens
import theme.toDp

@Composable
fun QuimiaOtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
    length: Int = 4,
    textStyle: TextStyle = Typography.titleLarge.copy(
        fontSize = 36.sp,
        lineHeight = TextUnit.Unspecified
    ),
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    containerColor: Color? = null,
    textColor: Color? = null,
    radius: Int? = RadiusFull,
    textMargin: Int = 7,
    isError: Boolean = false,
    errorColor: Color? = null,
    errorMessage: String? = null,
    errorTextStyle: TextStyle = Typography.titleMedium.copy(
        lineHeight = TextUnit.Unspecified
    ),
) {
    var isFieldFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val tokens = quimiaColorTokens()
    val actualContainer = containerColor ?: tokens.secondary
    val actualTextColor = textColor ?: tokens.foregroundPrimary

    val resolvedTextStyle = textStyle.copy(
        color = actualTextColor,
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )
    val actualErrorColor = errorColor ?: tokens.error

    Column(
        verticalArrangement = Arrangement.spacedBy(textMargin.toDp(), Alignment.CenterVertically),
        modifier = modifier
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = value,
            onValueChange = { new ->
                if (new.length <= length && new.all(Char::isDigit)) {
                    onValueChange(new)
                }
            },
            modifier = Modifier
                .onFocusChanged { isFieldFocused = it.isFocused }
                .semantics {
                    contentDescription = "One-time password, $length digits"
                    if (isError && errorMessage != null) error(errorMessage)
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            interactionSource = interactionSource,
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    repeat(length) { index ->
                        val char = value.getOrNull(index)?.toString()
                        val isFocused = isFieldFocused && index == value.length
                        QuimiaOtpItem(
                            value = char,
                            modifier = itemModifier.semantics {
                                contentDescription = "Digit ${index + 1} of $length"
                            },
                            textStyle = resolvedTextStyle,
                            containerColor = actualContainer,
                            textColor = actualTextColor,
                            radius = radius,
                            isError = isError,
                            isFocused = isFocused,
                            errorColor = actualErrorColor
                        )
                    }
                }
            }
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = errorTextStyle.copy(
                    color = actualErrorColor
                )
            )
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
    radius: Int? = RadiusFull,
    isFocused: Boolean = false,
    isError: Boolean = false,
    errorColor: Color? = null
) {
    val tokens = quimiaColorTokens()
    val actualContainer = containerColor ?: tokens.secondary
    val actualTextColor = textColor ?: tokens.foregroundPrimary

    val shape = RoundedCornerShape(radius?.toDp() ?: RadiusMedium.toDp())
    val resolvedTextStyle = textStyle.copy(
        color = actualTextColor,
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )
    val borderColor by animateColorAsState(
        if (isError) errorColor ?: tokens.error else Color.Transparent
    )

    val text = value ?: ""

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(77.dp)
            .height(123.dp)
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
                color = LightTokens.background,
                shape = RoundedCornerShape(RadiusSmall.toDp())
            )
            .height(493.dp)
            .padding(20.dp)
            .width(329.dp)
    ) {
        QuimiaOtpInput(value = "1234", onValueChange = {})
        QuimiaOtpInput(value = "", onValueChange = {})
        QuimiaOtpInput(value = "1234", onValueChange = {}, isError = true, errorMessage = "Error description")
    }
}