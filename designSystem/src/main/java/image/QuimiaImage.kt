package image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import theme.QuimiaTheme
import theme.RadiusMedium
import theme.quimiaColorTokens

/**
 * Quimia image surface with consistent clipping and a configurable empty state.
 * The caller owns image loading and supplies the resulting [Painter].
 */
@Composable
fun QuimiaImage(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    shape: Shape = RoundedCornerShape(RadiusMedium.dp),
    backgroundColor: Color? = null,
    placeholder: @Composable BoxScope.() -> Unit = {},
) {
    val tokens = quimiaColorTokens()

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor ?: tokens.surfaceDisabled),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = contentScale,
                alignment = alignment,
            )
        } else {
            placeholder()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuimiaImagePreview() {
    QuimiaTheme {
        QuimiaImage(
            painter = painterResource(android.R.drawable.ic_menu_camera),
            contentDescription = "Imagem de exemplo",
            modifier = Modifier.size(120.dp),
        )
    }
}
