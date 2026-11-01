package com.quimia.android.presentation.screens.rooms

import androidx.activity.compose.BackHandler
import com.composables.icons.lucide.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import buttons.QuimiaButton
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.rooms.*
import com.quimia.android.presentation.screens.catalog.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.shelf.ShelfActionsPanel
import com.quimia.android.presentation.screens.shelf.ShelfProductInformation
import com.quimia.android.presentation.screens.shelf.ShelfRemovalColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nav.QuimiaNav
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType

@Composable
internal fun RoomsRoute(userId: String, onMenuItemSelected: (Int) -> Unit, onAiClick: () -> Unit,
    onOpenCatalog: () -> Unit, onSessionExpired: () -> Unit, initialProductId: String? = null,
    initialRoomId: String? = null, onInitialConsumed: () -> Unit = {}) {
    val context = LocalContext.current.applicationContext
    var query by rememberSaveable(userId) { mutableStateOf("") }
    var draftName by rememberSaveable(userId) { mutableStateOf("") }
    var draftIcon by rememberSaveable(userId) { mutableStateOf<RoomIcon?>(null) }
    var selected by rememberSaveable(userId) { mutableStateOf(emptyList<Int>()) }
    var rememberedStage by rememberSaveable(userId) { mutableStateOf(RoomsStage.List.name) }
    var rememberedRoomId by rememberSaveable(userId) { mutableStateOf<Int?>(null) }
    val restore = remember(userId) { rememberedStage to rememberedRoomId }
    var validation by remember { mutableStateOf<String?>(null) }
    val controller = remember(context, userId) {
        RoomsController(RoomsRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, RoomsApi::class.java),
            AuthRepositoryProvider.get(context)), userId).apply { setQuery(query) }
    }
    val state by controller.state.collectAsStateWithLifecycle()
    val latestState by rememberUpdatedState(state)
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    var reload by remember { mutableIntStateOf(0) }
    var action by remember(userId) { mutableStateOf<CatalogProduct?>(null) }
    var information by remember(userId) { mutableStateOf<CatalogProduct?>(null) }
    var deleting by remember(userId) { mutableStateOf(false) }
    var editName by rememberSaveable(userId) { mutableStateOf("") }
    var editIcon by rememberSaveable(userId) { mutableStateOf(RoomIcon.Toilet) }
    var editSelected by rememberSaveable(userId) { mutableStateOf(emptyList<Int>()) }
    var removal by remember(userId) { mutableStateOf<CatalogProduct?>(null) }
    fun createDraft(seed: Int? = null) {
        draftName = ""; draftIcon = null; selected = listOfNotNull(seed); validation = null
        scope.launch { controller.prepareCreate() }
    }
    LaunchedEffect(controller) {
        when {
            initialRoomId?.toIntOrNull() != null -> controller.open(initialRoomId.toInt())
            initialProductId?.toIntOrNull() != null -> { selected = listOf(initialProductId.toInt()); controller.prepareCreate() }
            restore.first in setOf(RoomsStage.Create.name, RoomsStage.Error.name) -> controller.prepareCreate()
            restore.second != null && restore.first in setOf(RoomsStage.Detail.name, RoomsStage.Success.name, RoomsStage.Adding.name, RoomsStage.Editing.name) -> {
                controller.open(requireNotNull(restore.second))
                if (restore.first == RoomsStage.Adding.name) controller.prepareAdd()
                if (restore.first == RoomsStage.Editing.name) controller.prepareEdit()
            }
            restore.first == RoomsStage.Saving.name -> controller.notice("Confira a lista de cômodos antes de cadastrar novamente. A confirmação anterior foi interrompida.")
        }
        onInitialConsumed()
    }
    LaunchedEffect(state.stage, state.detailId) { rememberedStage = state.stage.name; rememberedRoomId = state.detailId }
    LaunchedEffect(state.shelfReady, state.available, state.stage, state.detail) {
        if (state.shelfReady && !state.busy && state.stage in setOf(RoomsStage.Create, RoomsStage.Adding)) {
            val existing = if (state.stage == RoomsStage.Adding) state.detail?.produtos.orEmpty().map { it.id }.toSet() else emptySet()
            val current = selected.filter { id -> id !in existing && state.available.any { it.id == id } }
            if (current != selected) { selected = current; controller.notice("Seleção atualizada conforme os produtos disponíveis para este cômodo.") }
        }
    }
    LaunchedEffect(controller, state.stage, state.query, reload) {
        if (state.stage == RoomsStage.List) { delay(350); controller.loadList() }
    }
    DisposableEffect(owner, controller) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) {
            when (latestState.stage) {
                RoomsStage.List -> reload++
                RoomsStage.Detail -> if (!latestState.busy) latestState.detailId?.let { id -> scope.launch { controller.open(id) } }
                RoomsStage.Create, RoomsStage.Adding, RoomsStage.Editing -> if (!latestState.busy) scope.launch { controller.loadShelf() }
                else -> Unit
            }
        } }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val problems = listOf(state.error, state.moreError, state.shelfError, state.detailError, state.mutationError)
    LaunchedEffect(problems) { if (problems.any { it?.kind == GenericErrorKind.SessionExpired }) onSessionExpired() }
    LaunchedEffect(state.notice) { if (state.notice != null) { delay(5_000); controller.notice(null) } }
    BackHandler(enabled = state.stage != RoomsStage.List) {
        if (!state.busy) when (state.stage) {
            RoomsStage.Adding, RoomsStage.Editing -> scope.launch { controller.cancelAdd() }
            RoomsStage.Error -> scope.launch { controller.prepareCreate() }
            else -> controller.showList()
        }
    }
    val tokens = quimiaColorTokens()
    val initialListLoading = com.quimia.android.presentation.components.rememberEntryLoading(state.loading, userId)
    val screenLoading = initialListLoading && state.stage == RoomsStage.List
    com.quimia.android.presentation.components.ReportDestinationLoading("rooms", pending = when (state.stage) {
        RoomsStage.List -> screenLoading
        RoomsStage.Create, RoomsStage.Editing -> state.shelfLoading
        RoomsStage.Adding -> false
        RoomsStage.Detail -> state.detailLoading
        else -> false
    }, navigationVisible = state.stage == RoomsStage.List,
        page = (if (state.stage == RoomsStage.Adding) RoomsStage.Detail else state.stage) to state.detailId)
    theme.QuimiaSkeleton(screenLoading) {
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground)) {
        // Adding products is an overlay over the same detail page, not a new loading destination.
        motion.QuimiaScreenDissolve(if (state.stage == RoomsStage.Adding) RoomsStage.Detail else state.stage, Modifier.fillMaxSize()) { stage ->
            when (stage) {
                RoomsStage.List -> RoomList(if (screenLoading) previewRooms else state.rooms.map { it.uiRoom() }, onOpen = { if (!screenLoading) scope.launch { controller.open(it.toInt()) } },
                    onCreate = { createDraft() }, onMenuItemSelected = onMenuItemSelected, onAiClick = onAiClick,
                    connected = true, initialQuery = query, onQueryChange = { query = it; controller.setQuery(it) },
                    onUnavailable = controller::notice, showEmpty = !state.loading && state.error == null,
                    statusContent = {
                        RoomsNotice(state.notice) { controller.notice(null) }
                        if (!screenLoading && state.loading && state.rooms.isEmpty()) CatalogLoading("Carregando cômodos…")
                        state.error?.let { CatalogError(it) { reload++ } }
                    }, footerContent = {
                        if (state.moreLoading) CatalogLoading("Carregando mais cômodos…")
                        state.moreError?.let { CatalogError(it) { scope.launch { controller.loadMore() } } }
                        if (state.nextPage != null && !state.loading && !state.moreLoading && state.moreError == null) {
                            QuimiaButton(text = "Carregar mais", iconSize = 0, espacamento = 0, modifier = Modifier.fillMaxWidth().height(48.dp),
                                onClick = { scope.launch { controller.loadMore() } })
                        }
                    })
                RoomsStage.Create -> {
                    val available = state.available
                    val valid = apiRoomValidation(draftName, draftIcon, selected, available.map { it.id }) == null
                    RoomForm(draftName, draftIcon, selected.map(Int::toString), available.map { it.catalogProduct() },
                        error = validation ?: state.mutationError?.message,
                        onNameChange = { draftName = it; validation = null }, onIconChange = { draftIcon = it; validation = null },
                        onToggleProduct = { raw ->
                            if (!state.busy && !state.shelfLoading) {
                                val id = raw.toInt()
                                if (id in selected) selected = selected - id
                                else if (selected.size < 20) selected = selected + id
                                else validation = "Um cômodo pode ter no máximo 20 produtos."
                            }
                        },
                        onClose = controller::showList, onOpenCatalog = onOpenCatalog,
                        onSubmit = { scope.launch {
                            controller.create(draftName, draftIcon, selected)
                        } }, formTitle = "Cadastrar cômodo",
                        submitLabel = if (state.busy) "Salvando…" else "Criar cômodo",
                        nameLimit = 100, enabled = !state.busy && !state.shelfLoading, canSubmit = valid && state.shelfReady && !state.shelfLoading && !state.busy,
                        showEmpty = state.shelfReady && !state.shelfLoading,
                        selectionHint = "Escolha os produtos que você usa para limpar esse cômodo",
                        statusContent = {
                            if (state.shelfLoading && available.isEmpty()) CatalogLoading("Carregando produtos da sua estante…")
                            state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
                            if (state.mutationError != null && !state.busy) QuimiaButton(text = "Atualizar produtos", iconSize = 0, espacamento = 0,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(48.dp), onClick = { scope.launch { controller.loadShelf() } })
                            RoomsNotice(state.notice) { controller.notice(null) }
                        })
                }
                RoomsStage.Editing -> {
                    val room = state.detail
                    val products = (room?.produtos.orEmpty() + state.available).distinctBy { it.id }
                    val changed = room != null && (normalizeRoomName(editName) != room.nome || editIcon.apiCode != room.icone ||
                        editSelected.toSet() != room.produtos.map { it.id }.toSet())
                    RoomForm(editName, editIcon, editSelected.map(Int::toString), products.map { it.catalogProduct() },
                        error = state.mutationError?.message,
                        onNameChange = { editName = it }, onIconChange = { editIcon = it },
                        onToggleProduct = { raw -> val id = raw.toInt(); if (!state.busy && !state.shelfLoading) {
                            editSelected = if (id in editSelected) editSelected - id else (editSelected + id).take(20)
                        } },
                        onClose = { if (!state.busy) scope.launch { controller.cancelAdd() } }, onOpenCatalog = onOpenCatalog,
                        onSubmit = { scope.launch { controller.edit(editName, editIcon, editSelected) } },
                        formTitle = "Editar cômodo", formDescription = "Editar as informações do cômodo",
                        highlightName = false,
                        submitLabel = if (state.busy) "Salvando…" else "Salvar", nameLimit = 100,
                        enabled = !state.busy && !state.shelfLoading,
                        canSubmit = room != null && (changed || state.mutationError != null) && editName.isNotBlank() && editSelected.size <= 20 && state.shelfReady && !state.shelfLoading && !state.busy,
                        showEmpty = state.shelfReady && !state.shelfLoading,
                        statusContent = {
                            if (state.shelfLoading) CatalogLoading("Carregando produtos da sua estante…")
                            state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
                            state.detailError?.let { CatalogError(it) { scope.launch {
                                state.detailId?.let { id -> controller.open(id); controller.prepareEdit() }
                            } } }
                            RoomsNotice(state.notice) { controller.notice(null) }
                        })
                }
                RoomsStage.Adding -> Unit
                RoomsStage.Saving -> RoomCreating(draftName)
                RoomsStage.Success, RoomsStage.Error -> RoomCreationResult(stage == RoomsStage.Success,
                    state.detail?.nome ?: draftName, controller::showList,
                    onContinue = { if (state.stage == RoomsStage.Success) controller.showCreated() else scope.launch { controller.prepareCreate() } },
                    errorMessage = state.mutationError?.message, failureAction = "Revisar cadastro")
                RoomsStage.Detail -> state.detail?.let { room ->
                    RoomDetails(room.uiRoom(), room.produtos.map { it.catalogProduct() }, controller::showList,
                        onProductActions = { id -> if (!state.busy) action = room.produtos.find { it.id.toString() == id }?.catalogProduct() },
                        apiAssessment = { ApiRoomAssessment(room) },
                        onEdit = { if (!state.busy) { editName = room.nome; editIcon = room.uiRoom().icon;
                            editSelected = room.produtos.map { it.id }; scope.launch { controller.prepareEdit() } } },
                        onDelete = { if (!state.busy) { controller.clearMutationError(); deleting = true } },
                        onAddProducts = { selected = emptyList(); validation = null; scope.launch { controller.prepareAdd() } },
                        enabled = !state.busy,
                        statusContent = {
                            RoomsNotice(state.notice) { controller.notice(null) }
                            if (state.busy) CatalogLoading("Atualizando cômodo…")
                            state.mutationError?.let { Text(it.message, Modifier.padding(24.dp), color = tokens.textPrimary) }
                        })
                } ?: Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    QuimiaNav(showBackButton = true, onBackClick = controller::showList, onCloseClick = controller::showList)
                    RoomsNotice(state.notice) { controller.notice(null) }
                    if (state.detailLoading) com.quimia.android.presentation.components.LoadingSkeleton(
                        com.quimia.android.presentation.components.SkeletonLayout.Detail, "Carregando cômodo e avaliação")
                    state.detailError?.let { CatalogError(it) { state.detailId?.let { id -> scope.launch { controller.open(id) } } } }
                }
            }
        }
    }
    }
    if (state.stage == RoomsStage.Adding) {
        val existing = state.detail?.produtos.orEmpty().map { it.id }.toSet()
        RoomAddProductsPanel(products = state.available.filterNot { it.id in existing }.map { it.catalogProduct() },
            selected = selected.map(Int::toString), busy = state.busy, loading = state.shelfLoading,
            ready = state.shelfReady, existingCount = existing.size,
            emptyShelf = state.available.isEmpty(),
            onToggle = { raw -> val id = raw.toInt()
                if (!state.busy && !state.shelfLoading) selected = if (id in selected) selected - id
                    else if (existing.size + selected.size < 20) selected + id else selected
            }, onDismiss = { scope.launch { controller.cancelAdd() } },
            onSubmit = { scope.launch { controller.add(selected) } }, onOpenCatalog = onOpenCatalog,
            statusContent = {
                if (state.shelfLoading) CatalogLoading("Carregando produtos da sua estante…")
                state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
                state.mutationError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
                RoomsNotice(state.notice) { controller.notice(null) }
            })
    }
    if (deleting) RoomDeleteConfirmation(state.detail?.nome.orEmpty(), busy = state.busy,
        onDismiss = { if (!state.busy) { deleting = false; controller.clearMutationError() } },
        onConfirm = { scope.launch { controller.delete(); if (controller.state.value.mutationError == null) deleting = false } },
        statusContent = { state.mutationError?.let { Text(it.message, color = tokens.textPrimary) } })
    action?.let { product -> ShelfActionsPanel(product, onDismiss = { action = null }, showMixtures = false,
        onMixtures = {}, onInformation = { action = null; information = product }, onRemove = { action = null; removal = product }) }
    information?.let { ShelfProductInformation(it, onDismiss = { information = null }) }
    removal?.let { product ->
        fun dismissRemoval() {
            if (!state.busy) {
                removal = null
                if (state.mutationError != null) state.detailId?.let { id -> scope.launch { controller.open(id) } }
            }
        }
        AlertDialog(onDismissRequest = ::dismissRemoval,
            containerColor = tokens.surfaceBase, title = { Text("Remover ${product.name} deste cômodo?", color = tokens.textPrimary) },
            text = { Column { Text("O produto continua salvo na sua estante.", color = tokens.textSecondary)
                state.mutationError?.let { Text(it.message, color = tokens.textPrimary) } } },
            confirmButton = { QuimiaButton(text = if (state.busy) "Removendo…" else "Remover", enabled = !state.busy,
                containerColor = ShelfRemovalColor, textColor = tokens.white, iconSize = 0, espacamento = 0, onClick = {
                    scope.launch { controller.remove(product.id.toInt()); if (controller.state.value.mutationError == null) removal = null }
                }) }, dismissButton = { TextButton(enabled = !state.busy, onClick = ::dismissRemoval) { Text("Cancelar", color = tokens.textPrimary) } })
    }
}

