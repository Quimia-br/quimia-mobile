package shortCut

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun QuimiaTag(
    size: Dp,
    icon: ImageVector,
) {
    val tokens = quimiaColorTokens()

    Row(
        modifier = Modifier
            .width(size * 2.8f)
            .height(size)
            .background(
                color = tokens.brandAccent,
                shape = RoundedCornerShape(percent = 50),
            )
            .padding(horizontal = size * 0.48f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(size * 0.20f),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tokens.white,
            modifier = Modifier.size(size * 0.50f),
        )
        Text(
            text = "Tag",
            color = tokens.white,
            fontSize = (size.value * 0.50f).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (size.value * 0.50f).sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuimiaTagPreview() {
    QuimiaTheme {
            QuimiaTag(
                size = 50.dp,
                icon = Lucide.Circle,
            )
    }
}
