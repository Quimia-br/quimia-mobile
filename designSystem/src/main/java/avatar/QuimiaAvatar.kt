package avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.quimiaColorTokens

@Composable
fun QuimiaAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    photo: Painter? = null,
    initials: String = "",
    backgroundColor: Color = quimiaColorTokens().surfaceVariant,
    foregroundColor: Color = quimiaColorTokens().foregroundPrimary,
    borderColor: Color = quimiaColorTokens().borderPrimary,
    borderWidth: Dp = 1.dp,
    contentDescription: String? = null,
) {
    val tokens = quimiaColorTokens()
    val resolvedBackground = backgroundColor
    val resolvedForeground = foregroundColor
    val resolvedBorder = borderColor

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(resolvedBackground)
            .border(borderWidth, resolvedBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                painter = photo,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
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
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuimiaAvatarPreview() {
    val tokens = quimiaColorTokens()

    Box(
        modifier = Modifier.background(tokens.background)
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            QuimiaAvatar(
                size = 44.dp,
                initials = "PP",
                backgroundColor = tokens.surfaceVariant,
                foregroundColor = tokens.foregroundPrimary
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))

            QuimiaAvatar(
                size = 44.dp,
                photo = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_camera),
                backgroundColor = tokens.surfaceVariant,
                foregroundColor = tokens.foregroundPrimary
            )
        }
    }
}

