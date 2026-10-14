package avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.QuimiaTheme
import theme.RadiusSmall
import theme.quimiaColorTokens

data class QuimiaAvatarGroupItem(
    val initials: String = "",
    val photo: Painter? = null,
    val backgroundColor: Color? = null,
    val foregroundColor: Color? = null,
    val contentDescription: String? = null
)

@Composable
fun QuimiaAvatarGroup(
    avatars: List<QuimiaAvatarGroupItem>,
    modifier: Modifier = Modifier,
    size: Dp = 45.dp,
    overlap: Dp = size / 1.5f
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-overlap)
    ) {
        avatars.reversed().forEach { avatar ->
            val avatarModifier = if (avatar.contentDescription != null) {
                Modifier.clearAndSetSemantics {
                    contentDescription = avatar.contentDescription
                }
            } else {
                Modifier
            }

            QuimiaAvatar(
                modifier = avatarModifier,
                size = size,
                photo = avatar.photo,
                initials = avatar.initials,
                backgroundColor = avatar.backgroundColor,
                foregroundColor = avatar.foregroundColor,
                contentDescription = avatar.contentDescription
            )
        }
    }
}

@Preview
@Composable
private fun PreviewQuimiaAvatarGroup() {
    QuimiaTheme {
        val tokens = quimiaColorTokens()
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(RadiusSmall.dp))
                .background(tokens.surfaceBackground)
                .padding(16.dp)
        ) {
            QuimiaAvatarGroup(
                avatars = listOf(
                    QuimiaAvatarGroupItem(
                        initials = "GS",
                        backgroundColor = tokens.surfaceBase
                    ),
                    QuimiaAvatarGroupItem(
                        initials = "EP",
                        backgroundColor = tokens.secondary
                    )
                )
            )
        }
    }
}
