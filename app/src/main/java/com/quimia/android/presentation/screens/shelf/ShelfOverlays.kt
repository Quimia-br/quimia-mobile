package com.quimia.android.presentation.screens.shelf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import card.QuimiaCard
import com.composables.icons.lucide.Atom
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.R
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import com.quimia.android.presentation.screens.catalog.CatalogDescriptionCard
import com.quimia.android.presentation.screens.catalog.CatalogModalScrimColor
import com.quimia.android.presentation.system.HideSystemNavigationBar
import image.QuimiaImage
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTheme
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

internal val ShelfRemovalColor = Color(0xFFFF2B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfFloatingPanel(onDismiss: () -> Unit, bottomMargin: Dp = 26.dp, content: @Composable ColumnScope.() -> Unit) {
    val tokens = quimiaColorTokens()
    if (LocalInspectionMode.current) {
        ShelfOverlayPreview(Alignment.BottomCenter) {
            Surface(
                modifier = Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomMargin)
                    .widthIn(max = 560.dp).fillMaxWidth(),
                color = tokens.surfaceBase, shape = RoundedCornerShape(40.dp),
            ) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(32.dp), content = content)
            }
        }
        return
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = bottomMargin),
        sheetMaxWidth = 560.dp, containerColor = tokens.surfaceBase, shape = RoundedCornerShape(40.dp),
        scrimColor = CatalogModalScrimColor, dragHandle = null,
    ) {
        HideSystemNavigationBar()
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(32.dp), content = content)
    }
}

@Composable
internal fun ShelfActionsPanel(
    product: CatalogProduct, onDismiss: () -> Unit,
    onRemove: () -> Unit, onMixtures: () -> Unit, onInformation: () -> Unit,
) {
    val tokens = quimiaColorTokens()
    ShelfFloatingPanel(onDismiss, bottomMargin = 17.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(product.name, Modifier.weight(1f).padding(end = 20.dp), style = ShelfBodyStyle.copy(fontSize = 20.sp, lineHeight = 24.sp),
                color = tokens.foregroundPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            ShelfCloseButton(onDismiss)
        }
        Spacer(Modifier.height(24.dp))
        QuimiaListItem(
            title = "Remover produto", onClick = onRemove, contentPadding = PaddingValues(12.dp), horizontalSpacing = 20.dp,
            leadingContent = { QuimiaShapeIcon(size = 50.dp, icon = Lucide.Minus, containerColor = ShelfRemovalColor, iconColor = tokens.white) },
        )
        Spacer(Modifier.height(20.dp))
        QuimiaListItem(
            title = "Ver misturas", onClick = onMixtures, contentPadding = PaddingValues(12.dp), horizontalSpacing = 20.dp,
            leadingContent = { QuimiaShapeIcon(size = 50.dp, icon = Lucide.Atom, containerColor = tokens.surfaceBase) },
        )
        Spacer(Modifier.height(20.dp))
        QuimiaListItem(
            title = "Ver informações", onClick = onInformation, contentPadding = PaddingValues(12.dp), horizontalSpacing = 20.dp,
            leadingContent = { QuimiaShapeIcon(size = 50.dp, icon = Lucide.Info, containerColor = tokens.surfaceBase) },
        )
    }
}

@Composable
internal fun ShelfProductInformation(product: CatalogProduct, onDismiss: () -> Unit) {
    val tokens = quimiaColorTokens()
    val clipboard = LocalClipboardManager.current
    ShelfFloatingPanel(onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
                Text(product.category, style = ShelfBodyStyle.copy(fontSize = 16.sp, lineHeight = 19.sp), color = tokens.textSecondary)
            }
            ShelfCloseButton(onDismiss)
        }
        Spacer(Modifier.height(34.dp))
        Box(Modifier.align(Alignment.CenterHorizontally).size(width = 147.dp, height = 172.dp).clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center) {
            QuimiaImage(
                painter = painterResource(if (product.id.startsWith("squeeze")) R.drawable.shelf_mr_musculo_figma else product.detailImageRes),
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize().then(if (product.id.startsWith("squeeze")) Modifier.graphicsLayer {
                    scaleX = -1.564f
                    translationX = -size.width * .0079f
                    translationY = size.height * .0008f
                } else Modifier), backgroundColor = tokens.surfaceBase,
                contentScale = if (product.id.startsWith("squeeze")) ContentScale.FillBounds else ContentScale.Fit,
            )
        }
        Spacer(Modifier.height(20.dp))
        CatalogDescriptionCard(description = product.description)
        if (product.barcode.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            QuimiaListItem(
                title = "Código de barras", subtitle = product.barcode,
                iconBackgroundColor = tokens.surfaceBase,
                trailingContent = {
                    QuimiaIconButton(icon = Lucide.Copy, containerColor = tokens.surfaceBase,
                        contentDescription = "Copiar código de barras", onClick = { clipboard.setText(AnnotatedString(product.barcode)) })
                },
                onClick = { clipboard.setText(AnnotatedString(product.barcode)) },
            )
        }
    }
}

