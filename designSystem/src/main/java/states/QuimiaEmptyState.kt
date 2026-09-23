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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onButtonClick: () -> Unit = {}
) {
    val tokens = quimiaColorTokens()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        QuimiaShapeIcon(
            size = QuimiaShapeIconSize.Medium.value,
            icon = Lucide.Circle
        )

        Spacer(modifier = Modifier.height(32.dp))

        title?.let {
            Text(
                text = it,
                color = tokens.foregroundPrimary,
                style = Typography.headlineSmall.copy(fontWeight = FontWeight.Medium)
            )
        }

        description?.let {
            Text(
                text = it,
                color = tokens.foregroundSubtle,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Medium)
            )
        }

        if (!buttonLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(36.dp))
            QuimiaButton(
                text = buttonLabel,
                onClick = onButtonClick,
                containerColor = tokens.secondary,
                textColor = tokens.textPrimary,
                modifier = Modifier
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
