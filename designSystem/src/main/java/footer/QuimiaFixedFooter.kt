package footer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import theme.PaddingHuge
import theme.QuimiaTheme
import theme.quimiaColorTokens

@Composable
fun QuimiaFixedFooter(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    containerColor: Color = quimiaColorTokens().surfaceBase,
    contentPadding: PaddingValues = PaddingValues(PaddingHuge.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .padding(contentPadding),
        content = content
    )
}

@Preview
@Composable
private fun PreviewQuimiaFixedFooter() {
    QuimiaTheme {
        QuimiaFixedFooter(modifier = Modifier.width(262.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(53.dp)
                    .background(Color(0xFFFFCDF2))
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xFFFF00AE),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                            )
                        )
                    },
            )
        }
    }
}
