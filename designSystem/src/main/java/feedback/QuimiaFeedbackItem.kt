package feedback

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import theme.RadiusFull
import theme.RadiusSmall
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun QuimiaFeedbackItem(
    isSuccess: Boolean,
    modifier: Modifier = Modifier,
    containerSize: Dp = 200.dp,
    iconSize: Dp = 40.dp,
    shape: Shape = RoundedCornerShape(RadiusFull.dp),
    iconSuccess: ImageVector = Lucide.Check,
    iconError: ImageVector = Lucide.Check,
    backgroundColor: Color? = null,
    iconSuccessColor: Color? = null,
    iconErrorColor: Color? = null,
    borderWidth: Dp = 3.dp,
    contentDescription: String? = null
) {
    val tokens = quimiaColorTokens()

    val icon = if (isSuccess) iconSuccess else iconError
    val resolvedBackgroundColor = backgroundColor ?: tokens.secondary

    val tint by animateColorAsState(
        targetValue = if (isSuccess) iconSuccessColor ?: tokens.foregroundPrimary else iconErrorColor ?: tokens.error,
        animationSpec = tween(durationMillis = 300),
        label = "IconTintAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSuccess) tokens.success else tokens.error,
        animationSpec = tween(durationMillis = 300),
        label = "BorderColorAnimation"
    )

    Box(
        modifier = modifier
            .size(containerSize)
            .clip(shape)
            .background(resolvedBackgroundColor)
            .border(borderWidth, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = tint
        )
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaFeedbackItemPreview() {
    QuimiaTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .clip(RoundedCornerShape(RadiusSmall.dp))
                .padding(20.dp)
        ) {
            QuimiaFeedbackItem(
                isSuccess = true
            )
            QuimiaFeedbackItem(
                isSuccess = false
            )
        }
    }
}
