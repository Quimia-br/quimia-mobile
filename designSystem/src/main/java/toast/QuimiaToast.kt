package toast

import theme.QuimiaTextStyles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import shapeIcon.QuimiaShapeIcon
import shapeIcon.QuimiaShapeIconSize
import shapeIcon.QuimiaShapeIconTheme
import theme.QuimiaTheme
import theme.RadiusFull
import theme.quimiaColorTokens

enum class QuimiaToastType { Info, Success, Warning, Error }

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
    description: String? = null,
    statusIconContainerColor: Color? = null,
) {
    val tokens = quimiaColorTokens()
    val accent = when (type) {
        QuimiaToastType.Info -> tokens.foregroundPrimary
        QuimiaToastType.Success -> tokens.success
        QuimiaToastType.Warning -> tokens.warning
        QuimiaToastType.Error -> tokens.error
    }
    val iconTheme = when (type) {
        QuimiaToastType.Info -> QuimiaShapeIconTheme.Secondary
        QuimiaToastType.Success -> QuimiaShapeIconTheme.Success
        QuimiaToastType.Warning -> QuimiaShapeIconTheme.Warning
        QuimiaToastType.Error -> QuimiaShapeIconTheme.Critical
    }
    val statusIcon = leadingIcon ?: when (type) {
        QuimiaToastType.Info -> Lucide.Info
        QuimiaToastType.Success -> Lucide.Check
        QuimiaToastType.Warning -> Lucide.TriangleAlert
        QuimiaToastType.Error -> Lucide.X
    }

    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(RadiusFull.dp),
        color = containerColor ?: tokens.surfaceBase,
        contentColor = tokens.textPrimary,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuimiaShapeIcon(
                size = QuimiaShapeIconSize.Small.value,
                icon = statusIcon,
                theme = iconTheme,
                containerColor = statusIconContainerColor,
            )
            Column(
                modifier = Modifier.weight(1f).sizeIn(minHeight = 50.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = message,
                    color = tokens.foregroundPrimary,
                    style = QuimiaTextStyles.medium16,
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        color = tokens.foregroundSubtle,
                        style = QuimiaTextStyles.medium14,
                    )
                }
            }

            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(text = actionLabel, color = accent)
                }
            }

            if (onDismiss != null) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = "Fechar aviso",
                        tint = tokens.foregroundSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaToastPreview() {
    QuimiaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(50.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            listOf(QuimiaToastType.Info, QuimiaToastType.Error, QuimiaToastType.Success, QuimiaToastType.Warning).forEach { type ->
                QuimiaToast(message = "Title", description = "Description", type = type, onDismiss = {})
            }
        }
    }
}
