package chip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import theme.QuimiaTheme
import theme.RadiusFull
import theme.Typography
import theme.quimiaColorTokens

private val ChipLabelStyle = Typography.titleMedium.copy(
    fontSize = 15.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

/** Figma Chip: a white filter, or a primary filter with a trailing remove icon. */
@Composable
fun QuimiaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showSelectedIcon: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    val containerColor = when {
        !enabled -> tokens.surfaceDisabled
        selected -> tokens.primary
        else -> tokens.surfaceBase
    }
    val contentColor = if (enabled) tokens.foregroundPrimary else tokens.foregroundDisabled

    Row(
        modifier = modifier
            .sizeIn(minHeight = 42.dp)
            .semantics { this.selected = selected }
            .clip(RoundedCornerShape(RadiusFull.dp))
            .background(containerColor)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = ChipLabelStyle, color = contentColor, maxLines = 1)
        if (selected && showSelectedIcon) {
            Icon(
                imageVector = Lucide.X,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (enabled) tokens.black else tokens.foregroundDisabled,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun QuimiaChipPreview() {
    QuimiaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(20.dp),
        ) {
            QuimiaChip(label = "Label", selected = true, onClick = {})
            QuimiaChip(label = "Label", selected = false, onClick = {})
        }
    }
}
