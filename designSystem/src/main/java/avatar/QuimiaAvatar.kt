package avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.PaddingSmall
import theme.QuimiaTheme
import theme.RadiusSmall
import theme.quimiaColorTokens

@Composable
fun QuimiaAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 45.dp,
    photo: Painter? = null,
    initials: String = "",
    backgroundColor: Color? = null,
    foregroundColor: Color? = null,
    contentDescription: String? = null,
) {
    val tokens = quimiaColorTokens()
    val resolvedBackground = backgroundColor ?: tokens.surfaceDisabled
    val resolvedForeground = foregroundColor ?: tokens.foregroundPrimary

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(resolvedBackground),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                painter = photo,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            val label = initials
                .takeIf { it.isNotBlank() }
                ?.take(2)
                ?.uppercase()
                ?: "?"

            Text(
                text = label,
                color = resolvedForeground,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun QuimiaAvatarPreview() {
    QuimiaTheme {
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
            QuimiaAvatar(
                initials = "GM",
                backgroundColor = tokens.onPrimary
            )

            QuimiaAvatar(
                photo = painterResource(
                    id = android.R.drawable.ic_menu_camera
                )
            )

            QuimiaAvatar(
                initials = "GB"
            )
        }
    }
}

