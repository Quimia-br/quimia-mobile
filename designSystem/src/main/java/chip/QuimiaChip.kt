package chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import theme.QuimiaTheme
import theme.RadiusFull
import theme.Typography
import theme.quimiaColorTokens

/** A compact, selectable filter chip that follows the active Quimia color tokens. */
@Composable
fun QuimiaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showSelectedIcon: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    val containerColor = when {
        !enabled -> tokens.surfaceDisabled
        selected -> tokens.primary
        else -> tokens.secondary
    }
    val contentColor = if (enabled) tokens.textPrimary else tokens.foregroundDisabled

    Surface(
        onClick = onClick,
        modifier = modifier
            .sizeIn(minHeight = 36.dp)
            .semantics { this.selected = selected },
        enabled = enabled,
        shape = RoundedCornerShape(RadiusFull.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected && showSelectedIcon) {
                Icon(
                    imageVector = Lucide.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor,
                )
            }
            Text(text = label, style = Typography.labelMedium, color = contentColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuimiaChipPreview() {
    QuimiaTheme {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            QuimiaChip(label = "Todos", selected = true, onClick = {}, showSelectedIcon = true)
            QuimiaChip(label = "Favoritos", selected = false, onClick = {})
        }
    }
}
