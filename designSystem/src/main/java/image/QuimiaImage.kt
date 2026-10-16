package image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.composables.icons.lucide.CameraOff
import com.composables.icons.lucide.Lucide
import com.quimia.designsystem.R
import states.QuimiaEmptyState
import states.QuimiaEmptyStateTheme
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
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    shape: Shape? = null,
    backgroundColor: Color? = null,
    onConfigure: () -> Unit = {},
    placeholder: @Composable BoxScope.() -> Unit = {
        QuimiaEmptyState(
            modifier = Modifier.matchParentSize(),
            title = "Câmera desabilitada",
            description = "Permita o acesso à\ncâmera",
            buttonLabel = "Configurar",
            icon = Lucide.CameraOff,
            fillAvailableSpace = true,
            theme = QuimiaEmptyStateTheme.OnColor,
            onButtonClick = onConfigure,
        )
    },
) {
    val tokens = quimiaColorTokens()

    Box(
        modifier = modifier
            .size(width = 264.dp, height = 310.dp)
            .clip(shape ?: RoundedCornerShape(if (painter == null) 24.dp else 32.dp))
            .background(backgroundColor ?: if (painter == null) tokens.secondary else Color.Transparent),
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

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun QuimiaImagePreview() {
    QuimiaTheme {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            QuimiaImage(painter = painterResource(R.drawable.login_image), contentDescription = "Imagem de exemplo")
            QuimiaImage(painter = null, contentDescription = null, onConfigure = {})
        }
    }
}
