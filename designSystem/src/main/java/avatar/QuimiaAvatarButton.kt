package avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.PaddingSmall
import theme.RadiusSmall
import theme.quimiaColorTokens

@Composable
fun QuimiaAvatarButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    size: Dp = 40.dp,
    photo: Painter? = null,
    initials: String = "",
    backgroundColor: Color? = null,
    foregroundColor: Color? = null,
    contentDescription: String? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        QuimiaAvatar(
            size = size,
            photo = photo,
            initials = initials,
            contentDescription = contentDescription,
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor
        )
    }
}

@Preview
@Composable
private fun QuimiaAvatarButtonPreview() {
    val tokens = quimiaColorTokens()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(64.dp)
            .height(162.dp)
            .clip(RoundedCornerShape(RadiusSmall.dp))
            .background(tokens.surfaceBackground)
            .padding(PaddingSmall.dp)
    ) {
        QuimiaAvatarButton(
            initials = "EP",
            backgroundColor = tokens.onPrimary,
            onClick = {}
        )

        QuimiaAvatarButton(
            photo = painterResource(id = android.R.drawable.ic_menu_camera),
            onClick = {}
        )

        QuimiaAvatarButton(
            initials = "GS",
            onClick = {}
        )
    }
}