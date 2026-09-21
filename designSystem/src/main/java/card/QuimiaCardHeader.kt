package card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import avatar.QuimiaAvatar
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.Typography
import theme.quimiaColorTokens

@Composable
fun QuimiaCardHeader(
    modifier: Modifier = Modifier,
    avatar: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    title: String? = null,
    description: String? = null,
    showTitle: Boolean = true,
    showDescription: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    val hasLeadingContent = avatar != null || icon != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.surfaceBase)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (avatar != null) {
            avatar()
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.foregroundDisabled,
                modifier = Modifier.size(28.dp),
            )
        }

        if (hasLeadingContent) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            if (showTitle && title != null) {
                Text(
                    text = title,
                    color = tokens.textPrimary,
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
            }

            if (showDescription && description != null) {
                if (showTitle && title != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = description,
                    color = tokens.textSecondary,
                    style = Typography.bodyLarge,
                )
            }
        }
    }
}

@Preview
@Composable
private fun QuimiaCardHeaderPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        QuimiaCardHeader(
            icon = Lucide.Circle,
            title = "Title",
            description = "Description",
        )

        QuimiaCardHeader(
            avatar = {
                QuimiaAvatar(
                    size = 56.dp,
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
