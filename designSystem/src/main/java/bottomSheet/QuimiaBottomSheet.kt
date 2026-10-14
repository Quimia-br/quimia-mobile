package bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import theme.RadiusLarge
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens

/**
 * Modal bottom sheet styled with Quimia surface and text tokens.
 * It supports the standard Material drag gesture and dismisses through
 * [onDismissRequest] when the user swipes it down or taps outside it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuimiaBottomSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    showDragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!isVisible) return

    val tokens = quimiaColorTokens()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = RadiusLarge.dp,
            topEnd = RadiusLarge.dp,
        ),
        containerColor = tokens.surfaceBase,
        contentColor = tokens.textPrimary,
        scrimColor = tokens.black.copy(alpha = 0.32f),
        dragHandle = if (showDragHandle) {
            { BottomSheetDefaults.DragHandle(color = tokens.foregroundDisabled) }
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    color = tokens.textPrimary,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            content()
        }
    }
}

@Preview
@Composable
private fun QuimiaBottomSheetPreview() {
    QuimiaTheme {
        QuimiaBottomSheet(
            isVisible = true,
            onDismissRequest = {},
            title = "Escanear rótulo",
        ) {
            Text(
                text = "Conteúdo do modal",
                style = Typography.bodyMedium,
            )
        }
    }
}
