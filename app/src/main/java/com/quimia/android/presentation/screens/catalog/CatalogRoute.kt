package com.quimia.android.presentation.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import coil.compose.AsyncImage
import com.composables.icons.lucide.ImageOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.catalog.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.system.HideSystemNavigationBar
import image.QuimiaImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import theme.Typography
import theme.quimiaColorTokens
import java.net.URI

internal fun ApiCatalogProduct.catalogProduct() = CatalogProduct(id.toString(), nome,
    when (tipo) {
        "limpeza_geral" -> "Limpeza geral"; "desinfetante" -> "Desinfetante"; "desincrustante" -> "Desincrustante"
        "desengraxante" -> "Desengraxante"; "alvejante" -> "Alvejante"; "aromatizante" -> "Aromatizante"
        "outro" -> "Outro"; else -> tipo
    }, 0, 0, description = descricao?.takeIf { it.isNotBlank() } ?: "Descrição não informada.",
    barcode = codBarras.orEmpty(), photoUrl = catalogImageUrl(fotoUrl), brandLogoUrl = catalogImageUrl(marca?.logoUrl), remote = true)

internal fun catalogImageUrl(value: String?): String? = value?.takeIf { url ->
    runCatching { URI(url).let { it.scheme.equals("https", true) && !it.host.isNullOrBlank() && it.userInfo == null } }.getOrDefault(false)
}

@Composable
internal fun CatalogImage(product: CatalogProduct, modifier: Modifier = Modifier, logo: Boolean = false, detail: Boolean = false) {
    val tokens = quimiaColorTokens()
    if (!product.remote) {
        QuimiaImage(painter = painterResource(if (logo) product.logoRes else if (detail) product.detailImageRes else product.imageRes),
            contentDescription = if (logo) null else product.name, modifier = modifier,
            contentScale = ContentScale.Fit, shape = if (logo) CircleShape else RoundedCornerShape(12.dp),
            backgroundColor = if (logo) tokens.surfaceBackground else tokens.surfaceBase)
        return
    }
    var failed by remember(product.id, product.photoUrl, product.brandLogoUrl, logo) { mutableStateOf(false) }
    val url = if (logo) product.brandLogoUrl else product.photoUrl
    Box(modifier.clip(if (logo) CircleShape else RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        if (url != null && !failed) AsyncImage(model = url, contentDescription = if (logo) null else product.name,
            contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(), onError = { failed = true })
        else Icon(Lucide.ImageOff, if (logo) "Marca sem imagem" else "Produto sem imagem disponível",
            modifier = Modifier.size(if (logo) 22.dp else 36.dp), tint = tokens.textSecondary)
    }
}

@Composable
internal fun CatalogRoute(userId: String, onMenuItemSelected: (Int) -> Unit, onAiClick: () -> Unit,
    onSessionExpired: () -> Unit, initialQuery: String = "") {
    val context = LocalContext.current.applicationContext
    var query by rememberSaveable(userId) { mutableStateOf(initialQuery) }
    val controller = remember(context, userId) {
        CatalogController(CatalogRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, CatalogApi::class.java),
            AuthRepositoryProvider.get(context)), userId).apply { setQuery(query) }
    }
    val state by controller.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(controller, state.query) { delay(350); controller.search() }
    DisposableEffect(owner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) scope.launch { controller.loadShelf() }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) scope.launch { controller.loadShelf() }
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.error, state.shelfError, state.moreError, state.saveError, state.detailError) {
        if (listOf(state.error, state.shelfError, state.moreError, state.saveError, state.detailError)
                .any { it?.kind == GenericErrorKind.SessionExpired }) onSessionExpired()
    }
    LaunchedEffect(state.notice) { if (state.notice != null) { delay(5_000); controller.dismissNotice() } }
    val canSave = state.shelfReady && !state.shelfLoading
    CatalogScreen(onMenuItemSelected = onMenuItemSelected, onAiClick = onAiClick,
        initialQuery = initialQuery, products = state.products.map { it.catalogProduct() },
        initialSavedProductIds = state.savedIds.map(Int::toString), onQueryChange = { query = it; controller.setQuery(it) },
        onSaveProducts = { ids -> scope.launch { controller.save(ids.mapNotNull(String::toIntOrNull)) } },
        onProductDetails = { product -> scope.launch { controller.openDetail(product.id.toInt()) } },
        saving = state.savingIds.isNotEmpty(), canSave = canSave,
        saveMessage = state.notice, saveMessageSuccess = state.noticeSuccess, onDismissSaveMessage = controller::dismissNotice,
        onBarcode = { code -> scope.launch { controller.findBarcode(code) } },
        statusContent = {
            if (state.loading) CatalogLoading("Carregando produtos…")
            state.error?.let { CatalogError(it) { scope.launch { controller.search() } } }
            if (!state.loading && state.error == null && state.products.isEmpty()) {
                Text("Nenhum produto encontrado", Modifier.padding(24.dp), color = quimiaColorTokens().textSecondary)
            }
            if (state.shelfLoading) CatalogLoading("Conferindo os produtos da sua estante…")
            state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
            state.saveError?.let { CatalogError(it) { scope.launch { controller.retrySave() } } }
        }, footerContent = {
            if (state.moreLoading) CatalogLoading("Carregando mais produtos…")
            state.moreError?.let { CatalogError(it) { scope.launch { controller.loadMore() } } }
            if (state.nextPage != null && !state.loading && !state.moreLoading && state.moreError == null) {
                QuimiaButton(text = "Carregar mais", modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(48.dp),
                    iconSize = 0, espacamento = 0, onClick = { scope.launch { controller.loadMore() } })
            }
        })
    if (state.detailOpen) {
        state.detail?.let { product ->
            CatalogProductDetailsSheet(product.catalogProduct(), product.id in state.savedIds, controller::closeDetail,
                onSave = { scope.launch { controller.save(listOf(product.id)) } },
                saving = state.savingIds.isNotEmpty(), canSave = canSave,
                statusContent = {
                    state.notice?.let { Text(it, style = Typography.bodyMedium, color = quimiaColorTokens().textPrimary) }
                    state.saveError?.let { CatalogError(it) { scope.launch { controller.save(listOf(product.id)) } } }
                    state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
                })
        } ?: CatalogDetailStatus(state.detailLoading, state.detailError, controller::closeDetail) {
            scope.launch { controller.retryDetail() }
        }
    }
}

