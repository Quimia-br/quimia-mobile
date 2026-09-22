package buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens

enum class QuimiaIconButtonSize(
    val buttonSize: Dp,
    val iconSize: Dp
) {
    Icon(
        buttonSize = 16.dp,
        iconSize = 16.dp
    ),
    Small(
        buttonSize = 32.dp,
        iconSize = 16.dp
    ),
    Medium(
        buttonSize = 45.dp,
        iconSize = 16.dp
    ),
    Large(
        buttonSize = 56.dp,
        iconSize = 16.dp
    )
}

@Composable
fun QuimiaIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit = {},
    containerColor: Color? = null,
    iconColor: Color? = null,
    size: QuimiaIconButtonSize = QuimiaIconButtonSize.Medium,
    contentDescription: String? = null
) {
    val tokens = quimiaColorTokens()

    val actualContainerColor = containerColor ?: tokens.primary
    val actualIconColor = iconColor ?: tokens.textPrimary

    IconButton(
        onClick = onClick,
        modifier = modifier.size(size.buttonSize),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = actualContainerColor,
            contentColor = actualIconColor
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size.iconSize)
        )
    }
}

@Preview
@Composable
fun PreviewQuimiaIconButton() {
    QuimiaIconButton(
        icon = Lucide.Circle,
        contentDescription = "Circle"
    )
}
