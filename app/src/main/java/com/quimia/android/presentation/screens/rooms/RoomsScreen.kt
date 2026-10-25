package com.quimia.android.presentation.screens.rooms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.*
import card.QuimiaCard
import chip.QuimiaChip
import com.composables.icons.lucide.*
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import com.quimia.android.presentation.screens.shelf.ShelfActionsPanel
import com.quimia.android.presentation.screens.shelf.ShelfProductInformation
import header.QuimiaSearchBar
import kotlinx.coroutines.yield
import kotlinx.coroutines.CancellationException
import menuBar.*
import states.QuimiaEmptyState
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import title.QuimiaSectionTitle
import java.util.UUID

enum class RoomPage { List, Create, Detail, Saving, Success, Error }

@Composable
fun RoomsScreen(
    rooms: List<QuimiaRoom>, onRoomsChange: (List<QuimiaRoom>) -> Unit,
    savedProductIds: List<String>, onMenuItemSelected: (Int) -> Unit = {},
    onAiClick: () -> Unit = {}, onOpenCatalog: () -> Unit = {},
    initialProductId: String? = null, onInitialProductConsumed: () -> Unit = {},
    initialPage: RoomPage = RoomPage.List, initialRoomId: String? = null,
    catalog: List<CatalogProduct> = sampleCatalogProducts,
    previewActionProductId: String? = null,
    initialDraftName: String = "", initialDraftIcon: RoomIcon? = null,
    initialDraftProductIds: List<String> = emptyList(),
) {
    val tokens = quimiaColorTokens()
    val preview = LocalInspectionMode.current
    var page by rememberSaveable { mutableStateOf(if (initialProductId != null) RoomPage.Create else initialPage) }
    var selectedRoomId by rememberSaveable { mutableStateOf(initialRoomId) }
    var draftName by rememberSaveable { mutableStateOf(initialDraftName) }
    var draftIcon by rememberSaveable { mutableStateOf(initialDraftIcon) }
    var draftProductIds by rememberSaveable { mutableStateOf((listOfNotNull(initialProductId) + initialDraftProductIds).distinct().filter { it in savedProductIds }) }
    var draftId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var validation by rememberSaveable { mutableStateOf<String?>(null) }
    var actionProductId by rememberSaveable { mutableStateOf(previewActionProductId) }
    var informationProductId by rememberSaveable { mutableStateOf<String?>(null) }
    val availableProducts = remember(savedProductIds, catalog) {
        val indexed = catalog.associateBy { it.id }
        savedProductIds.distinct().mapNotNull(indexed::get)
    }
    val selectedRoom = rooms.firstOrNull { it.id == selectedRoomId }
    LaunchedEffect(Unit) { if (initialProductId != null) onInitialProductConsumed() }
    LaunchedEffect(page) {
        if (page == RoomPage.Saving && !preview) {
            yield()
            val error = roomValidationError(draftName, draftIcon, draftProductIds, availableProducts.map { it.id })
            if (error != null) { validation = error; page = RoomPage.Create }
            else try {
                val room = QuimiaRoom(draftId, draftName.trim(), requireNotNull(draftIcon), draftProductIds.distinct())
                onRoomsChange(rooms.upsertRoom(room))
                selectedRoomId = room.id
                page = RoomPage.Success
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                page = RoomPage.Error
            }
        }
    }
    fun startCreate() {
        draftName = ""; draftIcon = null; draftProductIds = emptyList()
        draftId = UUID.randomUUID().toString(); validation = null; page = RoomPage.Create
    }
    BackHandler(enabled = page != RoomPage.List) { page = if (page == RoomPage.Error) RoomPage.Create else RoomPage.List }
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground)) {
        when (page) {
            RoomPage.List -> RoomList(rooms, onOpen = { selectedRoomId = it; page = RoomPage.Detail },
                onCreate = ::startCreate, onMenuItemSelected = onMenuItemSelected, onAiClick = onAiClick)
            RoomPage.Create -> RoomForm(
                name = draftName, icon = draftIcon, selectedIds = draftProductIds, products = availableProducts,
                error = validation, onNameChange = { draftName = it; validation = null },
                onIconChange = { draftIcon = it; validation = null },
                onToggleProduct = { id -> draftProductIds = if (id in draftProductIds) draftProductIds - id else draftProductIds + id; validation = null },
                onClose = { page = RoomPage.List }, onOpenCatalog = onOpenCatalog,
                onSubmit = {
                    validation = roomValidationError(draftName, draftIcon, draftProductIds, availableProducts.map { it.id })
                    if (validation == null) page = RoomPage.Saving
                },
            )
            RoomPage.Detail -> if (selectedRoom != null) RoomDetails(
                room = selectedRoom, products = roomProducts(selectedRoom, catalog),
                onClose = { page = RoomPage.List }, onProductActions = { actionProductId = it },
            ) else RoomList(rooms, { selectedRoomId = it; page = RoomPage.Detail }, ::startCreate, onMenuItemSelected, onAiClick)
            RoomPage.Saving -> RoomCreating(draftName.ifBlank { "Banheiro" })
            RoomPage.Success, RoomPage.Error -> RoomCreationResult(
                success = page == RoomPage.Success, name = selectedRoom?.name ?: draftName.ifBlank { "banheiro" },
                onClose = { page = RoomPage.List }, onContinue = { page = if (page == RoomPage.Success) RoomPage.Detail else RoomPage.List },
            )
        }
    }
    catalog.firstOrNull { it.id == actionProductId }?.let { product ->
        ShelfActionsPanel(product = product, onDismiss = { actionProductId = null }, showMixtures = false,
            onMixtures = {}, onInformation = { informationProductId = product.id; actionProductId = null },
            onRemove = {
                selectedRoom?.let { onRoomsChange(rooms.upsertRoom(it.withoutProduct(product.id))) }
                actionProductId = null
            }, previewBackground = {
                selectedRoom?.let { RoomDetails(it, roomProducts(it, catalog), {}, {}) }
            })
    }
    catalog.firstOrNull { it.id == informationProductId }?.let {
        ShelfProductInformation(it, onDismiss = { informationProductId = null })
    }
}

