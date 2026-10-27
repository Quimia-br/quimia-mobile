package checkBox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import theme.QuimiaTheme
import theme.quimiaColorTokens
import theme.quimiaHighContrastEnabled

@Composable
fun QuimiaCheckBox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    val shape = RoundedCornerShape(8.dp)
    val interaction = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        )
    } else {
        Modifier.semantics {
            this.toggleableState = ToggleableState(checked)
            if (!enabled) disabled()
        }
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .then(interaction)
            .background(
                color = if (checked) tokens.brandAccent.copy(alpha = if (enabled) 1f else 0.38f)
                    else Color.Transparent,
                shape = shape,
            )
            .then(
                if (!checked || quimiaHighContrastEnabled()) Modifier.border(
                    width = if (quimiaHighContrastEnabled()) 3.dp else 1.5.dp,
                    color = if (quimiaHighContrastEnabled()) tokens.borderFocus else if (enabled) tokens.borderPrimary else tokens.foregroundDisabled,
                    shape = shape,
                ) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Lucide.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (enabled) tokens.foregroundPrimary else tokens.foregroundDisabled,
            )
        }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaCheckBoxPreview() {
    QuimiaTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(44.dp),
        ) {
            QuimiaCheckBox(checked = false, onCheckedChange = {})
            QuimiaCheckBox(checked = true, onCheckedChange = {})
        }
    }
}
