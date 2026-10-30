package com.quimia.android.presentation.screens.mixtures

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Atom
import com.composables.icons.lucide.Lucide
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.catalog.ApiCatalogProduct
import com.quimia.android.data.home.HomeMixture
import com.quimia.android.data.mixtures.*
import com.quimia.android.presentation.screens.catalog.catalogProduct
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.home.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import states.QuimiaEmptyState
import toast.QuimiaToastHost
import toast.QuimiaToastType

@Composable
internal fun MixturesRoute(userId: String, onClose: () -> Unit, onOpenCatalog: () -> Unit,
    onSessionExpired: () -> Unit, onRecorded: () -> Unit, initialProductId: String? = null,
    initialHistory: Boolean = false, historyProductId: String? = null) {
    val context = LocalContext.current.applicationContext
    val sessions = remember(context) { AuthRepositoryProvider.get(context) }
    val repository = remember(userId, sessions) {
        MixturesRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, MixturesApi::class.java), sessions)
    }
    val controller = remember(userId) { MixtureController() }
    val scope = rememberCoroutineScope()
    val shelfLock = remember(userId) { Mutex() }
    var products by remember(userId) { mutableStateOf(emptyList<ApiCatalogProduct>()) }
    var shelfLoading by remember(userId) { mutableStateOf(true) }
    var shelfError by remember(userId) { mutableStateOf<String?>(null) }
    var history by remember(userId) { mutableStateOf(emptyList<HomeMixture>()) }
    var historyLoading by remember(userId) { mutableStateOf(false) }
    var historyLoaded by remember(userId) { mutableStateOf(false) }
    var historyError by remember(userId) { mutableStateOf<String?>(null) }
    var nextPage by remember(userId) { mutableStateOf<Int?>(null) }
    var result by remember(userId) { mutableStateOf<HomeMixture?>(null) }
    var notice by remember(userId) { mutableStateOf<String?>(null) }
    fun errorKind(error: Exception): GenericErrorKind = homeProblem(error).also {
        if (it == GenericErrorKind.SessionExpired || sessions.session.value == SessionState.SignedOut) onSessionExpired()
    }
    suspend fun loadShelf() {
        if (!shelfLock.tryLock()) return
        shelfLoading = true; shelfError = null
        try {
            products = repository.shelf(userId)
            if (controller.state.pendingId == null) controller.state.slots.forEachIndexed { index, ingredient ->
                if (ingredient != null && products.none { it.id.toString() == ingredient.id }) controller.remove(index)
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { errorKind(error); shelfError = authErrorMessage(error) }
        finally { shelfLoading = false; shelfLock.unlock() }
    }
    suspend fun loadHistory(reset: Boolean = true) {
        if (historyLoading) return
        val page = if (reset) 0 else nextPage ?: return
        historyLoading = true; historyError = null
        try {
            val response = repository.history(userId, page, historyProductId?.toIntOrNull())
            history = (if (reset) response.itens else history + response.itens).distinctBy { it.id }
            nextPage = (page + 1).takeIf { it < response.totalPaginas }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { errorKind(error); historyError = authErrorMessage(error) }
        finally { historyLoading = false; historyLoaded = true }
    }
    LaunchedEffect(repository, initialProductId) {
        loadShelf()
        initialProductId?.let { id ->
            products.firstOrNull { it.id.toString() == id }?.let { controller.startWith(it.catalogProduct().mixtureIngredient()) }
                ?: run { if (shelfError == null) notice = "Esse produto não está mais na sua estante." }
        }
    }
    val catalog = remember(products) { products.map { it.catalogProduct() } }
    Box(Modifier.fillMaxSize()) {
        MixturesScreen(controller = controller, savedProductIds = catalog.map { it.id }, catalog = catalog,
            substances = emptyList(), allowSubstances = false, loadingProducts = shelfLoading, productsError = shelfError,
            onRetryProducts = { scope.launch { loadShelf() } }, onClose = onClose, onOpenCatalog = onOpenCatalog,
            initialHistory = initialHistory, historyProductId = historyProductId,
            onHistoryOpen = { scope.launch { loadHistory() } },
            analyzer = { request ->
                try {
                    val ids = request.ingredients.map { requireNotNull(it.id.toIntOrNull()) }
                    result = repository.analyze(userId, ids[0], ids[1])
                    onRecorded()
                    MixtureReply.Recorded
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) {
                    notice = authErrorMessage(error)
                    val kind = errorKind(error)
                    if (error is retrofit2.HttpException && error.code() == 404) loadShelf()
                    MixtureReply.Failed(kind)
                }
            }, remoteHistoryContent = {
                if (historyLoading || !historyLoaded) item {
                    QuimiaToastHost("Carregando histórico…", Modifier.padding(24.dp), type = QuimiaToastType.Loading)
                }
                historyError?.let { message -> item {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuimiaToastHost(message, type = QuimiaToastType.Error)
                        QuimiaButton(text = "Tentar novamente", iconSize = 0, espacamento = 0,
                            onClick = { scope.launch { loadHistory(history.isEmpty()) } })
                    }
                } }
                if (historyLoaded && !historyLoading && historyError == null && history.isEmpty()) item {
                    QuimiaEmptyState(icon = Lucide.Atom, title = "Nenhuma mistura no histórico",
                        description = "Suas análises aparecerão aqui depois de usar Misturar.")
                }
                items(history, key = { it.id }) { item -> HomeApiHistoryCard(item) { result = item } }
                if (nextPage != null && !historyLoading && historyError == null) item {
                    QuimiaButton(text = "Carregar mais", iconSize = 0, espacamento = 0,
                        modifier = Modifier.fillMaxWidth().padding(24.dp), onClick = { scope.launch { loadHistory(false) } })
                }
            })
        QuimiaToastHost(notice, Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(16.dp),
            type = QuimiaToastType.Error, onDismiss = { notice = null })
        result?.let { item -> HomeHistoryDetails(item) { result = null } }
    }
}
