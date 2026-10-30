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
import com.composables.icons.lucide.TriangleAlert
import dragger.QuimiaDragger
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import androidx.compose.ui.text.style.TextAlign
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
        "outro" -> "Outro"; else -> tipo?.takeIf { it.isNotBlank() } ?: "Tipo não informado"
    }, 0, 0, description = descricao?.takeIf { it.isNotBlank() } ?: "Descrição não informada.",
    barcode = codBarras.orEmpty(), photoUrl = catalogImageUrl(fotoUrl), brandLogoUrl = catalogImageUrl(marca?.logoUrl), remote = true,
    brandName = marca?.nome)

internal fun catalogImageUrl(value: String?): String? = value?.takeIf { url ->
    runCatching { URI(url).let { it.scheme.equals("https", true) && !it.host.isNullOrBlank() && it.userInfo == null } }.getOrDefault(false)
}

@Composable
internal fun CatalogImage(product: CatalogProduct, modifier: Modifier = Modifier, logo: Boolean = false, detail: Boolean = false, grayscale: Boolean = false) {
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
    var imageLoading by remember(url) { mutableStateOf(url != null) }
    Box(modifier.clip(if (logo) CircleShape else RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        if (url != null && !failed) AsyncImage(model = url, contentDescription = if (logo) null else product.name,
            colorFilter = if (grayscale) androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }) else null,
            contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(), onLoading = { imageLoading = true }, onSuccess = { imageLoading = false }, onError = { failed = true; imageLoading = false })
        if (!detail && url != null && imageLoading && !failed) com.quimia.android.presentation.components.SkeletonBlock(Modifier.fillMaxSize(), if (logo) 100 else 12)
        if (url == null || failed) Icon(Lucide.ImageOff, if (logo) "Marca sem imagem" else "Produto sem imagem disponível",
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
    var rescan by rememberSaveable { mutableStateOf(false) }
    if (rescan) CatalogBarcodeScannerSheet(onDismiss = { rescan = false }, onBarcode = { code -> rescan = false; scope.launch { controller.findBarcode(code) } })
    val canSave = state.shelfReady && !state.shelfLoading
    val screenLoading = com.quimia.android.presentation.components.rememberEntryLoading(state.loading || state.shelfLoading, userId)
    CompositionLocalProvider(theme.LocalQuimiaSkeleton provides screenLoading) {
    CatalogScreen(onMenuItemSelected = onMenuItemSelected, onAiClick = onAiClick,
        initialQuery = initialQuery, products = if (screenLoading) sampleCatalogProducts.take(5) else state.products.map { it.catalogProduct() },
        initialSavedProductIds = state.savedIds.map(Int::toString), onQueryChange = { query = it; controller.setQuery(it) },
        onSaveProducts = { ids -> scope.launch { controller.save(ids.mapNotNull(String::toIntOrNull)) } },
        onProductDetails = { product -> if (!screenLoading) scope.launch { controller.openDetail(product.id.toInt()) } },
        saving = state.savingIds.isNotEmpty(), canSave = canSave,
        showEmpty = !screenLoading && !state.loading && !state.shelfLoading && state.error == null,
        saveMessage = state.notice, saveMessageSuccess = state.noticeSuccess, onDismissSaveMessage = controller::dismissNotice,
        onBarcode = { code -> scope.launch { controller.findBarcode(code) } },
        statusContent = {
            if (!screenLoading) {
            if (state.loading && state.products.isEmpty()) CatalogLoading("Carregando produtos…")
            state.error?.let { CatalogError(it) { scope.launch { controller.search() } } }
            if (state.shelfLoading && !state.loading && state.products.isEmpty()) com.quimia.android.presentation.components.LoadingSkeleton(com.quimia.android.presentation.components.SkeletonLayout.Lines, "Conferindo os produtos da sua estante")
            state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
            state.saveError?.let { CatalogError(it) { scope.launch { controller.retrySave() } } }
            }
        }, footerContent = {
            if (state.moreLoading) CatalogLoading("Carregando mais produtos…")
            state.moreError?.let { CatalogError(it) { scope.launch { controller.loadMore() } } }
            if (state.nextPage != null && !state.loading && !state.moreLoading && state.moreError == null) {
                QuimiaButton(text = "Carregar mais", modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(48.dp),
                    iconSize = 0, espacamento = 0, onClick = { scope.launch { controller.loadMore() } })
            }
        })
    }
    if (state.detailOpen) {
        state.detail?.let { product ->
            CatalogProductDetailsSheet(product.catalogProduct(), product.id in state.savedIds, controller::closeDetail,
                onSave = { scope.launch { controller.save(listOf(product.id)) } },
                saving = state.savingIds.isNotEmpty(), canSave = canSave,
                statusContent = {
                    state.detailError?.let { CatalogError(it) { scope.launch { controller.retryDetail() } } }
                    state.notice?.let { Text(it, style = Typography.bodyMedium, color = quimiaColorTokens().textPrimary) }
                    state.saveError?.let { CatalogError(it) { scope.launch { controller.save(listOf(product.id)) } } }
                    state.shelfError?.let { CatalogError(it) { scope.launch { controller.loadShelf() } } }
                })
        } ?: if (state.lookupBarcode != null && state.detailError?.kind == GenericErrorKind.ContentUnavailable) {
            BarcodeNotFoundSheet(state.lookupBarcode!!, controller::closeDetail) { controller.closeDetail(); rescan = true }
        } else CatalogDetailStatus(state.detailLoading, state.detailError, controller::closeDetail, state.lookupBarcode) {
            scope.launch { controller.retryDetail() }
        }
    }
}

@Composable
internal fun CatalogLoading(message: String) {
    if (theme.LocalQuimiaSkeleton.current) {
        com.quimia.android.presentation.components.LoadingSkeleton(
            if (message.contains("socorros") || message.contains("resumo")) com.quimia.android.presentation.components.SkeletonLayout.Lines
            else com.quimia.android.presentation.components.SkeletonLayout.Grid, message)
    } else toast.QuimiaToastHost(message, modifier = Modifier.padding(16.dp), type = toast.QuimiaToastType.Loading)
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
private fun CatalogDetailStatus(loading: Boolean, error: CatalogProblem?, close: () -> Unit, barcode: String? = null, retry: () -> Unit) {
    val tokens = quimiaColorTokens()
    ModalBottomSheet(onDismissRequest = close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp), sheetMaxWidth = 560.dp,
        shape = RoundedCornerShape(36.dp), containerColor = tokens.surfaceBase, scrimColor = CatalogModalScrimColor, dragHandle = null) {
        HideSystemNavigationBar()
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            QuimiaIconButton(icon = Lucide.X, contentDescription = "Fechar detalhes", onClick = close)
            barcode?.let { Text("Código lido: $it", style = Typography.bodyMedium, color = tokens.textPrimary) }
            if (loading) CatalogLoading("Carregando detalhes…")
            error?.let { CatalogError(it, retry) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarcodeNotFoundSheet(code: String, close: () -> Unit, rescan: () -> Unit) {
    val tokens = quimiaColorTokens()
    ModalBottomSheet(onDismissRequest = close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp), sheetMaxWidth = 326.dp,
        shape = RoundedCornerShape(40.dp), containerColor = tokens.surfaceBase,
        scrimColor = CatalogModalScrimColor, dragHandle = null) {
        HideSystemNavigationBar()
        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().height(27.dp), contentAlignment = Alignment.Center) { QuimiaDragger(height = 3.dp, width = 91.dp) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Escanear rótulo", style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
                    Text("Aponte a câmera para o rótulo do seu produto para achá-lo", style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle)
                }
                QuimiaIconButton(icon = Lucide.X, containerColor = tokens.secondary, contentDescription = "Fechar", onClick = close)
            }
            Column(Modifier.fillMaxWidth().heightIn(min = 322.dp).padding(top = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                QuimiaShapeIcon(size = 79.dp, icon = Lucide.TriangleAlert, containerColor = tokens.surfaceBackground)
                Text("Não foi possível encontrar o produto.", style = QuimiaTextStyles.medium16,
                    color = tokens.foregroundPrimary, textAlign = TextAlign.Center)
                Text("Código lido: $code. Por favor, tente novamente.", style = QuimiaTextStyles.medium16,
                    color = tokens.foregroundSubtle, textAlign = TextAlign.Center)
                QuimiaButton(text = "Escanear novamente", modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    iconSize = 0, espacamento = 0, containerColor = tokens.secondary,
                    textColor = tokens.foregroundPrimary, onClick = rescan)
            }
        }
    }
}
