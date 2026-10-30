package buttons

import motion.quimiaPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import theme.quimiaSkeleton
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens
import theme.quimiaContrastOutline
import theme.quimiaHighContrastEnabled
import theme.QuimiaTheme
import theme.actionContentColor

enum class QuimiaIconButtonSize(
    val buttonSize: Dp,
    val iconSize: Dp
) {
    Icon(
        buttonSize = 16.dp,
        iconSize = 12.dp
    ),
    Small(
        buttonSize = 32.dp,
        iconSize = 16.dp
    ),
    Medium(
        buttonSize = 45.dp,
        iconSize = 20.dp
    ),
    Large(
        buttonSize = 56.dp,
        iconSize = 24.dp
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
    iconSize: Dp? = null,
    contentDescription: String? = null,
    iconPainter: Painter? = null,
    contrastOutline: Boolean = size != QuimiaIconButtonSize.Icon,
) {
    val tokens = quimiaColorTokens()

    val actualContainerColor = containerColor ?: tokens.primary
    val actualIconColor = actionContentColor(actualContainerColor, iconColor, tokens)

    val motionInteractions = remember { MutableInteractionSource() }
    val motionPressed by motionInteractions.collectIsPressedAsState()
    IconButton(
        interactionSource = motionInteractions,
        onClick = onClick,
        modifier = modifier.quimiaPress(motionPressed).size(size.buttonSize).quimiaSkeleton(size.buttonSize).quimiaContrastOutline(CircleShape, contrastOutline && quimiaHighContrastEnabled()),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = actualContainerColor,
            contentColor = actualIconColor
        )
    ) {
        if (iconPainter != null) Icon(
            painter = iconPainter, contentDescription = contentDescription,
            tint = actualIconColor, modifier = Modifier.size(iconSize ?: size.iconSize),
        ) else Icon(
            imageVector = icon, contentDescription = contentDescription,
            tint = actualIconColor, modifier = Modifier.size(iconSize ?: size.iconSize),
        )
    }
}

@preview.QuimiaDevicePreviews
@Composable
fun PreviewQuimiaIconButton() {
    QuimiaTheme {
        QuimiaIconButton(
            icon = Lucide.Circle,
            contentDescription = "Circle",
        )
    }
}
