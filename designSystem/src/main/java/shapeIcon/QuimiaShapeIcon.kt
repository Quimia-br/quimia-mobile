package shapeIcon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens


@Composable
fun QuimiaShapeIcon(
    size: Dp,
    icon: ImageVector,
) {
    val tokens = quimiaColorTokens()

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(tokens.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tokens.black,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuimiaShapeIconPreview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuimiaShapeIcon(
            size = 100.dp,
            icon = Lucide.Circle,
        )
        Spacer(modifier = Modifier.height(20.dp))
        QuimiaShapeIcon(
            size = 76.dp,
            icon = Lucide.Circle,
        )
        Spacer(modifier = Modifier.height(20.dp))
        QuimiaShapeIcon(
            size = 50.dp,
            icon = Lucide.Circle,
        )
    }

}
