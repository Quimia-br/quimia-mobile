package bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import dragger.QuimiaDragger
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens

enum class QuimiaBottomSheetTheme { Primary, Secondary }

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

/** The same sheet body is available to Compose Preview without opening a dialog. */
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
                        style = Typography.titleLarge.copy(fontSize = 24.sp, lineHeight = 29.sp,
                            letterSpacing = 0.sp, platformStyle = PlatformTextStyle(includeFontPadding = false)))
                    if (!description.isNullOrBlank()) Text(text = description, color = tokens.textSecondary,
                        style = Typography.titleMedium.copy(lineHeight = 19.sp, letterSpacing = 0.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)))
                }
                if (showCloseButton) QuimiaIconButton(icon = Lucide.X, onClick = onClose,
                    size = QuimiaIconButtonSize.Small, containerColor = secondary,
                    contentDescription = "Fechar")
            }
        }
        Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), content = content)
        if (!primaryActionLabel.isNullOrBlank() || !secondaryActionLabel.isNullOrBlank()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val actionStyle = Typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 18.sp,
                    letterSpacing = 0.sp, platformStyle = PlatformTextStyle(includeFontPadding = false))
                if (!secondaryActionLabel.isNullOrBlank()) QuimiaButton(
                    text = secondaryActionLabel, textStyle = actionStyle,
                    modifier = Modifier.weight(1f).height(52.dp), iconSize = 20, espacamento = 0,
                    containerColor = secondary, onClick = onSecondaryActionClick)
                if (!primaryActionLabel.isNullOrBlank()) QuimiaButton(
                    text = primaryActionLabel, textStyle = actionStyle,
                    modifier = Modifier.weight(1f).height(52.dp), iconSize = 20, espacamento = 0,
                    enabled = primaryActionEnabled, onClick = onPrimaryActionClick)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6, widthDp = 702)
@Composable
private fun QuimiaBottomSheetPreview() {
    QuimiaTheme {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuimiaBottomSheetTheme.entries.forEach { theme ->
                Surface(modifier = Modifier.width(326.dp).height(547.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = if (theme == QuimiaBottomSheetTheme.Primary) quimiaColorTokens().surfaceBase else quimiaColorTokens().secondary) {
                    QuimiaBottomSheetContent(modifier = Modifier.fillMaxSize(), title = "Title",
                        description = "Description", theme = theme,
                        primaryActionLabel = "Label", secondaryActionLabel = "Label") {
                        Text("Conteúdo", style = Typography.bodyMedium)
                    }
                }
            }
        }
    }
}