@Composable
private fun RoomsNotice(message: String?, dismiss: () -> Unit) {
    toast.QuimiaToastHost(message = message, type = QuimiaToastType.Info, modifier = Modifier.padding(16.dp), onDismiss = dismiss)
}

@Composable
private fun ApiRoomAssessment(room: ApiRoom) {
    val tokens = quimiaColorTokens()
    val a = room.avaliacao
    val title = when (a.status) { "seguro" -> "Seguro"; "atencao" -> "Atenção"; "critico" -> "Crítico"
        "nao_avaliado" -> "Segurança não avaliada"; else -> "Avaliação não reconhecida" }
    val critical = a.status == "critico"
    val description = when (a.status) {
        "seguro" -> "A combinação é segura"
        "critico" -> "A combinação dos produtos apresenta altos riscos à saúde"
        "atencao" -> "Confira as orientações antes de usar os produtos juntos."
        else -> "A API ainda não confirmou a compatibilidade destes produtos."
    }
    listItem.QuimiaListItem(
        title = if (critical) "Crítico: nunca use esses produtos juntos" else title,
        subtitle = description,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = if (critical) 16.dp else 24.dp),
        horizontalSpacing = 20.dp, cornerRadius = 32.dp, subtitleMaxLines = 4,
        containerColor = if (critical) tokens.error else tokens.surfaceBase,
        titleColor = if (critical) tokens.white else tokens.foregroundPrimary,
        subtitleColor = if (critical) tokens.white.copy(alpha = .85f) else tokens.foregroundSubtle,
        leadingContent = {
            shapeIcon.QuimiaShapeIcon(size = 50.dp, iconSize = 24.dp,
                icon = when (a.status) { "seguro" -> com.composables.icons.lucide.Lucide.Check; "critico" -> com.composables.icons.lucide.Lucide.ShieldX; else -> com.composables.icons.lucide.Lucide.Info },
                containerColor = when (a.status) { "seguro" -> tokens.success; "atencao" -> tokens.warning; else -> tokens.surfaceBase },
                iconColor = if (critical) tokens.error else androidx.compose.ui.graphics.Color(0xFF404040))
        })
}
