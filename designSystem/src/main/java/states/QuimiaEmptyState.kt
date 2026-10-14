package states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import shapeIcon.QuimiaShapeIcon
import shapeIcon.QuimiaShapeIconSize
import theme.RadiusFull
import theme.RadiusLarge
import theme.RadiusMedium
import theme.Typography
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun QuimiaEmptyState(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    buttonLabel: String? = null,
    icon: ImageVector = Lucide.Circle,
    iconSize: Dp = QuimiaShapeIconSize.Medium.value,
    contentSpacing: Dp = 32.dp,
    buttonSpacing: Dp = 36.dp,
    buttonModifier: Modifier = Modifier,
    titleTextStyle: TextStyle = Typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
    descriptionTextStyle: TextStyle = Typography.titleLarge.copy(fontWeight = FontWeight.Medium),
    buttonTextStyle: TextStyle = Typography.labelLarge,
    fillAvailableSpace: Boolean = true,
    onButtonClick: () -> Unit = {}
) {
    val tokens = quimiaColorTokens()

    Column(
        modifier = modifier
            .then(if (fillAvailableSpace) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        QuimiaShapeIcon(
            size = iconSize,
            icon = icon
        )

        Spacer(modifier = Modifier.height(contentSpacing))

        title?.let {
            Text(
                text = it,
                color = tokens.foregroundPrimary,
                textAlign = TextAlign.Center,
                style = titleTextStyle
            )
        }

        description?.let {
            Text(
                text = it,
                color = tokens.foregroundSubtle,
                textAlign = TextAlign.Center,
                style = descriptionTextStyle
            )
        }

        if (!buttonLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(buttonSpacing))
            QuimiaButton(
                text = buttonLabel,
                textStyle = buttonTextStyle,
                onClick = onButtonClick,
                containerColor = tokens.secondary,
                textColor = tokens.textPrimary,
                modifier = buttonModifier
                    .widthIn(max = 158.dp)
                    .height(43.dp),
                iconSize = 20,
                curvaCirculo = RadiusFull,
                espacamento = 0
            )
        }
    }
}

@Preview
@Composable
fun QuimiaEmptyStatePreview() {
    QuimiaTheme {
        QuimiaEmptyState(
            title = "Title",
            description = "Description",
            buttonLabel = "Label",
            onButtonClick = {}
        )
    }
}
