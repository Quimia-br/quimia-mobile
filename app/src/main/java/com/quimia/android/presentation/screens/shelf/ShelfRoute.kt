package com.quimia.android.presentation.screens.shelf

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import buttons.QuimiaIconButton
import card.QuimiaCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.shelf.*
import com.quimia.android.presentation.screens.catalog.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import theme.Typography
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType

internal fun ShelfApiSubstance.shelfSubstance() = ShelfSubstance(id.toString(), nome,
    descricao?.takeIf(String::isNotBlank) ?: "Descrição não informada.", casNumero, classes, produtos.map { it.nome })

internal fun firstAidText(aid: ShelfFirstAid): String {
    if (!aid.encontrada) return "Primeiros socorros não informados para este produto."
    return buildList {
        addAll(aid.orientacoesGerais.filter(String::isNotBlank))
        aid.porExposicao.forEach { exposure ->
            val name = when (exposure.rotaExposicao) { "inalacao" -> "Inalação"; "pele" -> "Pele"; "olhos" -> "Olhos"; "ingestao" -> "Ingestão"; else -> exposure.rotaExposicao }
            val parts = buildList {
                exposure.descricao?.takeIf(String::isNotBlank)?.let(::add)
                exposure.sintomas?.takeIf(String::isNotBlank)?.let { add("Sintomas: $it") }
                exposure.tratamentoEspecial?.takeIf(String::isNotBlank)?.let { add("Tratamento especial: $it") }
                if (exposure.atencaoMedicaImediata) add("Atenção médica imediata indicada na FDS.")
            }
            if (parts.isNotEmpty()) add("$name\n${parts.joinToString("\n")}")
        }
        if (aid.notasAoMedico.any(String::isNotBlank)) add("Notas ao médico\n${aid.notasAoMedico.filter(String::isNotBlank).joinToString("\n")}")
    }.joinToString("\n\n").ifBlank { "Não há orientações detalhadas no retorno." }
}

