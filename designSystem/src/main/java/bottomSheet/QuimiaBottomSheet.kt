package bottomSheet

import theme.QuimiaTextStyles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import dragger.QuimiaDragger
import listItem.QuimiaListItem
import theme.QuimiaTheme
import theme.QuimiaFontFamily
import theme.quimiaColorTokens

enum class QuimiaBottomSheetTheme { Primary, Secondary }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuimiaBottomSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    showDragHandle: Boolean = true,
    description: String? = null,
    showCloseButton: Boolean = true,
    theme: QuimiaBottomSheetTheme = QuimiaBottomSheetTheme.Primary,
    sheetMaxWidth: Dp = 326.dp,
    primaryActionLabel: String? = null,
    secondaryActionLabel: String? = null,
    primaryActionEnabled: Boolean = true,
    onPrimaryActionClick: () -> Unit = {},
    onSecondaryActionClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!isVisible) return

    val tokens = quimiaColorTokens()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = RoundedCornerShape(
            topStart = 40.dp,
            topEnd = 40.dp,
        ),
        containerColor = if (theme == QuimiaBottomSheetTheme.Primary) tokens.surfaceBase else tokens.secondary,
        contentColor = tokens.textPrimary,
        scrimColor = tokens.black.copy(alpha = 0.32f),
        dragHandle = null,
    ) {
        QuimiaBottomSheetContent(
            modifier = Modifier.fillMaxWidth(),
            title = title, description = description, theme = theme,
            showDragHandle = showDragHandle, showCloseButton = showCloseButton,
            onClose = onDismissRequest, primaryActionLabel = primaryActionLabel,
            secondaryActionLabel = secondaryActionLabel, primaryActionEnabled = primaryActionEnabled,
            onPrimaryActionClick = onPrimaryActionClick, onSecondaryActionClick = onSecondaryActionClick,
            content = content,
        )
    }
}

@Composable
fun QuimiaBottomSheetContent(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    theme: QuimiaBottomSheetTheme = QuimiaBottomSheetTheme.Primary,
    showDragHandle: Boolean = true,
    showCloseButton: Boolean = true,
    onClose: () -> Unit = {},
    primaryActionLabel: String? = null,
    secondaryActionLabel: String? = null,
    primaryActionEnabled: Boolean = true,
    onPrimaryActionClick: () -> Unit = {},
    onSecondaryActionClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = quimiaColorTokens()
    val secondary = if (theme == QuimiaBottomSheetTheme.Primary) tokens.secondary else tokens.surfaceBase
    Column(
        modifier = modifier.fillMaxWidth().height(547.dp).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showDragHandle) QuimiaDragger(color = secondary, height = 3.dp)
        if (!title.isNullOrBlank() || !description.isNullOrBlank() || showCloseButton) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!title.isNullOrBlank()) Text(text = title, color = tokens.textPrimary,
                        style = QuimiaTextStyles.medium24)
                    if (!description.isNullOrBlank()) Text(text = description, color = tokens.textSecondary,
                        style = QuimiaTextStyles.medium16)
                }
                if (showCloseButton) QuimiaIconButton(icon = Lucide.X, onClick = onClose,
                    size = QuimiaIconButtonSize.Small, containerColor = secondary,
                    contentDescription = "Fechar")
            }
        }
        Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), content = content)
        if (!primaryActionLabel.isNullOrBlank() || !secondaryActionLabel.isNullOrBlank()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val actionStyle = QuimiaTextStyles.medium15
                if (!secondaryActionLabel.isNullOrBlank()) QuimiaButton(
                    text = secondaryActionLabel, textStyle = actionStyle,
                    fontFamily = QuimiaFontFamily, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f).height(52.dp), iconSize = 20, espacamento = 0,
                    containerColor = secondary, textColor = tokens.foregroundPrimary,
                    onClick = onSecondaryActionClick)
                if (!primaryActionLabel.isNullOrBlank()) QuimiaButton(
                    text = primaryActionLabel, textStyle = actionStyle,
                    fontFamily = QuimiaFontFamily, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f).height(52.dp), iconSize = 20, espacamento = 0,
                    enabled = primaryActionEnabled, textColor = tokens.foregroundPrimary,
                    onClick = onPrimaryActionClick)
            }
        }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaBottomSheetPreview() {
    QuimiaTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            val themes = if (maxWidth >= 600.dp) QuimiaBottomSheetTheme.entries else listOf(QuimiaBottomSheetTheme.Primary)
            if (maxWidth >= 600.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    themes.forEach { theme ->
                        QuimiaBottomSheetPreviewItem(theme, Modifier.weight(1f))
                    }
                }
            } else {
                themes.forEach { theme ->
                    QuimiaBottomSheetPreviewItem(theme, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun QuimiaBottomSheetPreviewItem(
    theme: QuimiaBottomSheetTheme,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(547.dp),
        shape = RoundedCornerShape(40.dp),
        color = if (theme == QuimiaBottomSheetTheme.Primary) quimiaColorTokens().surfaceBase else quimiaColorTokens().secondary,
    ) {
        QuimiaBottomSheetContent(
            modifier = Modifier.fillMaxSize(),
            title = "Title",
            description = "Description",
            theme = theme,
            primaryActionLabel = "Label",
            secondaryActionLabel = "Label",
        ) {
            QuimiaListItem(
                icon = Lucide.Circle,
                title = "Componente de exemplo",
                subtitle = "Conteúdo dentro do BottomSheet",
            )
        }
    }
}
