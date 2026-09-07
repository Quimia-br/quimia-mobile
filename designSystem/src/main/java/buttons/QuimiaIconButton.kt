package buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.PaddingMedium
import theme.RadiusFull
import theme.quimiaColorTokens

@Composable
fun QuimiaIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit = {},
    containerColor: Color? = null,
    iconColor: Color? = null,
    iconSize: Int,
    contentDescription: String? = null,
    spacing: Int = PaddingMedium,
    radius: Int = RadiusFull
) {
    val tokens = quimiaColorTokens()
    val actualContainer = containerColor ?: tokens.primary
    val actualIconColor = iconColor ?: tokens.textPrimary
    val buttonSize = (iconSize + (spacing * 2)).dp

    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        colors = ButtonDefaults.buttonColors(
            containerColor = actualContainer
        ),
        shape = RoundedCornerShape(radius.dp),
        contentPadding = PaddingValues(spacing.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = actualIconColor,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Preview
@Composable
fun PreviewQuimiaIconButton() {
    QuimiaIconButton(
        icon = Lucide.Circle,
        modifier = Modifier.width(50.dp),
        iconSize = 20,
        contentDescription = "Circle"
    )
}