@Composable
internal fun ShelfRemoveConfirmation(product: CatalogProduct, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ShelfConfirmation(
        title = "Tem certeza que quer remover\n${product.name}?",
        description = "Todas as misturas com esse produto serão apagadas",
        destructive = true, action = "Remover", onDismiss = onDismiss, onConfirm = onConfirm,
    )
}

@Composable
internal fun ShelfNoRoomConfirmation(onDismiss: () -> Unit, onCreate: () -> Unit) {
    ShelfConfirmation(
        title = "Esse produto ainda não pertence a nenhum cômodo", description = null,
        destructive = false, action = "Criar", onDismiss = onDismiss, onConfirm = onCreate,
    )
}

@Composable
private fun ShelfConfirmation(
    title: String, description: String?, destructive: Boolean, action: String,
    onDismiss: () -> Unit, onConfirm: () -> Unit,
) {
    val tokens = quimiaColorTokens()
    val confirmation: @Composable () -> Unit = {
        Surface(Modifier.widthIn(max = 297.dp).fillMaxWidth(), shape = RoundedCornerShape(40.dp), color = tokens.surfaceBase) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                QuimiaShapeIcon(
                    size = 76.dp, icon = if (destructive) Lucide.TriangleAlert else Lucide.Info,
                    containerColor = if (destructive) ShelfRemovalColor else tokens.surfaceBackground,
                    iconColor = if (destructive) tokens.white else tokens.foregroundPrimary,
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(title, Modifier.fillMaxWidth(), style = ShelfBodyStyle.copy(fontSize = 16.sp, lineHeight = 19.sp),
                        color = tokens.foregroundPrimary, textAlign = TextAlign.Center)
                    description?.let {
                        Text(it, Modifier.fillMaxWidth(), style = ShelfBodyStyle.copy(fontSize = 12.sp, lineHeight = 14.sp),
                            color = tokens.foregroundSubtle, textAlign = TextAlign.Center)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuimiaButton(
                        modifier = Modifier.weight(1f).height(43.dp), text = "Cancelar", iconSize = 0, espacamento = 0,
                        textStyle = ShelfBodyStyle.copy(fontSize = 16.sp), containerColor = tokens.secondary,
                        textColor = tokens.foregroundPrimary, onClick = onDismiss,
                    )
                    QuimiaButton(
                        modifier = Modifier.weight(1f).height(43.dp), text = action, iconSize = 0, espacamento = 0,
                        textStyle = ShelfBodyStyle.copy(fontSize = 16.sp),
                        containerColor = if (destructive) ShelfRemovalColor else tokens.primary,
                        textColor = if (destructive) tokens.white else tokens.foregroundPrimary, onClick = onConfirm,
                    )
                }
            }
        }
    }
    if (LocalInspectionMode.current) {
        ShelfOverlayPreview(Alignment.Center, confirmation)
    } else {
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            HideSystemNavigationBar()
            val window = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect { window?.setDimAmount(0.08f) }
            confirmation()
        }
    }
}

@Composable
private fun ShelfOverlayPreview(alignment: Alignment, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        ShelfScreen(sampleCatalogProducts.take(3).map { it.id }, {})
        Box(Modifier.fillMaxSize().background(CatalogModalScrimColor), contentAlignment = alignment) {
            content()
        }
    }
}

@Composable
private fun ShelfCloseButton(onDismiss: () -> Unit) {
    val tokens = quimiaColorTokens()
    QuimiaIconButton(
        icon = Lucide.X, size = QuimiaIconButtonSize.Medium, containerColor = tokens.secondary,
        iconColor = tokens.foregroundPrimary, contentDescription = "Fechar", onClick = onDismiss,
    )
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Estante · menu de ações", group = "Figma", widthDp = 360, heightDp = 805)
@Composable
private fun ActionsPreview() {
    QuimiaTheme(false) { ShelfActionsPanel(sampleCatalogProducts.first(), {}, {}, {}, {}) }
}
@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Estante · informações", group = "Figma", widthDp = 360, heightDp = 805)
@Composable
private fun InformationPreview() {
    QuimiaTheme(false) { ShelfProductInformation(sampleCatalogProducts.first(), {}) }
}
@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Estante · remover", group = "Figma", widthDp = 360, heightDp = 805)
@Composable
private fun RemovePreview() {
    QuimiaTheme(false) { ShelfRemoveConfirmation(sampleCatalogProducts.first(), {}, {}) }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Estante · sem cômodo", group = "Figma", widthDp = 360, heightDp = 805)
@Composable
private fun NoRoomPreview() {
    QuimiaTheme(false) { ShelfNoRoomConfirmation({}, {}) }
}
