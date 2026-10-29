package com.quimia.android.presentation.screens.rooms

import androidx.activity.compose.BackHandler
import com.composables.icons.lucide.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    var reload by remember { mutableIntStateOf(0) }
    var action by remember(userId) { mutableStateOf<CatalogProduct?>(null) }
    var information by remember(userId) { mutableStateOf<CatalogProduct?>(null) }
    var editing by remember(userId) { mutableStateOf(false) }
    var deleting by remember(userId) { mutableStateOf(false) }
    var editName by remember(userId) { mutableStateOf("") }
    var editIcon by remember(userId) { mutableStateOf(RoomIcon.Toilet) }
    var iconMenu by remember { mutableStateOf(false) }
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
            restore.second != null && restore.first in setOf(RoomsStage.Detail.name, RoomsStage.Success.name, RoomsStage.Adding.name) -> {
                controller.open(requireNotNull(restore.second))
                if (restore.first == RoomsStage.Adding.name) controller.prepareAdd()
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
            when (state.stage) {
                RoomsStage.List -> reload++
                RoomsStage.Detail -> if (!state.busy) state.detailId?.let { id -> scope.launch { controller.open(id) } }
                RoomsStage.Create, RoomsStage.Adding -> if (!state.busy) scope.launch { controller.loadShelf() }
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
            RoomsStage.Adding -> scope.launch { controller.cancelAdd() }
            RoomsStage.Error -> scope.launch { controller.prepareCreate() }
            else -> controller.showList()
        }
    }
    val tokens = quimiaColorTokens()
    CompositionLocalProvider(theme.LocalQuimiaSkeleton provides (state.loading && state.stage == RoomsStage.List || state.shelfLoading && state.stage in setOf(RoomsStage.Create, RoomsStage.Adding) || state.detailLoading)) {
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground)) {
        when (state.stage) {
            RoomsStage.List -> RoomList(if (state.loading) previewRooms else state.rooms.map { it.uiRoom() }, onOpen = { if (!state.loading) scope.launch { controller.open(it.toInt()) } },
                onCreate = { createDraft() }, onMenuItemSelected = onMenuItemSelected, onAiClick = onAiClick,
                connected = true, initialQuery = query, onQueryChange = { query = it; controller.setQuery(it) },
                onUnavailable = controller::notice, showEmpty = !state.loading && state.error == null,
                statusContent = {
                    RoomsNotice(state.notice) { controller.notice(null) }

                    state.error?.let { CatalogError(it) { reload++ } }
                }, footerContent = {
                    if (state.moreLoading) CatalogLoading("Carregando mais cômodos…")
                    state.moreError?.let { CatalogError(it) { scope.launch { controller.loadMore() } } }
                    if (state.nextPage != null && !state.loading && !state.moreLoading && state.moreError == null) {
                        QuimiaButton(text = "Carregar mais", iconSize = 0, espacamento = 0, modifier = Modifier.fillMaxWidth().height(48.dp),
                            onClick = { scope.launch { controller.loadMore() } })
                    }
                })
            RoomsStage.Create, RoomsStage.Adding -> {
                val adding = state.stage == RoomsStage.Adding
                val existing = if (adding) state.detail?.produtos.orEmpty().map { it.id }.toSet() else emptySet()
                val available = state.available.filterNot { it.id in existing }
                val valid = if (adding) selected.isNotEmpty() && existing.size + selected.distinct().size <= 20 &&
                    selected.all { id -> available.any { it.id == id } }
                    else apiRoomValidation(draftName, draftIcon, selected, available.map { it.id }) == null
                RoomForm(draftName, draftIcon, selected.map(Int::toString), if (state.shelfLoading) sampleCatalogProducts.take(4) else available.map { it.catalogProduct() },
                    error = validation ?: state.mutationError?.message,
                    onNameChange = { draftName = it; validation = null }, onIconChange = { draftIcon = it; validation = null },
                    onToggleProduct = { raw ->
                        if (!state.busy && !state.shelfLoading) {
                            val id = raw.toInt()
                            if (id in selected) selected = selected - id
                            else if (existing.size + selected.size < 20) selected = selected + id
                            else validation = "Um cômodo pode ter no máximo 20 produtos."
                        }
                    },
                    onClose = { if (adding) scope.launch { controller.cancelAdd() } else controller.showList() }, onOpenCatalog = onOpenCatalog,
                    onSubmit = { scope.launch {
                        if (adding) controller.add(selected) else controller.create(draftName, draftIcon, selected)
                    } }, formTitle = if (adding) "Adicionar produtos" else "Cadastrar cômodo",
                    submitLabel = if (state.busy) "Salvando…" else if (adding) "Adicionar produtos" else "Criar cômodo",
                    nameLimit = 100, showIdentity = !adding, canSubmit = valid && state.shelfReady && !state.shelfLoading && !state.busy,
                    showEmpty = state.shelfReady && !state.shelfLoading,
                    selectionHint = if (adding) "Selecione os produtos da sua estante. Limite de 20 por cômodo."
                        else "Escolha os produtos que você usa para limpar esse cômodo",
                    statusContent = {

                        state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
                        if (state.mutationError != null && !state.busy) QuimiaButton(text = "Atualizar produtos", iconSize = 0, espacamento = 0,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(48.dp), onClick = { scope.launch { controller.loadShelf() } })
                        RoomsNotice(state.notice) { controller.notice(null) }
                    })
            }
            RoomsStage.Saving -> RoomCreating(draftName)
            RoomsStage.Success, RoomsStage.Error -> RoomCreationResult(state.stage == RoomsStage.Success,
                state.detail?.nome ?: draftName, controller::showList,
                onContinue = { if (state.stage == RoomsStage.Success) controller.showCreated() else scope.launch { controller.prepareCreate() } },
                errorMessage = state.mutationError?.message, failureAction = "Revisar cadastro")
            RoomsStage.Detail -> state.detail?.let { room ->
                RoomDetails(room.uiRoom(), room.produtos.map { it.catalogProduct() }, controller::showList,
                    onProductActions = { id -> if (!state.busy) action = room.produtos.find { it.id.toString() == id }?.catalogProduct() },
                    apiAssessment = { ApiRoomAssessment(room) },
                    onEdit = { if (!state.busy) { controller.clearMutationError(); editName = room.nome; editIcon = room.uiRoom().icon; editing = true } },
                    onDelete = { if (!state.busy) { controller.clearMutationError(); deleting = true } },
                    onAddProducts = { selected = emptyList(); validation = null; scope.launch { controller.prepareAdd() } },
                    statusContent = {
                        RoomsNotice(state.notice) { controller.notice(null) }
                        if (state.busy) CatalogLoading("Atualizando cômodo…")
                        state.mutationError?.let { Text(it.message, Modifier.padding(24.dp), color = tokens.textPrimary) }
                    })
            } ?: if (state.detailLoading) {
                RoomDetails(previewRooms.first(), sampleCatalogProducts.take(4), controller::showList, {})
            } else Column(Modifier.fillMaxSize().statusBarsPadding()) {
                QuimiaNav(showBackButton = true, onBackClick = controller::showList, onCloseClick = controller::showList)
                RoomsNotice(state.notice) { controller.notice(null) }
                if (state.detailLoading) CatalogLoading("Carregando cômodo e avaliação…")
                state.detailError?.let { CatalogError(it) { state.detailId?.let { id -> scope.launch { controller.open(id) } } } }
            }
        }
    }
    }
    if (editing) AlertDialog(
        onDismissRequest = { if (!state.busy) { editing = false; controller.clearMutationError() } },
        containerColor = tokens.surfaceBase,
        title = { Text("Editar cômodo", color = tokens.textPrimary) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = editName, onValueChange = { editName = it.take(100) }, label = { Text("Nome") }, singleLine = true)
            Box {
                TextButton(onClick = { iconMenu = true }, enabled = !state.busy) { Text("Ícone: ${editIcon.label}", color = tokens.textPrimary) }
                DropdownMenu(expanded = iconMenu, onDismissRequest = { iconMenu = false }) {
                    RoomIcon.entries.forEach { icon -> DropdownMenuItem(text = { Text(icon.label) }, onClick = { editIcon = icon; iconMenu = false }) }
                }
            }
            state.mutationError?.let { Text(it.message, color = tokens.textPrimary) }
        } },
        confirmButton = { TextButton(enabled = !state.busy && editName.isNotBlank(), onClick = { scope.launch {
            controller.edit(editName, editIcon)
            if (controller.state.value.mutationError == null) editing = false
        } }) { Text(if (state.busy) "Salvando…" else "Salvar", color = tokens.textPrimary) } },
        dismissButton = { TextButton(enabled = !state.busy, onClick = { editing = false; controller.clearMutationError() }) { Text("Cancelar", color = tokens.textPrimary) } })
    if (deleting) AlertDialog(
        onDismissRequest = { if (!state.busy) { deleting = false; controller.clearMutationError() } },
        containerColor = tokens.surfaceBase,
        title = { Text("Excluir cômodo?", color = tokens.textPrimary) },
        text = { Column { Text("Os produtos continuam na sua estante.", color = tokens.textSecondary)
            state.mutationError?.let { Text(it.message, color = tokens.textPrimary) } } },
        confirmButton = { TextButton(enabled = !state.busy, onClick = { scope.launch {
            controller.delete()
            if (controller.state.value.mutationError == null) deleting = false
        } }) { Text(if (state.busy) "Excluindo…" else "Excluir", color = tokens.textPrimary) } },
        dismissButton = { TextButton(enabled = !state.busy, onClick = { deleting = false; controller.clearMutationError() }) { Text("Cancelar", color = tokens.textPrimary) } })
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
    message?.let { QuimiaToast(message = it, type = QuimiaToastType.Info, modifier = Modifier.padding(16.dp), onDismiss = dismiss) }
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
