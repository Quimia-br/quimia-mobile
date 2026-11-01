package com.quimia.android.presentation.screens.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bottomSheet.QuimiaBottomSheet
import bottomSheet.QuimiaBottomSheetTheme
import buttons.QuimiaIconButton
import com.composables.icons.lucide.*
import com.quimia.android.presentation.screens.catalog.CatalogModalScrimColor
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.presentation.screens.shelf.ShelfConfirmation
import com.quimia.android.presentation.screens.shelf.ShelfFloatingPanel
import com.quimia.android.presentation.system.HideSystemNavigationBar
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
internal fun RoomActionsPanel(name: String, onDismiss: () -> Unit, onAdd: (() -> Unit)?,
    onEdit: (() -> Unit)?, onDelete: (() -> Unit)?, previewBackground: (@Composable () -> Unit)? = null) {
    val tokens = quimiaColorTokens()
    ShelfFloatingPanel(onDismiss, bottomMargin = 17.dp, previewBackground = previewBackground) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Editar $name", Modifier.weight(1f).padding(end = 20.dp), style = QuimiaTextStyles.medium20,
                color = tokens.foregroundPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            QuimiaIconButton(icon = Lucide.X, contentDescription = "Fechar ações do cômodo", containerColor = tokens.secondary, onClick = onDismiss)
        }
        Spacer(Modifier.height(24.dp))
        listOf(Triple("Adicionar produto", Lucide.Plus, onAdd), Triple("Editar cômodo", Lucide.Pencil, onEdit),
            Triple("Excluir cômodo", Lucide.Trash, onDelete)).forEachIndexed { index, (label, icon, callback) ->
            if (callback != null) {
                if (index > 0) Spacer(Modifier.height(10.dp))
                QuimiaListItem(title = label, containerColor = tokens.surfaceBackground,
                    contentPadding = PaddingValues(12.dp), horizontalSpacing = 20.dp, onClick = callback,
                    leadingContent = { QuimiaShapeIcon(size = 50.dp, icon = icon,
                        containerColor = if (index == 2) tokens.error else tokens.surfaceBase,
                        iconColor = if (index == 2) tokens.white else tokens.foregroundPrimary) })
            }
        }
    }
}

@Composable
internal fun RoomDeleteConfirmation(name: String, busy: Boolean, onDismiss: () -> Unit,
    onConfirm: () -> Unit, statusContent: @Composable () -> Unit = {}) {
    ShelfConfirmation(title = "Tem certeza que quer excluir o cômodo $name?",
        description = "Essa ação não poderá ser desfeita", destructive = true,
        action = if (busy) "Excluindo…" else "Excluir", busy = busy, onDismiss = onDismiss,
        onConfirm = onConfirm, statusContent = statusContent)
}

@Composable
internal fun RoomAddProductsPanel(products: List<CatalogProduct>, selected: List<String>, busy: Boolean,
    loading: Boolean, ready: Boolean, existingCount: Int, onToggle: (String) -> Unit,
    onDismiss: () -> Unit, onSubmit: () -> Unit, onOpenCatalog: () -> Unit,
    emptyShelf: Boolean = false,
    statusContent: @Composable () -> Unit = {}) {
    val tokens = quimiaColorTokens()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        QuimiaBottomSheet(isVisible = true, onDismissRequest = { if (!busy) onDismiss() },
            title = "Adicionar produto", titleTextStyle = QuimiaTextStyles.medium20, closeButtonColor = tokens.secondary, showDragHandle = false,
            theme = QuimiaBottomSheetTheme.Secondary, sheetMaxWidth = com.quimia.android.presentation.components.quimiaOverlayWidth(560.dp, 840.dp),
            modifier = Modifier.statusBarsPadding().padding(horizontal = 17.dp).padding(bottom = 17.dp),
            shape = RoundedCornerShape(40.dp), scrimColor = CatalogModalScrimColor,
            contentHeight = (maxHeight * .84f).coerceAtMost(850.dp),
            primaryActionLabel = if (busy) "Adicionando…" else "Adicionar${if (selected.isNotEmpty()) " (${selected.size})" else ""}",
            primaryActionEnabled = ready && !loading && !busy && selected.isNotEmpty() && existingCount + selected.size <= 20,
            onPrimaryActionClick = onSubmit) {
            HideSystemNavigationBar()
            Spacer(Modifier.height(14.dp))
            statusContent()
            if (products.isEmpty() && ready && !loading) {
                states.QuimiaEmptyState(icon = Lucide.GalleryVerticalEnd,
                    title = if (emptyShelf) "Sua estante está vazia" else "Nenhum produto disponível",
                    description = if (emptyShelf) "Guarde produtos no Catálogo para adicioná-los ao cômodo."
                        else "Os produtos da sua estante já estão neste cômodo. Guarde outros no Catálogo.",
                    buttonLabel = "Explorar catálogo", onButtonClick = onOpenCatalog)
            }
            products.forEach { product ->
                RoomProductCard(product, Modifier.fillMaxWidth(), selectable = true, checked = product.id in selected,
                    enabled = !busy && !loading && (product.id in selected || existingCount + selected.size < 20),
                    onCheckedChange = { onToggle(product.id) })
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}
