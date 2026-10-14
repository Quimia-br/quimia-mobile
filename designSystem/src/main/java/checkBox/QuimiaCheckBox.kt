package checkBox

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import theme.QuimiaTheme
import theme.quimiaColorTokens

/** Controlled checkbox using Quimia colors; state is owned by the caller. */
@Composable
fun QuimiaCheckBox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = tokens.primary,
            uncheckedColor = tokens.foregroundDisabled,
            checkmarkColor = tokens.textPrimary,
            disabledCheckedColor = tokens.primaryDisabled,
            disabledUncheckedColor = tokens.surfaceDisabled,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun QuimiaCheckBoxPreview() {
    QuimiaTheme {
        Row {
            QuimiaCheckBox(checked = false, onCheckedChange = {})
            QuimiaCheckBox(checked = true, onCheckedChange = {})
            QuimiaCheckBox(checked = true, onCheckedChange = null, enabled = false)
        }
    }
}
