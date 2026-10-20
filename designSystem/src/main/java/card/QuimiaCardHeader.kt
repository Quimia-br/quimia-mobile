package card

import theme.QuimiaTextStyles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import avatar.QuimiaAvatar
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun QuimiaCardHeader(
    modifier: Modifier = Modifier,
    avatar: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    title: String? = null,
    description: String? = null,
    showTitle: Boolean = true,
    showDescription: Boolean = true,
    showStartIcon: Boolean = true,
    endIcon: ImageVector? = Lucide.Circle,
    onEndIconClick: () -> Unit = {},
    endIconContentDescription: String? = null,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    titleTextStyle: TextStyle = QuimiaTextStyles.medium15,
    descriptionTextStyle: TextStyle = titleTextStyle,
) {
    val tokens = quimiaColorTokens()

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showStartIcon && avatar != null) {
            avatar()
        } else if (showStartIcon && icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.foregroundPrimary,
                modifier = Modifier.size(iconSize),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            if (showTitle && title != null) {
                Text(
                    text = title,
                    color = tokens.foregroundPrimary,
                    style = titleTextStyle,
                )
            }

            if (showDescription && description != null) {
                if (showTitle && title != null) {
                    Spacer(modifier = Modifier.height(7.dp))
                }
                Text(
                    text = description,
                    color = tokens.foregroundSubtle,
                    style = descriptionTextStyle,
                )
            }
        }
        if (endIcon != null) {
            QuimiaIconButton(icon = endIcon, size = QuimiaIconButtonSize.Small,
                onClick = onEndIconClick, contentDescription = endIconContentDescription)
        }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaCardHeaderPreview() {
    QuimiaTheme {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        QuimiaCardHeader(
            modifier = Modifier.fillMaxWidth(),
            icon = Lucide.Circle,
            title = "Title",
            description = "Description",
        )

        QuimiaCardHeader(
            modifier = Modifier.fillMaxWidth(),
            avatar = {
                QuimiaAvatar(
                    size = 39.dp,
                    initials = "PP",
                    backgroundColor = quimiaColorTokens().surfaceBase,
                    foregroundColor = quimiaColorTokens().foregroundPrimary,
                )
            },
            title = "Title",
            description = "Description",
        )
        }
    }
}
