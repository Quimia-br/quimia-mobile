package buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Astroid
import com.composables.icons.lucide.Lucide
import theme.PaddingSmall
import theme.QuimiaTheme
import theme.RadiusSmall
import theme.quimiaColorTokens

@Composable
fun QuimiaFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    containerColor: Color? = null,
    iconColor: Color? = null,
    contentDescription: String? = null
) {
    val tokens = quimiaColorTokens()

    val actualContainerColor = containerColor ?: tokens.primary
    val actualIconColor = iconColor ?: tokens.textPrimary

    IconButton(
        onClick = onClick,
        modifier = modifier.size(64.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = actualContainerColor,
            contentColor = actualIconColor
        )
    ) {
        Icon(
            imageVector = Lucide.Astroid,
            tint = actualIconColor,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview
@Composable
private fun PreviewQuimiaFAB() {
    QuimiaTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(RadiusSmall.dp))
                .background(quimiaColorTokens().surfaceBackground)
                .padding(PaddingSmall.dp),
            contentAlignment = Alignment.Center
        ) {
            QuimiaFAB(onClick = {})
        }
    }
}
