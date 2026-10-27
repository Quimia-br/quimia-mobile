package chip

import theme.QuimiaTextStyles

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
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import theme.QuimiaTheme
import theme.RadiusFull
import theme.quimiaColorTokens
import theme.quimiaContrastOutline
import theme.quimiaHighContrastEnabled

@Composable
fun QuimiaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showSelectedIcon: Boolean = true,
    centerContent: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    val containerColor = when {
        !enabled -> tokens.surfaceDisabled
        selected -> tokens.primary
        else -> tokens.surfaceBase
    }
    val contentColor = when {
        !enabled -> tokens.foregroundDisabled
        selected && quimiaHighContrastEnabled() -> tokens.onPrimary
        else -> tokens.foregroundPrimary
    }

    Row(
        modifier = modifier
            .sizeIn(minHeight = 42.dp)
            .semantics { this.selected = selected }
            .clip(RoundedCornerShape(RadiusFull.dp))
            .background(containerColor)
            .quimiaContrastOutline(RoundedCornerShape(RadiusFull.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp, if (centerContent) Alignment.CenterHorizontally else Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = QuimiaTextStyles.medium15, color = contentColor, maxLines = 1)
        if (selected && showSelectedIcon) {
            Icon(
                imageVector = Lucide.X,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor,
            )
        }
    }
}
@preview.QuimiaDevicePreviews
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