@Composable
internal fun CatalogLoading(message: String) {
    val tokens = quimiaColorTokens()
    Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = tokens.primary)
        Text(message, style = Typography.bodyMedium, color = tokens.textPrimary)
    }
}
@Composable
internal fun CatalogError(problem: CatalogProblem, retry: () -> Unit) {
    val tokens = quimiaColorTokens()
    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(problem.message, style = Typography.bodyMedium, color = tokens.textPrimary)
        if (problem.kind != GenericErrorKind.SessionExpired) QuimiaButton(text = "Tentar novamente", modifier = Modifier.fillMaxWidth().height(44.dp),
            iconSize = 0, espacamento = 0, onClick = retry)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogDetailStatus(loading: Boolean, error: CatalogProblem?, close: () -> Unit, retry: () -> Unit) {
    val tokens = quimiaColorTokens()
    ModalBottomSheet(onDismissRequest = close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp), sheetMaxWidth = 560.dp,
        shape = RoundedCornerShape(36.dp), containerColor = tokens.surfaceBase, scrimColor = CatalogModalScrimColor, dragHandle = null) {
        HideSystemNavigationBar()
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            QuimiaIconButton(icon = Lucide.X, contentDescription = "Fechar detalhes", onClick = close)
            if (loading) CatalogLoading("Carregando detalhes…")
            error?.let { CatalogError(it, retry) }
        }
    }
}