@Composable
private fun RoomList(
    rooms: List<QuimiaRoom>, onOpen: (String) -> Unit, onCreate: () -> Unit,
    onMenuItemSelected: (Int) -> Unit, onAiClick: () -> Unit,
) {
    val tokens = quimiaColorTokens()
    var query by rememberSaveable { mutableStateOf("") }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf(RoomSort.Oldest) }
    val visibleRooms = remember(rooms, query, sort) { filterRooms(rooms, query, sort) }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(276.dp).background(Brush.verticalGradient(
            listOf(tokens.brandSecondary.copy(alpha = .86f), tokens.surfaceBackground.copy(alpha = .72f), tokens.surfaceBackground))))
        Column(Modifier.align(Alignment.TopCenter).widthIn(max = 840.dp).fillMaxSize().statusBarsPadding()) {
            Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            QuimiaSectionTitle(title = "Cômodos", titleSize = 24.sp, textStyle = QuimiaTextStyles.medium24,
                horizontalPadding = 32.dp, verticalPadding = 32.dp)
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                QuimiaSearchBar(value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f).height(58.dp),
                    placeholder = "Pesquisar", icon = Lucide.Mic, iconDescription = "Pesquisar por voz", searchIconSize = 24.dp,
                    horizontalPadding = 16.dp, verticalPadding = 12.dp, spacedBy = 10.dp, textStyle = QuimiaTextStyles.medium16)
                QuimiaIconButton(icon = Lucide.Settings2, containerColor = if (filtersVisible) tokens.secondaryPressed else tokens.surfaceBase,
                    contentDescription = "Mostrar ou ocultar filtros", onClick = { filtersVisible = !filtersVisible })
                QuimiaIconButton(icon = Lucide.Plus, containerColor = tokens.surfaceBase, contentDescription = "Cadastrar cômodo", onClick = onCreate)
            }
            if (filtersVisible) Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoomSort.entries.forEach { item -> QuimiaChip(label = item.label, modifier = Modifier.weight(1f), centerContent = true,
                    selected = item == sort, onClick = { sort = item }) }
            }
            BoxWithConstraints(Modifier.weight(1f)) {
                val columns = when { maxWidth >= 700.dp -> 4; maxWidth >= 500.dp -> 3; else -> 2 }
                if (visibleRooms.isEmpty()) Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    QuimiaEmptyState(fillAvailableSpace = false, icon = Lucide.Bath,
                        title = if (query.isBlank()) "Seus cômodos ficam aqui" else "Nenhum cômodo encontrado",
                        description = if (query.isBlank()) "Cadastre um cômodo e organize os produtos da sua estante." else "Experimente pesquisar outro nome.")
                    QuimiaButton(text = "Cadastrar cômodo", iconSize = 0, espacamento = 0, onClick = onCreate)
                } else LazyVerticalGrid(columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp,
                        bottom = QuimiaMenuBarHeight + QuimiaMenuBarBottomSpacing + QuimiaFABSize + 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    items(visibleRooms, key = { it.id }) { room ->
                        QuimiaCard(title = room.name, showDescription = false, endIcon = null, primaryActionLabel = "Ver", secondaryActionLabel = null,
                            modifier = Modifier.heightIn(min = 248.dp), sectionSpacing = 28.dp, contentPadding = 16.dp,
                            headerContent = { Text(room.name, style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onPrimaryActionClick = { onOpen(room.id) }) {
                            RoomShapeIcon(room.icon, size = 100.dp, modifier = Modifier.fillMaxWidth(), critical = room.safety == RoomSafety.Critical)
                        }
                    }
                }
            }
        }
        QuimiaMenuBarBackdrop(Modifier.align(Alignment.BottomCenter))
        QuimiaMenuBar(modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 840.dp).fillMaxWidth()
            .navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = QuimiaMenuBarBottomSpacing),
            selectedIndex = 4, onItemSelected = onMenuItemSelected, fabSpacing = 24.dp,
            fab = { QuimiaFAB(onClick = onAiClick, contentDescription = "Abrir assistente de IA") })
    }
}
