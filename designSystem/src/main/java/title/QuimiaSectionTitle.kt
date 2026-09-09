package title

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaIconButton
import com.composables.icons.lucide.BotOff
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.PaddingSmall
import theme.RadiusSmall
import theme.Typography
import theme.quimiaColorTokens

@Composable
fun QuimiaSectionTitle(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    titleSize: TextUnit = 24.sp,
    descriptionSize: TextUnit = 16.sp,
    textStyle: TextStyle = Typography.titleMedium.copy(
        lineHeight = TextUnit.Unspecified
    ),
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    textPrimary: Color? = null,
    textSecondary: Color? = null,
    horizontalPadding: Dp = 32.dp,
    verticalPadding: Dp = PaddingSmall.dp,
    content: (@Composable () -> Unit)? = null,
) {
    val tokens = quimiaColorTokens()
    val actualTextPrimary = textPrimary ?: tokens.textPrimary
    val actualTextSecondary = textSecondary ?: tokens.textSecondary

    val resolvedTextStyle = textStyle.copy(
        fontFamily = fontFamily ?: textStyle.fontFamily,
        fontWeight = fontWeight ?: textStyle.fontWeight
    )


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                maxLines = 1,
                color = actualTextPrimary,
                style = resolvedTextStyle.copy(fontSize = titleSize)
            )
            if (description != null) {
                Text(
                    text = description,
                    maxLines = 1,
                    color = actualTextSecondary,
                    style = resolvedTextStyle.copy(fontSize = descriptionSize)
                )
            }
        }
        content?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun QuimiaSectionTitlePreview() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = quimiaColorTokens().surfaceBackground,
                shape = RoundedCornerShape(RadiusSmall.dp)
            )
            .width(400.dp)
            .height(116.dp)
            .padding(20.dp)
    ) {
        QuimiaSectionTitle(title = "Title", description = "Description") {
            QuimiaIconButton(
                icon = Lucide.BotOff,
                onClick = {},
                contentDescription = null,
                containerColor = quimiaColorTokens().secondary,
                iconColor = quimiaColorTokens().foregroundSecondary,
                iconSize = 17.dp,
                padding = 8.dp,
                width = 2.dp
            )
            QuimiaIconButton(
                icon = Lucide.Circle,
                onClick = {},
                contentDescription = null,
                containerColor = quimiaColorTokens().secondary,
                iconColor = quimiaColorTokens().foregroundSecondary,
                iconSize = 17.dp,
                padding = 8.dp,
                width = 2.dp
            )
            QuimiaIconButton(
                icon = Lucide.Circle,
                onClick = {},
                contentDescription = null,
                containerColor = quimiaColorTokens().secondary,
                iconColor = quimiaColorTokens().foregroundSecondary,
                iconSize = 17.dp,
                padding = 8.dp,
                width = 2.dp
            )
        }
    }
}