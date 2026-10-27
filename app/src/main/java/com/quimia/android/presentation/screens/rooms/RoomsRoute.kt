package com.quimia.android.presentation.screens.rooms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground)) {
        when (state.stage) {
            RoomsStage.List -> RoomList(state.rooms.map { it.uiRoom() }, onOpen = { scope.launch { controller.open(it.toInt()) } },
                onCreate = { createDraft() }, onMenuItemSelected = onMenuItemSelected, onAiClick = onAiClick,
                connected = true, initialQuery = query, onQueryChange = { query = it; controller.setQuery(it) },
                onUnavailable = controller::notice, showEmpty = !state.loading && state.error == null,
                statusContent = {
                    RoomsNotice(state.notice) { controller.notice(null) }
                    if (state.loading) CatalogLoading("Carregando cômodos…")
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
                RoomForm(draftName, draftIcon, selected.map(Int::toString), available.map { it.catalogProduct() },
                    error = validation ?: state.mutationError?.message,
                    onNameChange = { draftName = it; validation = null }, onIconChange = { draftIcon = it; validation = null },
                    onToggleProduct = { raw ->
                        if (!state.busy) {
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
                        else "Selecione de 2 a 20 produtos da sua estante. Cada produto pode pertencer a um cômodo.",
                    statusContent = {
                        if (state.shelfLoading) CatalogLoading("Carregando produtos da sua estante…")
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
                    onAddProducts = { selected = emptyList(); validation = null; scope.launch { controller.prepareAdd() } },
                    statusContent = {
                        RoomsNotice(state.notice) { controller.notice(null) }
                        if (state.busy) CatalogLoading("Atualizando cômodo…")
                        state.mutationError?.let { Text(it.message, Modifier.padding(24.dp), color = tokens.textPrimary) }
                    })
            } ?: Column(Modifier.fillMaxSize().statusBarsPadding()) {
                QuimiaNav(showBackButton = true, onBackClick = controller::showList, onCloseClick = controller::showList)
                RoomsNotice(state.notice) { controller.notice(null) }
                if (state.detailLoading) CatalogLoading("Carregando cômodo e avaliação…")
                state.detailError?.let { CatalogError(it) { state.detailId?.let { id -> scope.launch { controller.open(id) } } } }
            }
        }
    }
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
    val background = when (a.status) { "critico" -> tokens.error; "atencao" -> tokens.warning
        "seguro" -> tokens.success; else -> tokens.surfaceBase }
    val foreground = if (a.status == "critico") tokens.white else if (a.status in setOf("seguro", "atencao")) androidx.compose.ui.graphics.Color.Black else tokens.textPrimary
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).background(background, androidx.compose.foundation.shape.RoundedCornerShape(32.dp)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = QuimiaTextStyles.medium20, color = foreground)
        Text("${a.totalProdutos} produto(s) · ${a.paresAvaliados} par(es) avaliado(s)", color = foreground)
        if (a.status == "nao_avaliado") Text("A avaliação disponível não confirmou a compatibilidade destes produtos.", color = foreground)
        a.severidadeMaxima?.let { Text("Severidade máxima: $it", color = foreground) }
        a.incompatibilidades.forEach { pair ->
            val first = room.produtos.first { it.id == pair.produtoIdA }.nome
            val second = room.produtos.first { it.id == pair.produtoIdB }.nome
            Text("$first + $second", style = QuimiaTextStyles.medium16, color = foreground)
            pair.descricaoRisco?.let { Text(it, color = foreground) }
            pair.severidade?.let { Text("Severidade: $it", color = foreground) }
        }
    }
}
