package card

import theme.quimiaSkeleton
import motion.quimiaAnimatedColor
import theme.QuimiaTextStyles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.RadiusLarge
import theme.quimiaColorTokens
import theme.quimiaHighContrastEnabled
import theme.QuimiaTheme

enum class QuimiaCardLayout {
    Default,
    Stacked,
}

enum class QuimiaCardTheme { Primary, Secondary }

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
    contentPadding: Dp = 16.dp,
    paddingValues: PaddingValues? = null,
    sectionSpacing: Dp = 24.dp,
    actionHeight: Dp = 42.dp,
    iconSize: Dp = 24.dp,
    titleTextStyle: TextStyle = QuimiaTextStyles.medium15,
    descriptionTextStyle: TextStyle = titleTextStyle,
    actionTextStyle: TextStyle = titleTextStyle,
    onPrimaryActionClick: () -> Unit = {},
    onSecondaryActionClick: () -> Unit = {},
    theme: QuimiaCardTheme = QuimiaCardTheme.Primary,
    outline: Boolean = false,
    endIcon: ImageVector? = Lucide.Circle,
    onEndIconClick: () -> Unit = {},
    containerColor: Color? = null,
    headerContent: (@Composable () -> Unit)? = null,
    contrastOutline: Boolean = true,
    outlineColor: Color? = null,
    outlineWidth: Dp = 3.dp,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val tokens = quimiaColorTokens()
    val hasActions = !primaryActionLabel.isNullOrBlank() || !secondaryActionLabel.isNullOrBlank()
    val background = quimiaAnimatedColor(containerColor ?: if (theme == QuimiaCardTheme.Primary) tokens.surfaceBase else tokens.secondary)
    val border = quimiaAnimatedColor(if (outline || (contrastOutline && quimiaHighContrastEnabled())) outlineColor ?: tokens.borderFocus else Color.Transparent)

    Column(
        modifier = modifier.quimiaSkeleton()
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusLarge.dp))
            .background(background)
            .border(outlineWidth, border, RoundedCornerShape(RadiusLarge.dp))
            .padding(paddingValues ?: PaddingValues(contentPadding)),
        verticalArrangement = if (layout == QuimiaCardLayout.Default) Arrangement.SpaceBetween else Arrangement.Top,
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
            if (headerContent != null) headerContent() else QuimiaCardHeader(
                icon = icon, title = title, description = description,
                showTitle = showTitle, showDescription = showDescription,
                titleTextStyle = titleTextStyle, descriptionTextStyle = descriptionTextStyle,
                iconSize = iconSize, endIcon = endIcon, onEndIconClick = onEndIconClick,
            )
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!secondaryActionLabel.isNullOrBlank()) {
                    QuimiaButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        text = secondaryActionLabel,
                        textStyle = actionTextStyle,
                        containerColor = if (theme == QuimiaCardTheme.Primary) tokens.secondary else tokens.surfaceBase,
                        textColor = tokens.foregroundPrimary,
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
                        textColor = tokens.foregroundPrimary,
                        iconSize = 20,
                        espacamento = 0,
                        onClick = onPrimaryActionClick,
                    )
                }
            }
        }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaCardPreview() {
    QuimiaTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            val cardVariants = listOf(
                QuimiaCardTheme.Primary to false,
                QuimiaCardTheme.Secondary to true,
            )
            if (maxWidth >= 600.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    cardVariants.forEach { (theme, outline) ->
                        QuimiaCardPreviewItem(theme, outline, Modifier.weight(1f))
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    cardVariants.forEach { (theme, outline) ->
                        QuimiaCardPreviewItem(theme, outline, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun QuimiaCardPreviewItem(
    theme: QuimiaCardTheme,
    outline: Boolean,
    modifier: Modifier = Modifier,
) {
    QuimiaCard(
        modifier = modifier.height(248.dp),
        title = "Title",
        description = "Description",
        icon = Lucide.Circle,
        primaryActionLabel = "Label",
        secondaryActionLabel = "Label",
        theme = theme,
        outline = outline,
    ) {
        Box(
            Modifier.fillMaxWidth().height(80.dp)
                .background(quimiaColorTokens().secondary, RoundedCornerShape(8.dp)),
        )
    }
}
