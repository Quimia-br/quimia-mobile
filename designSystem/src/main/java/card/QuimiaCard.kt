package card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.RadiusLarge
import theme.Typography
import theme.quimiaColorTokens
import menuBar.QuimiaMenuBar

enum class QuimiaCardLayout {
    Default,
    Stacked,
}

@Composable
fun QuimiaCard(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    primaryActionLabel: String?,
    secondaryActionLabel: String?,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    showDescription: Boolean = true,
    layout: QuimiaCardLayout = QuimiaCardLayout.Default,
    contentPadding: Dp = 24.dp,
    paddingValues: PaddingValues? = null,
    sectionSpacing: Dp = 24.dp,
    actionHeight: Dp = 48.dp,
    iconSize: Dp = 28.dp,
    titleTextStyle: TextStyle = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
    descriptionTextStyle: TextStyle = Typography.bodyLarge,
    actionTextStyle: TextStyle = Typography.labelLarge,
    onPrimaryActionClick: () -> Unit = {},
    onSecondaryActionClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val tokens = quimiaColorTokens()
    val hasActions = !primaryActionLabel.isNullOrBlank() || !secondaryActionLabel.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusLarge.dp))
            .background(tokens.surfaceBase)
            .padding(paddingValues ?: PaddingValues(contentPadding)),
    ) {
        if (layout == QuimiaCardLayout.Stacked) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = tokens.foregroundDisabled,
                    modifier = Modifier.size(iconSize),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showTitle) {
                Text(
                    text = title,
                    color = tokens.textPrimary,
                    style = titleTextStyle,
                )
            }

            if (showDescription && description != null) {
                if (showTitle) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Text(
                    text = description,
                    color = tokens.textSecondary,
                    style = descriptionTextStyle,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tokens.foregroundDisabled,
                        modifier = Modifier.size(iconSize),
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(verticalArrangement = Arrangement.Center) {
                    if (showTitle) {
                        Text(
                            text = title,
                            color = tokens.textPrimary,
                            style = titleTextStyle,
                        )
                    }

                    if (showDescription && description != null) {
                        if (showTitle) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = description,
                            color = tokens.textSecondary,
                            style = descriptionTextStyle,
                        )
                    }
                }
            }
        }

        if (layout == QuimiaCardLayout.Default) {
            Spacer(modifier = Modifier.height(sectionSpacing))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )

        if (hasActions) {
            Spacer(modifier = Modifier.height(sectionSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!secondaryActionLabel.isNullOrBlank()) {
                    QuimiaButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        text = secondaryActionLabel,
                        textStyle = actionTextStyle,
                        containerColor = tokens.secondary,
                        textColor = tokens.textPrimary,
                        iconSize = 20,
                        espacamento = 0,
                        onClick = onSecondaryActionClick,
                    )
                }

                if (!primaryActionLabel.isNullOrBlank()) {
                    QuimiaButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        text = primaryActionLabel,
                        textStyle = actionTextStyle,
                        containerColor = tokens.primary,
                        textColor = tokens.textPrimary,
                        iconSize = 20,
                        espacamento = 0,
                        onClick = onPrimaryActionClick,
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF7F6F5,
    widthDp = 392,
)
@Composable
private fun QuimiaCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        QuimiaCard(
            title = "Title",
            description = "Description",
            icon = Lucide.Circle,
            primaryActionLabel = "Label",
            secondaryActionLabel = "Label",
        ) {
           QuimiaMenuBar {  }
        }
    }
}
