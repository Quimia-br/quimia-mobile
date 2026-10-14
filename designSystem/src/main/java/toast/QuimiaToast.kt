package toast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import theme.QuimiaTheme
import theme.RadiusMedium
import theme.Typography
import theme.quimiaColorTokens

enum class QuimiaToastType { Info, Success, Warning, Error }

/** Visual content for a toast. The host controls placement, visibility, and duration. */
@Composable
fun QuimiaToast(
    message: String,
    modifier: Modifier = Modifier,
    type: QuimiaToastType = QuimiaToastType.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    containerColor: Color? = null,
    leadingIcon: ImageVector? = null,
) {
    val tokens = quimiaColorTokens()
    val accent = when (type) {
        QuimiaToastType.Info -> tokens.info
        QuimiaToastType.Success -> tokens.success
        QuimiaToastType.Warning -> tokens.warning
        QuimiaToastType.Error -> tokens.error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RadiusMedium.dp),
        color = containerColor ?: tokens.surfaceBase,
        contentColor = tokens.textPrimary,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent, CircleShape),
            )

            leadingIcon?.let { icon ->
                Icon(imageVector = icon, contentDescription = null, tint = accent)
            }

            Text(
                text = message,
                style = Typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )

            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(text = actionLabel, color = accent)
                }
            }

            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = "Fechar aviso",
                        tint = tokens.foregroundSecondary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuimiaToastPreview() {
    QuimiaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            QuimiaToast(message = "Produto salvo com sucesso", type = QuimiaToastType.Success, onDismiss = {})
            QuimiaToast(message = "Não foi possível salvar", type = QuimiaToastType.Error, actionLabel = "Tentar", onAction = {})
        }
    }
}
