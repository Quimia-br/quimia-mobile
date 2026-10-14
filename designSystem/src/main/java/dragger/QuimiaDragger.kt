package dragger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.PaddingMedium
import theme.PaddingSmall
import theme.QuimiaTheme
import theme.RadiusSmall
import theme.quimiaColorTokens

@Composable
fun QuimiaDragger(
    modifier: Modifier = Modifier,
    color: Color = quimiaColorTokens().secondary,
    width: Dp = 91.dp,
    height: Dp = 4.dp,
    verticalPadding: Dp = PaddingSmall.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Spacer(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(CircleShape)
                .background(color = color)
        )
    }
}

@Preview
@Composable
private fun PreviewQuimiaDragger() {
    QuimiaTheme {
        val tokens = quimiaColorTokens()

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(294.dp)
                .height(106.dp)
                .clip(RoundedCornerShape(RadiusSmall.dp))
                .background(color = tokens.surfaceBackground)
                .padding(PaddingMedium.dp)
        ) {
            QuimiaDragger()
            QuimiaDragger(color = tokens.surfaceBase)
        }
    }
}
