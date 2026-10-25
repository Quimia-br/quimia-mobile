package states

import theme.QuimiaTextStyles

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import shapeIcon.QuimiaShapeIcon
import shapeIcon.QuimiaShapeIconSize
import shapeIcon.QuimiaShapeIconTheme
import theme.RadiusFull
import theme.quimiaColorTokens
import theme.QuimiaTheme

enum class QuimiaEmptyStateTheme { Secondary, OnColor }

@Composable
fun QuimiaEmptyState(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    buttonLabel: String? = null,
    icon: ImageVector = Lucide.Circle,
    iconSize: Dp = QuimiaShapeIconSize.Medium.value,
    contentSpacing: Dp = 24.dp,
    buttonSpacing: Dp = 24.dp,
    buttonModifier: Modifier = Modifier,
    titleTextStyle: TextStyle = QuimiaTextStyles.medium16,
    descriptionTextStyle: TextStyle = QuimiaTextStyles.medium15,
    buttonTextStyle: TextStyle = QuimiaTextStyles.medium16,
    fillAvailableSpace: Boolean = false,
    theme: QuimiaEmptyStateTheme = QuimiaEmptyStateTheme.Secondary,
    onButtonClick: () -> Unit = {}
) {
    val tokens = quimiaColorTokens()

    Column(
        modifier = modifier
            .then(if (fillAvailableSpace) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        QuimiaShapeIcon(
            size = iconSize,
            icon = icon,
            theme = if (theme == QuimiaEmptyStateTheme.OnColor) QuimiaShapeIconTheme.Primary
                else QuimiaShapeIconTheme.Secondary,
        )

        if (!title.isNullOrBlank() || !description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(contentSpacing))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                if (!title.isNullOrBlank()) {
                    Text(text = title, color = tokens.foregroundPrimary,
                        textAlign = TextAlign.Center, style = titleTextStyle)
                }
                if (!description.isNullOrBlank()) {
                    Text(text = description, color = tokens.foregroundSubtle,
                        textAlign = TextAlign.Center, style = descriptionTextStyle)
                }
            }
        }

        if (!buttonLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(buttonSpacing))
            QuimiaButton(
                text = buttonLabel,
                textStyle = buttonTextStyle,
                onClick = onButtonClick,
                containerColor = if (theme == QuimiaEmptyStateTheme.OnColor) tokens.surfaceBase else tokens.secondary,
                textColor = tokens.textPrimary,
                modifier = buttonModifier
                    .widthIn(max = 158.dp)
                    .height(50.dp),
                iconSize = 20,
                curvaCirculo = RadiusFull,
                espacamento = 0
            )
        }
    }
}

@Preview(name = "Celular compacto", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 320, heightDp = 720)
@Composable
private fun QuimiaEmptyStateCompactPhonePreview() {
    QuimiaEmptyStatePreviewContent()
}

@Preview(name = "Celular", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 393, heightDp = 852)
@Composable
private fun QuimiaEmptyStatePhonePreview() {
    QuimiaEmptyStatePreviewContent()
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 800, heightDp = 1280)
@Composable
private fun QuimiaEmptyStateTabletPreview() {
    QuimiaEmptyStatePreviewContent()
}

@Composable
private fun QuimiaEmptyStatePreviewContent() {
    QuimiaTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QuimiaEmptyState(
                title = "Title",
                description = "Description",
                buttonLabel = "Label",
                theme = QuimiaEmptyStateTheme.Secondary,
            )
            QuimiaEmptyState(
                title = "Title",
                description = "Description",
                buttonLabel = "Label",
                theme = QuimiaEmptyStateTheme.OnColor,
            )
        }
    }
}
