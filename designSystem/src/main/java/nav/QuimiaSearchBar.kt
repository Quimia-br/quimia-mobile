package nav

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import buttons.QuimiaIconButton
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import theme.PaddingMedium
import theme.PaddingSmall
import theme.QuimiaColorTokens
import theme.RadiusFull
import theme.Typography
import theme.quimiaColorTokens

enum class QuimiaSearchBarVariant { Primary, Secondary }

private data class SearchBarColors(
    val container: Color,
    val sticky: Color,
    val icon: Color
)

private fun resolveColors(
    variant: QuimiaSearchBarVariant,
    tokens: QuimiaColorTokens
): SearchBarColors = when (variant) {
    QuimiaSearchBarVariant.Primary -> SearchBarColors(
        container = tokens.surfaceBase,
        sticky = tokens.secondary,
        icon = tokens.foregroundSecondary
    )
    QuimiaSearchBarVariant.Secondary -> SearchBarColors(
        container = tokens.secondary,
        sticky = tokens.onPrimary,
        icon = tokens.foregroundPrimary
    )
}

@Composable
fun QuimiaSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    placeholder: String? = null,
    icon: ImageVector? = null,
    iconSize: Dp = 20.dp,
    iconSpacing: Dp = 6.dp,
    searchIconSize: Dp = 24.dp,
    iconDescription: String? = null,
    variant: QuimiaSearchBarVariant = QuimiaSearchBarVariant.Primary,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions: KeyboardActions = KeyboardActions(
        onSearch = { onSearch?.invoke() }
    ),
    radius: Dp = RadiusFull.dp,
    horizontalPadding: Dp = PaddingMedium.dp,
    verticalPadding: Dp = 14.dp,
    spacedBy: Dp = 20.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = Typography.titleMedium.copy(
        lineHeight = TextUnit.Unspecified
    ),
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    enabled: Boolean = true
) {
    val tokens = quimiaColorTokens()
    val colors = resolveColors(variant, tokens)

    val shape = RoundedCornerShape(radius)

    val resolvedTextStyle = textStyle.copy(
        color = tokens.textPrimary,
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacedBy),
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.container,
                shape = shape
            )
            .padding(horizontalPadding, verticalPadding),
    ) {
        Icon(
            imageVector = Lucide.Search,
            contentDescription = null,
            tint = tokens.foregroundPrimary,
            modifier = Modifier.size(searchIconSize)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            textStyle = resolvedTextStyle,
            singleLine = true,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .height(32.dp)
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = resolvedTextStyle,
                            color = tokens.textSecondary
                        )
                    }
                    innerTextField()
                }
            }
        )

        icon?.let {
            QuimiaIconButton(
                icon = icon,
                onClick = { onSearch?.invoke() },
                containerColor = colors.sticky,
                iconColor = colors.icon,
                iconSize = iconSize,
                contentDescription = iconDescription,
                padding = iconSpacing
            )
        }
    }
}

@Preview
@Composable
private fun PreviewQuimiaSearchBar() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = quimiaColorTokens().surfaceBackground,
                shape = RoundedCornerShape(PaddingSmall.dp)
            )
            .width(315.dp)
            .height(176.dp)
            .padding(21.dp)
    ) {
        QuimiaSearchBar(
            value = "",
            onValueChange = {},
            placeholder = "Pesquisar",
            icon = Lucide.Circle,
            iconDescription = "Botao de pesquisa"
        )
        QuimiaSearchBar(
            value = "",
            onValueChange = {},
            placeholder = "Pesquisar",
            icon = Lucide.Circle,
            iconDescription = "Botao de pesquisa",
            variant = QuimiaSearchBarVariant.Secondary
        )
    }
}