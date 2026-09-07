package input

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import buttons.QuimiaIconButton
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import theme.LightTokens
import theme.RadiusFull
import theme.RadiusSmall
import theme.Typography
import theme.quimiaColorTokens

@Composable
fun QuimiaInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    description: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
    textStyle: TextStyle = Typography.titleMedium.copy(
        lineHeight = TextUnit.Unspecified
    ),
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    containerColor: Color? = null,
    primaryTextColor: Color? = null,
    secondaryTextColor: Color? = null,
    radius: Int = RadiusFull,
    focusColor: Color? = null,
    error: String? = null,
    errorColor: Color? = null
    ) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isError = error != null

    val tokens = quimiaColorTokens()
    val actualContainerColor = containerColor ?: tokens.secondary
    val actualPrimaryColor = primaryTextColor ?: tokens.foregroundPrimary
    val actualSecondaryColor = secondaryTextColor ?: tokens.foregroundSubtle
    val actualFocusColor = focusColor ?: tokens.borderFocus
    val actualErrorColor = errorColor ?: tokens.error

    val shape = RoundedCornerShape(radius.dp)
    val resolvedTextStyle = textStyle.copy(
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )
    val primaryTextStyle = resolvedTextStyle.copy(
        color = actualPrimaryColor
    )
    val secondaryTextStyle = resolvedTextStyle.copy(
        color = actualSecondaryColor
    )
    val errorTextStyle = resolvedTextStyle.copy(
        color = actualErrorColor
    )
    val borderColor by animateColorAsState(
        when {
            isError -> actualErrorColor
            isFocused -> actualFocusColor
            else -> Color.Transparent
        }
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (label != null) {
            Text(
                text = label,
                style = primaryTextStyle
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            textStyle = primaryTextStyle,
            singleLine = true,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = actualContainerColor,
                    shape = shape
                )
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = shape
                )
                .padding(20.dp, 12.dp),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(text = placeholder, style = secondaryTextStyle)
                    }
                    innerTextField()
                }
                    if (value.isNotEmpty()) {
                        QuimiaIconButton(
                            modifier = Modifier.padding(start = 4.dp),
                            icon = Lucide.X,
                            onClick = { onValueChange("") },
                            iconColor = actualPrimaryColor,
                            containerColor = actualContainerColor,
                            iconSize = 16,
                            contentDescription = "Clear all",
                            spacing = 0,
                            radius = 0
                        )
                    }
                }
            }
        )

        if (isError) {
            Text(
                text = error,
                style = errorTextStyle
            )
        } else if (description != null) {
            Text(
                text = description,
                style = secondaryTextStyle
            )
        }
    }
}

@Preview
@Composable
private fun QuimiaInputPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = LightTokens.background,
                shape = RoundedCornerShape(RadiusSmall.dp)
            )
            .width(352.dp)
            .padding(20.dp, 37.dp)
    ) {
        QuimiaInput(
            value = "",
            onValueChange = {},
            label = "Label",
            placeholder = "Value",
            description = "Description"
        )
        QuimiaInput(
            value = "Value",
            onValueChange = {},
            label = "Label",
            placeholder = "Value",
            description = "Description"
        )
        QuimiaInput(
            value = "Value",
            onValueChange = {},
            label = "Label",
            placeholder = "Value",
            description = "Description",
            error = "Error Description"
        )
    }
}