@Composable
internal fun ShelfRoute(userId: String, onMenuItemSelected: (Int) -> Unit, onAiClick: () -> Unit,
    onAddProduct: () -> Unit, onSessionExpired: () -> Unit,
    onCreateRoom: (String) -> Unit = {}, onOpenRoom: (String) -> Unit = {}) {
    val context = LocalContext.current.applicationContext
    var query by rememberSaveable(userId) { mutableStateOf("") }
    var substancesTab by rememberSaveable(userId) { mutableStateOf(false) }
    var alphabetical by rememberSaveable(userId) { mutableStateOf(true) }
    val repository = remember(context) { ShelfRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, ShelfApi::class.java), AuthRepositoryProvider.get(context)) }
    var favoriteBusy by remember(userId) { mutableStateOf(false) }
    val controller = remember(context, userId) {
        ShelfController(repository, userId).apply {
            setQuery(query); setTab(substancesTab); setSort(if (alphabetical) ShelfSort.Alphabetical else null)
        }
    }
    val state by controller.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    var reload by remember { mutableIntStateOf(0) }
    var action by remember(userId) { mutableStateOf<CatalogProduct?>(null) }
    var removal by remember(userId) { mutableStateOf<CatalogProduct?>(null) }
    LaunchedEffect(controller, state.query, state.order, state.substancesTab, reload) { delay(350); controller.load() }
    DisposableEffect(owner, controller) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) reload++ }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val errors = listOf(state.error, state.moreError, state.removeError, state.summary.error, state.room.error, state.historyError) +
        state.protection.values.map { it.error }
    LaunchedEffect(errors) { if (errors.any { it?.kind == GenericErrorKind.SessionExpired }) onSessionExpired() }
    LaunchedEffect(state.removedId) { if (state.removedId != null && removal?.id == state.removedId.toString()) removal = null }
    LaunchedEffect(state.notice) { if (state.notice != null) { delay(5_000); controller.notice(null) } }
    val products = state.products.map { it.catalogProduct() }
    val screenLoading = com.quimia.android.presentation.components.rememberEntryLoading(state.loading, userId)
    CompositionLocalProvider(theme.LocalQuimiaSkeleton provides screenLoading) {
    ShelfScreen(favoriteIds = state.products.filter { it.favorito }.map { it.id.toString() }, savedIds = products.map { it.id }, onSavedIdsChange = {}, onMenuItemSelected = onMenuItemSelected,
        onAiClick = onAiClick, onAddProduct = onAddProduct, initialQuery = query, initialSubstancesTab = substancesTab,
        apiProducts = if (screenLoading) sampleCatalogProducts.take(4) else products, apiSubstances = if (screenLoading) sampleShelfSubstances else state.substances.map { it.shelfSubstance() },
        onQueryChange = { query = it; controller.setQuery(it) }, onTabChange = { substancesTab = it; controller.setTab(it) },
        onSortChange = { alphabetical = it == ShelfSort.Alphabetical; controller.setSort(it) },
        onApiFavorite = { id, favorite -> if (!favoriteBusy) { favoriteBusy = true; scope.launch {
            try { repository.setFavorite(userId, id.toInt(), favorite); controller.load() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (failure: Exception) { controller.notice(com.quimia.android.data.auth.authErrorMessage(failure)) }
            finally { favoriteBusy = false }
        } } },
        onUnavailable = { controller.notice(it) }, onApiActions = { action = it },
        onApiRoom = { scope.launch { controller.showRoom(it.toInt()) } },
        onApiExpand = { if (!state.loading) scope.launch { controller.protect(it.toInt()) } },
        onMixProduct = { controller.notice("Misturas indisponíveis no momento.") },
        onSubstanceMix = { controller.notice("A mistura direta de substâncias ainda não está disponível.") },
        onSubstanceMixtures = { controller.notice("O histórico por substância ainda não está disponível.") },
        externalOverlay = action != null || removal != null || state.infoId != null || state.roomId != null || state.historyId != null,
        showEmpty = !state.loading && state.error == null,
        protectionContent = if (screenLoading) null else { id ->
            LaunchedEffect(controller, id) { controller.protect(id.toInt()) }
            val protection = state.protection[id.toInt()]
            Text("Como se proteger?", style = Typography.titleMedium, color = quimiaColorTokens().textPrimary)
            if (protection == null || protection.loading) CatalogLoading("Carregando primeiros socorros…")
            protection?.error?.let { CatalogError(it) { scope.launch { controller.protect(id.toInt(), force = true) } } }
            protection?.data?.let { aid ->
                CatalogDescriptionCard(firstAidText(aid))
                aid.versaoFds?.let { Text("FDS: $it", style = Typography.bodySmall, color = quimiaColorTokens().textSecondary) }
                aid.dataAtualizacaoFds?.let { Text("Atualização da FDS: $it", style = Typography.bodySmall, color = quimiaColorTokens().textSecondary) }
            }
        }, statusContent = {
            state.notice?.let { QuimiaToast(modifier = Modifier.padding(16.dp), message = it, type = QuimiaToastType.Info,
                onDismiss = { controller.notice(null) }) }

            if (!screenLoading && state.loading && (if (state.substancesTab) state.substances.isEmpty() else state.products.isEmpty()))
                CatalogLoading(if (state.substancesTab) "Carregando substâncias…" else "Carregando produtos…")
            state.error?.let { CatalogError(it) { reload++ } }
        }, footerContent = {
            if (state.moreLoading) CatalogLoading("Carregando mais produtos…")
            state.moreError?.let { CatalogError(it) { scope.launch { controller.loadMore() } } }
            if (state.nextPage != null && !state.moreLoading && !state.loading && state.moreError == null) {
                QuimiaButton(text = "Carregar mais", modifier = Modifier.fillMaxWidth().padding(24.dp).height(48.dp),
                    iconSize = 0, espacamento = 0, onClick = { scope.launch { controller.loadMore() } })
            }
        })
    }
    action?.let { product ->
        ShelfActionsPanel(product, onDismiss = { action = null }, onRemove = { controller.clearRemoval(); action = null; removal = product },
            onMixtures = { action = null; scope.launch { controller.showHistory(product.id.toInt()) } },
            onInformation = { action = null; scope.launch { controller.showInformation(product.id.toInt()) } })
    }
    removal?.let { product ->
        ShelfRemoveConfirmation(product, onDismiss = { removal = null; controller.clearRemoval() },
            onConfirm = { scope.launch { controller.remove(product.id.toInt()) } }, busy = state.removingId != null,
            statusContent = { state.removeError?.let { Text(it.message, color = quimiaColorTokens().textPrimary, style = Typography.bodyMedium) } })
    }
    state.infoId?.let { id -> products.find { it.id == id.toString() }?.let { product ->
        ShelfProductInformation(product, controller::closeInformation) {
            Spacer(Modifier.height(20.dp))
            Text("Resumo de segurança", style = Typography.titleMedium, color = quimiaColorTokens().textPrimary)
            if (state.summary.loading) CatalogLoading("Carregando resumo…")
            state.summary.error?.let { CatalogError(it) { scope.launch { controller.showInformation(id) } } }
            state.summary.data?.let { summary ->
                CatalogDescriptionCard(summary.resumo?.takeIf(String::isNotBlank) ?: "Resumo de segurança não informado para este produto.")
                summary.versao?.let { Text("FDS: $it", color = quimiaColorTokens().textSecondary, style = Typography.bodySmall) }
                summary.dataAtualizacao?.let { Text("Atualização: $it", color = quimiaColorTokens().textSecondary, style = Typography.bodySmall) }
            }
        }
    } }
    state.roomId?.let { id ->
        if (!state.room.loading && state.room.error == null && state.room.data == null) {
            ShelfNoRoomConfirmation(controller::closeRoom) {
                controller.closeRoom(); onCreateRoom(id.toString())
            }
        } else ShelfFloatingPanel(controller::closeRoom) {
            ShelfPanelTitle("Cômodo do produto", controller::closeRoom)
            if (state.room.loading) CatalogLoading("Carregando cômodo…")
            state.room.error?.let { CatalogError(it) { scope.launch { controller.showRoom(id) } } }
            state.room.data?.let { room ->
                Text(room.nome, color = quimiaColorTokens().textPrimary, style = Typography.titleMedium)
                Text("${room.totalProdutos} produto(s)", color = quimiaColorTokens().textSecondary)
                Text("Situação: ${room.status}", color = quimiaColorTokens().textSecondary)
                QuimiaButton(text = "Abrir cômodo", iconSize = 0, espacamento = 0, onClick = {
                    controller.closeRoom(); onOpenRoom(room.id.toString())
                })
            }
        }
    }
    state.historyId?.let { id -> ShelfFloatingPanel(controller::closeHistory) {
        ShelfPanelTitle("Histórico de misturas", controller::closeHistory)
        if (state.historyLoading) CatalogLoading("Carregando misturas…")
        state.historyError?.let { CatalogError(it) { scope.launch {
            if (state.historyNext != null) controller.moreHistory() else controller.showHistory(id)
        } } }
        if (!state.historyLoading && state.historyError == null && state.history.isEmpty()) {
            Text("Nenhuma mistura salva com este produto.", color = quimiaColorTokens().textSecondary)
        }
        state.history.forEach { mixture ->
            QuimiaCard(title = "${mixture.produtoA?.nome.orEmpty()} & ${mixture.produtoB?.nome.orEmpty()}",
                primaryActionLabel = null, secondaryActionLabel = null, endIcon = null) {
                Text("Resultado: ${shelfMixtureLabel(mixture.resultado)}", color = quimiaColorTokens().textPrimary)
                mixture.severidade?.let { Text("Severidade: $it", color = quimiaColorTokens().textSecondary) }
                mixture.descricaoRisco?.let { Text(it, color = quimiaColorTokens().textSecondary) }
                Text(mixture.consultadoEm ?: "Data não informada", style = Typography.bodySmall, color = quimiaColorTokens().textSecondary)
            }
            Spacer(Modifier.height(16.dp))
        }
        if (state.historyNext != null && !state.historyLoading && state.historyError == null) {
            QuimiaButton(text = "Carregar mais", iconSize = 0, espacamento = 0, onClick = { scope.launch { controller.moreHistory() } })
        }
    } }
}
internal fun shelfMixtureLabel(result: String) = when (result) {
    "compativel" -> "Compatível"; "incompativel" -> "Incompatível"; "nao_avaliado" -> "Não avaliado"; else -> "Resultado não reconhecido ($result)"
}
@Composable
private fun ShelfPanelTitle(title: String, close: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        Text(title, modifier = Modifier.weight(1f), style = Typography.titleLarge, color = quimiaColorTokens().textPrimary)
        QuimiaIconButton(icon = Lucide.X, contentDescription = "Fechar", onClick = close)
    }
    Spacer(Modifier.height(16.dp))
}
