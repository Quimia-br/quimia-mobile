package com.quimia.android.presentation.screens.rooms

import motion.quimiaContent
import theme.quimiaSkeleton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import buttons.*
import com.composables.icons.lucide.*
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import feedback.QuimiaFeedbackItem
import input.QuimiaInput
import listItem.QuimiaListItem
import nav.QuimiaNav
import progressBar.QuimiaProgressBar
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import title.QuimiaMainTitle
import toast.QuimiaToast
import toast.QuimiaToastType

@Composable
internal fun RoomForm(
    name: String, icon: RoomIcon?, selectedIds: List<String>, products: List<CatalogProduct>, error: String?,
    onNameChange: (String) -> Unit, onIconChange: (RoomIcon) -> Unit, onToggleProduct: (String) -> Unit,
    onClose: () -> Unit, onOpenCatalog: () -> Unit, onSubmit: () -> Unit,
    formTitle: String = "Cadastrar cômodo", submitLabel: String = "Criar cômodo", nameLimit: Int = 60,
    showIdentity: Boolean = true, canSubmit: Boolean? = null, showEmpty: Boolean = true,
    selectionHint: String = "Escolha os produtos que você usa para limpar esse cômodo",
    statusContent: (@Composable () -> Unit)? = null,
    formDescription: String = "Insira as informações do cômodo",
    enabled: Boolean = true,
    highlightName: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).quimiaContent()) {
                Spacer(Modifier.height(com.quimia.android.presentation.components.formTopSpace(62.dp)))
                QuimiaNav(showBackButton = true, onBackClick = onClose, onCloseClick = onClose)
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    QuimiaMainTitle(title = formTitle)
                    Spacer(Modifier.height(4.dp))
                    Text(formDescription, modifier = Modifier.quimiaSkeleton(8.dp), style = QuimiaTextStyles.medium16, color = tokens.textSecondary)
                }
                if (showIdentity) {
                Box(Modifier.fillMaxWidth().padding(24.dp)) {
                    QuimiaInput(value = name, onValueChange = { onNameChange(it.take(nameLimit)) }, label = "Nome", placeholder = "Sala de estar",
                        highlighted = highlightName && name.isNotBlank(), borderWidth = 3.dp, enabled = enabled,
                        trailingIconSize = 16.dp, trailingIconContentDescription = "Limpar nome do cômodo",
                        trailingIcon = if (name.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { onNameChange("") })
                }
                Text("Ícone", Modifier.padding(horizontal = 24.dp).quimiaSkeleton(8.dp), style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                Text("Escolha o ícone que melhor representa o cômodo", Modifier.padding(horizontal = 24.dp, vertical = 12.dp).quimiaSkeleton(8.dp),
                    style = QuimiaTextStyles.medium14, color = tokens.textSecondary)
                BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    val iconColumns = roomIconColumns(maxWidth.value)
                    Column {
                        RoomIcon.entries.chunked(iconColumns).forEachIndexed { index, row ->
                            Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = if (index == (RoomIcon.entries.size - 1) / iconColumns) 24.dp else 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                row.forEach { item ->
                                    QuimiaIconButton(icon = if (item == RoomIcon.Sofa) Lucide.Sofa else item.vector,
                                        iconPainter = item.drawable?.let { painterResource(it) }, iconSize = 20.dp,
                                        size = QuimiaIconButtonSize.Large,
                                        containerColor = if (icon == item) tokens.primary else tokens.secondary,
                                        enabled = enabled,
                                        contentDescription = "Ícone ${item.label}${if (icon == item) ", selecionado" else ""}",
                                        onClick = { onIconChange(item) })
                                }
                                repeat(iconColumns - row.size) { Spacer(Modifier.size(56.dp)) }
                            }
                        }
                    }
                }
                }
                Text("Produtos", Modifier.padding(horizontal = 24.dp).quimiaSkeleton(8.dp), style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                Text(selectionHint, Modifier.padding(horizontal = 24.dp, vertical = 16.dp).quimiaSkeleton(8.dp),
                    style = QuimiaTextStyles.medium14, color = tokens.textSecondary)
                statusContent?.invoke()
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val columns = roomGridColumns(maxWidth.value)
                    if (products.isEmpty() && showEmpty) {
                        states.QuimiaEmptyState(icon = Lucide.GalleryVerticalEnd, title = "Sua estante está vazia",
                            description = "Guarde produtos no Catálogo para usá-los neste cômodo.", buttonLabel = "Explorar catálogo",
                            onButtonClick = onOpenCatalog, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        Column {
                            products.chunked(columns).forEach { row ->
                                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    row.forEach { product ->
                                        RoomProductCard(product, Modifier.weight(1f), selectable = true, checked = product.id in selectedIds,
                                            enabled = enabled && (product.id in selectedIds || selectedIds.size < 20),
                                            onCheckedChange = { onToggleProduct(product.id) })
                                    }
                                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            com.quimia.android.presentation.components.FormFooter(bottomPadding = 16.dp, spacing = 8.dp) {
                if (error != null) QuimiaToast(message = error, type = QuimiaToastType.Error)
                QuimiaButton(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    text = submitLabel, textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0,
                    enabled = enabled && (canSubmit ?: (roomValidationError(name, icon, selectedIds, products.map { it.id }) == null)), onClick = onSubmit)
            }
        }
    }
}

@Composable
internal fun RoomDetails(room: QuimiaRoom, products: List<CatalogProduct>, onClose: () -> Unit, onProductActions: (String) -> Unit,
    apiAssessment: (@Composable () -> Unit)? = null, onAddProducts: (() -> Unit)? = null, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null,
    statusContent: (@Composable () -> Unit)? = null, enabled: Boolean = true, initialMenuVisible: Boolean = false) {
    val tokens = quimiaColorTokens()
    val preview = LocalInspectionMode.current
    val safety = if (preview || apiAssessment != null) room.safety else RoomSafety.NotEvaluated
    var noticeVisible by rememberSaveable(room.id) { mutableStateOf(true) }
    var menuVisible by rememberSaveable(room.id) { mutableStateOf(initialMenuVisible) }
    if (menuVisible) RoomActionsPanel(room.name, onDismiss = { menuVisible = false },
        onAdd = onAddProducts?.let { { menuVisible = false; it() } },
        onEdit = onEdit?.let { { menuVisible = false; it() } },
        onDelete = onDelete?.let { { menuVisible = false; it() } },
        previewBackground = { RoomDetails(room, products, {}, {}) })
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 840.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            val columns = roomGridColumns(maxWidth.value)
            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    QuimiaIconButton(icon = Lucide.ChevronLeft, contentDescription = "Voltar para cômodos", containerColor = tokens.secondary, enabled = enabled, onClick = onClose)
                    QuimiaIconButton(icon = Lucide.Menu, contentDescription = "Ações do cômodo", enabled = enabled,
                        containerColor = if (menuVisible) tokens.secondaryPressed else tokens.secondary, onClick = { menuVisible = true })
                }
                LazyColumn(modifier = Modifier.quimiaContent(), contentPadding = PaddingValues(bottom = 24.dp)) {
                    item {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            RoomShapeIcon(room.icon, 76.dp, critical = safety == RoomSafety.Critical, onBase = true, knownIcon = room.knownIcon)
                            Spacer(Modifier.height(if (safety == RoomSafety.Critical) 18.dp else 26.dp))
                            Text(room.name, Modifier.padding(24.dp).quimiaSkeleton(8.dp), style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
                            Spacer(Modifier.height(if (safety == RoomSafety.Critical) 18.dp else 26.dp))
                            val title = when (safety) {
                                RoomSafety.Critical -> "Crítico: nunca use esses produtos juntos"
                                RoomSafety.Safe -> "Seguro"
                                RoomSafety.NotEvaluated -> "Segurança não avaliada"
                            }
                            val description = when (safety) {
                                RoomSafety.Critical -> "A combinação dos produtos apresenta altos riscos à saúde"
                                RoomSafety.Safe -> "A combinação é segura"
                                RoomSafety.NotEvaluated -> "Não misture produtos. Siga as orientações dos rótulos."
                            }
                            if (apiAssessment != null) apiAssessment() else QuimiaListItem(title = title, subtitle = description, modifier = Modifier.padding(horizontal = 24.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = if (safety == RoomSafety.Critical) 16.dp else 24.dp),
                                horizontalSpacing = 20.dp, cornerRadius = 32.dp,
                                containerColor = if (safety == RoomSafety.Critical) tokens.error else tokens.surfaceBase,
                                titleColor = if (safety == RoomSafety.Critical) tokens.white else tokens.foregroundPrimary,
                                subtitleColor = if (safety == RoomSafety.Critical) tokens.white.copy(alpha = .8f) else tokens.foregroundSubtle,
                                subtitleMaxLines = 4,
                                leadingContent = { QuimiaShapeIcon(size = 50.dp,
                                    icon = when (safety) { RoomSafety.Critical -> Lucide.ShieldX; RoomSafety.Safe -> Lucide.Check; else -> Lucide.Info },
                                    containerColor = if (safety == RoomSafety.Safe) tokens.success else tokens.surfaceBase,
                                    iconColor = if (safety == RoomSafety.Critical) tokens.error else tokens.foregroundPrimary) })
                            statusContent?.invoke()
                            if (safety == RoomSafety.Critical && noticeVisible) Box(Modifier.fillMaxWidth().heightIn(min = 132.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                                QuimiaToast(message = "Alterar status da mistura", description = "Adicione ou remova produtos do cômodo para recalcular",
                                    leadingIcon = Lucide.Check, statusIconContainerColor = tokens.success,
                                    onDismiss = { noticeVisible = false })
                            } else Spacer(Modifier.height(24.dp))
                        }
                    }
                    if (products.isEmpty()) item {
                        QuimiaListItem(modifier = Modifier.padding(horizontal = 24.dp), title = "Nenhum produto neste cômodo", subtitle = "Os produtos da sua estante continuam salvos.")
                    } else items(products.chunked(columns)) { row ->
                        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            row.forEach { product -> RoomProductCard(product, Modifier.weight(1f), critical = safety == RoomSafety.Critical,
                                enabled = enabled,
                                previewQuantity = preview && safety == RoomSafety.Safe, onActions = { onProductActions(product.id) }) }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoomCreating(name: String) {
    val tokens = quimiaColorTokens()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 40.dp)) {
            Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Criando $name", style = QuimiaTextStyles.medium20, color = tokens.foregroundPrimary)
                Spacer(Modifier.height(20.dp))
                Text("O cômodo está sendo criado", style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle)
            }
            QuimiaProgressBar(progress = .15f)
        }
    }
}

@Composable
internal fun RoomCreationResult(success: Boolean, name: String, onClose: () -> Unit, onContinue: () -> Unit,
    errorMessage: String? = null, failureAction: String = "Voltar para cômodos") {
    val tokens = quimiaColorTokens()
    val preview = LocalInspectionMode.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            QuimiaNav(onCloseClick = onClose)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).quimiaContent(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    QuimiaFeedbackItem(isSuccess = success, iconError = Lucide.X, containerSize = 200.dp, iconSize = 41.311.dp)
                }
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (success) "Cômodo ${name.lowercase()} criado com sucesso" else "Não foi possível criar o cômodo.",
                        style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    if (!success) Text(errorMessage ?: if (preview) "Por favor verifique sua conexão e tente novamente." else "Não foi possível salvar o cômodo neste aparelho. Tente novamente.",
                        style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                if (success) Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuimiaListItem(title = "Segurança é o nosso pilar", containerColor = tokens.surfaceBase, cornerRadius = 24.dp, contentPadding = PaddingValues(16.dp), horizontalSpacing = 20.dp,
                        leadingContent = { QuimiaIconButton(icon = Lucide.Shield, containerColor = tokens.secondary) })
                    QuimiaListItem(title = "Organização personalizada para sua casa", containerColor = tokens.surfaceBase, cornerRadius = 24.dp, contentPadding = PaddingValues(16.dp), horizontalSpacing = 20.dp,
                        leadingContent = { QuimiaIconButton(icon = Lucide.Puzzle, containerColor = tokens.secondary) })
                }
            }
            QuimiaButton(modifier = Modifier.fillMaxWidth().padding(24.dp).height(43.dp), text = if (success) "Abrir cômodo" else failureAction,
                iconSize = 0, espacamento = 0, textStyle = QuimiaTextStyles.medium16, onClick = onContinue)
        }
    }
}
