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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import shapeIcon.QuimiaShapeIcon
import shapeIcon.QuimiaShapeIconSize
import shapeIcon.QuimiaShapeIconTheme
import theme.RadiusFull
import theme.RadiusLarge
import theme.RadiusMedium
import theme.Typography
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
    titleTextStyle: TextStyle = Typography.titleMedium.copy(
        lineHeight = 19.sp, letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    descriptionTextStyle: TextStyle = Typography.titleMedium.copy(
        fontSize = 15.sp, lineHeight = 18.sp, letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    buttonTextStyle: TextStyle = Typography.titleMedium.copy(
        lineHeight = 19.sp, letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
fun QuimiaEmptyStatePreview() {
    QuimiaTheme {
        Column {
            QuimiaEmptyStateTheme.entries.forEach { theme ->
                QuimiaEmptyState(title = "Title", description = "Description",
                    buttonLabel = "Label", theme = theme, onButtonClick = {})
            }
        }
    }
}
