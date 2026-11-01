package com.quimia.android.presentation.screens.proximity

import motion.QuimiaExpand
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.*
import listItem.QuimiaListItem
import title.QuimiaSectionTitle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import buttons.QuimiaButton
import chip.QuimiaChip
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.proximity.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nav.QuimiaNav
import com.quimia.android.presentation.screens.errors.*
import theme.LightTokens
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastBorder

internal fun NearbyAddress.apiAddress(): AuthAddress {
    val n = number.toIntOrNull()?.takeIf { it > 0 }
        ?: throw IllegalArgumentException("A API aceita somente número de endereço inteiro e maior que zero.")
    require(validationError() == null) { validationError().orEmpty() }
    return AuthAddress(postalCode.filter(Char::isDigit), state.uppercase(), district, street, n, complement.ifBlank { null })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProximityRoute(userId: String, onMenuItemSelected: (Int) -> Unit, onSessionExpired: () -> Unit) {
    val context = LocalContext.current
    val auth = remember(context) { AuthRepositoryProvider.get(context) }
    val session by auth.session.collectAsState()
    val repository = remember(auth) { ProximityRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, ProximityApi::class.java), auth) }
    val scope = rememberCoroutineScope()
    val homeResolver = remember(context) { HomeAddressResolver(context) }
    var home by remember(userId) { mutableStateOf<NearbyCoordinate?>(null) }
    var homeLoading by remember { mutableStateOf(false) }
    var homeError by remember { mutableStateOf<String?>(null) }
    var homeAttempt by remember { mutableIntStateOf(0) }
    val tokens = quimiaColorTokens()
    var type by rememberSaveable(userId) { mutableStateOf("descarte") }
    val registered = false
    var expanded by rememberSaveable { mutableStateOf(false) }
    var address by remember(userId) { mutableStateOf<AuthAddress?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var listVisible by rememberSaveable { mutableStateOf(false) }
    var saved by remember(userId) { mutableStateOf<List<ApiPoint>>(emptyList()) }
    var savedReady by remember(userId) { mutableStateOf(false) }
    var savedLoadCount by remember(userId) { mutableIntStateOf(0) }
    var savingPoint by remember { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<Int?>(null) }
    var showSaved by remember { mutableStateOf(false) }
    var points by remember(userId) { mutableStateOf(emptyList<ApiPoint>()) }
    var nextPage by remember { mutableIntStateOf(0) }
    var total by remember { mutableStateOf<Long?>(null) }
    var hasMore by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var errorKind by remember { mutableStateOf(GenericErrorKind.Unknown) }
    var notice by remember { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    fun load(reset: Boolean = true) {
        if (!reset && (loading || !hasMore)) return
        job?.cancel()
        val requestRevision = ++revision
        val requestType = type; val requestRegistered = registered
        val page = if (reset) 0 else nextPage
        if (reset) { points = emptyList(); total = null; hasMore = false; nextPage = 0 }
        loading = true; error = null
        job = scope.launch {
            try {
                val result = repository.points(userId, requestType, requestRegistered, page)
                if (revision == requestRevision) {
                    points = (if (reset) result.itens else points + result.itens).distinctBy { it.id }
                    total = result.totalItens; nextPage = page + 1; hasMore = nextPage < result.totalPaginas
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { if (revision == requestRevision) { errorKind = apiErrorKind(failure, auth.session.value == SessionState.SignedOut); error = authErrorMessage(failure) } }
            finally { if (revision == requestRevision) loading = false }
        }
    }
    suspend fun loadSaved() {
        savedLoadCount++
        try { saved = repository.saved(userId); savedReady = true }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { savedReady = false; notice = authErrorMessage(failure) }
        finally { savedLoadCount-- }
    }
    suspend fun loadAddress() {
        try { address = repository.address(userId); addressError = null }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { addressError = authErrorMessage(failure) }
    }
    LaunchedEffect(userId, address, addressError, homeAttempt) {
        home = null; homeError = null
        val current = address?.takeIf { addressError == null } ?: return@LaunchedEffect
        homeLoading = true
        try {
            home = homeResolver.resolve(current)
            if (home == null) homeError = "Não foi possível localizar sua casa com precisão. Confira o endereço cadastrado e tente novamente."
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { homeError = "Não foi possível localizar sua casa agora. Confira a conexão e tente novamente." }
        finally { homeLoading = false }
    }
    LaunchedEffect(userId, type, registered) { load() }
    LaunchedEffect(userId) { loadAddress(); loadSaved() }
    LaunchedEffect(session) { if (session == SessionState.SignedOut) onSessionExpired() }
    val owner = LocalLifecycleOwner.current
    val latestReload by rememberUpdatedState({ load(); scope.launch { loadAddress() } })
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) latestReload() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); job?.cancel() }
    }
    fun openMaps(point: ApiPoint) {
        val query = point.mapsQuery ?: run { notice = "Este ponto não tem endereço suficiente na API para abrir uma rota."; return }
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))) }
        catch (_: android.content.ActivityNotFoundException) { notice = "Nenhum aplicativo de mapas disponível neste aparelho." }
    }
    Column(Modifier.fillMaxSize().background(tokens.surfaceBackground).statusBarsPadding()) {
        Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
        NearbyNavigationHeader(loadingMessage = when {
            savingPoint -> "Salvando ponto…"
            loading || savedLoadCount > 0 -> "Carregando pontos…"
            else -> null
        }, onBack = { onMenuItemSelected(0) })
        QuimiaSectionTitle(title = "Proximidade", titleSize = 24.sp, textStyle = QuimiaTextStyles.medium24,
            horizontalPadding = 32.dp, verticalPadding = 32.dp) {
            QuimiaIconButton(icon = Lucide.ChevronDown, iconRotationDegrees = if (expanded) 180f else 0f,
                modifier = Modifier.size(24.dp), containerColor = Color.Transparent, iconSize = 24.dp,
                contentDescription = if (expanded) "Recolher endereço" else "Mostrar endereço", onClick = { expanded = !expanded })
        }
        QuimiaExpand(visible = expanded) {
            QuimiaListItem(modifier = Modifier.padding(horizontal = 24.dp), title = "Casa",
                subtitle = addressError ?: address?.let { listOfNotNull(it.rua, it.numero.toString(), it.bairro, it.estado).joinToString(" · ") }
                    ?: "Defina seu endereço de referência",
                titleMaxLines = 1, subtitleMaxLines = 1, containerColor = tokens.surfaceBase, contentPadding = PaddingValues(24.dp),
                trailingContent = { QuimiaIconButton(icon = Lucide.Pencil, size = QuimiaIconButtonSize.Medium,
                    containerColor = tokens.secondary, contentDescription = "Alterar endereço", onClick = { editing = true }) },
                onClick = { editing = true })
            Spacer(Modifier.height(16.dp))
        }
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 39.dp, topEnd = 39.dp))) {
            ProximityMap(points.mapNotNull { point ->
                val coordinate = point.latitude?.let { lat -> point.longitude?.let { lng -> NearbyCoordinate(lat, lng) } }
                    ?.takeIf { it.isValid() } ?: return@mapNotNull null
                NearbyPoint(point.id, point.nome, point.address, if (point.tipo == "descarte") NearbyCategory.Collection else NearbyCategory.Shopping,
                    "", 0f, 0f, coordinate)
            }, selectedPoint, { selectedPoint = it; showSaved = false; listVisible = false }, bottomContentInset = if (selectedPoint == null) 91.dp else 265.dp,
                homeCoordinate = home, homeLabel = address?.let { "${it.rua.orEmpty()}, ${it.numero} · ${it.bairro}" }.orEmpty(),
                onHomeUnavailable = {
                    when {
                        addressError != null -> notice = addressError
                        address == null -> editing = true
                        homeLoading -> notice = "Localizando seu endereço cadastrado…"
                        else -> { notice = homeError; homeAttempt++ }
                    }
                })
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("descarte" to "Descarte", "compra" to "Compra").forEach { (value, label) ->
                    QuimiaChip(label = label, selected = type == value, centerContent = true, modifier = Modifier.weight(1f), onClick = { type = value })
                }
            }
            if (error != null) TextButton(modifier = Modifier.align(Alignment.TopCenter).padding(top = 72.dp),
                onClick = { showSaved = false; listVisible = true }) { Text("Não foi possível carregar os pontos. Tentar novamente", color = tokens.foregroundPrimary) }
            points.find { it.id == selectedPoint }?.takeIf { it.latitude != null && it.longitude != null }?.let { point ->
                NearbyPointCard(point = NearbyPoint(point.id, point.nome, point.address,
                    if (point.tipo == "descarte") NearbyCategory.Collection else NearbyCategory.Shopping, "", 0f, 0f,
                    NearbyCoordinate(requireNotNull(point.latitude), requireNotNull(point.longitude))),
                    saved = saved.any { it.id == point.id },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(start = 17.dp, end = 17.dp, bottom = 91.dp),
                    onSave = {
                        if (savedReady && !savingPoint) {
                            savingPoint = true
                            scope.launch {
                                try {
                                    if (saved.any { it.id == point.id }) repository.removePoint(userId, point.id)
                                    else repository.savePoint(userId, point.id)
                                    loadSaved()
                                    notice = if (saved.any { it.id == point.id }) "Ponto salvo com sucesso" else "Ponto removido dos salvos"
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (failure: Exception) { notice = authErrorMessage(failure) }
                                finally { savingPoint = false }
                            }
                        }
                    })
            }
            Surface(Modifier.align(Alignment.BottomCenter).padding(horizontal = 17.dp).fillMaxWidth().height(75.dp)
                .pointerInput(Unit) { detectVerticalDragGestures { change, amount -> change.consume(); if (amount < -8f) { showSaved = true; listVisible = true; scope.launch { loadSaved() } } } }
                .clickable(onClickLabel = "Abrir pontos salvos") { showSaved = true; listVisible = true; scope.launch { loadSaved() } },
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp), color = tokens.secondary, border = quimiaContrastBorder()) {
                Box(contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.padding(top = 44.dp).width(90.dp).height(3.dp).background(tokens.white, androidx.compose.foundation.shape.CircleShape))
                }
            }

        }
    }
    if (listVisible && (error == null || showSaved)) {
        val listed = if (showSaved) saved else points
        NearbyPointsSheet(points = listed.map { point -> NearbyPointListEntry(point.id, point.nome, point.address,
            point.tipo, saved.any { it.id == point.id }, point.mapsQuery != null) },
            initialCategory = type, savedOnly = showSaved, loading = if (showSaved) savedLoadCount > 0 else loading,
            error = notice, onErrorDismiss = { notice = null }, canToggleSaved = savedReady && !savingPoint,
            hasMore = !showSaved && hasMore && error == null, onMore = { load(false) }, onCategory = { type = it },
            onShowSaved = { showSaved = true; scope.launch { loadSaved() } }, onDismiss = { listVisible = false },
            onSelect = { id -> listed.find { it.id == id }?.let(::openMaps) },
            onOpenMap = { id -> listed.find { it.id == id }?.let(::openMaps) },
            onToggleSaved = { id ->
                if (savedReady && !savingPoint) {
                    savingPoint = true
                    scope.launch {
                        try {
                            if (saved.any { it.id == id }) repository.removePoint(userId, id) else repository.savePoint(userId, id)
                            loadSaved()
                        } catch (cancelled: CancellationException) { throw cancelled }
                        catch (failure: Exception) { notice = authErrorMessage(failure) }
                        finally { savingPoint = false }
                    }
                }
            })
    }
    if (listVisible && error != null && !showSaved) GenericErrorScreen(errorKind,
        onClose = { listVisible = false }, description = if (errorKind == GenericErrorKind.Unknown) error.orEmpty() else errorKind.description,
        onAction = { action -> when (action) {
            GenericErrorAction.Retry -> load(reset = points.isEmpty())
            GenericErrorAction.SignIn -> onSessionExpired()
            GenericErrorAction.Home -> onMenuItemSelected(0)
        } })
    if (editing) NearbyAddressSheet(onDismiss = { editing = false }, onSave = { editing = false; if (registered) load() },
        initialAddress = address?.let { NearbyAddress(it.cep, it.estado, "", it.bairro, it.rua.orEmpty(), it.numero.toString(), it.complemento.orEmpty()) },
        saveRemote = { value ->
            val body = value.apiAddress()
            val coordinate = try { homeResolver.resolve(body) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { null }
            address = repository.saveAddress(userId, body.copy(latitude = coordinate?.latitude, longitude = coordinate?.longitude))
            addressError = null
        })
    notice?.takeIf { !listVisible }?.let { text ->
        com.quimia.android.presentation.components.QuimiaFeedbackDialog(title = text, onDismiss = { notice = null },
            kind = if (text == "Ponto salvo com sucesso" || text == "Ponto removido dos salvos")
                com.quimia.android.presentation.components.QuimiaFeedbackKind.Success else com.quimia.android.presentation.components.QuimiaFeedbackKind.Info)
    }
}
