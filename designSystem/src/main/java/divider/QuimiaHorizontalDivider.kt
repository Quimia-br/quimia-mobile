package divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.quimiaColorTokens

@Composable
fun QuimiaHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = quimiaColorTokens().secondary
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

@Preview
@Composable
private fun QuimiaHorizontalDividerPreview() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(quimiaColorTokens().background)
    ) {
        QuimiaHorizontalDivider()
    }
}