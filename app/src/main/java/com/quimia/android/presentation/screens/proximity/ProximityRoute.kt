package com.quimia.android.presentation.screens.proximity

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
    var registered by rememberSaveable(userId) { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var address by remember(userId) { mutableStateOf<AuthAddress?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var listVisible by rememberSaveable { mutableStateOf(false) }
    var saved by remember(userId) { mutableStateOf<List<ApiPoint>>(emptyList()) }
    var savedReady by remember(userId) { mutableStateOf(false) }
    var savingPoint by remember { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<Int?>(null) }
    var showSaved by remember { mutableStateOf(false) }
    var points by remember(userId) { mutableStateOf(emptyList<ApiPoint>()) }
    var nextPage by remember { mutableIntStateOf(0) }
    var total by remember { mutableStateOf<Long?>(null) }
    var hasMore by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
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
            catch (failure: Exception) { if (revision == requestRevision) error = authErrorMessage(failure) }
            finally { if (revision == requestRevision) loading = false }
        }
    }
    suspend fun loadSaved() {
        try { saved = repository.saved(userId); savedReady = true }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { savedReady = false; notice = authErrorMessage(failure) }
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
        QuimiaNav(showBackButton = true, showCloseButton = false, onBackClick = { onMenuItemSelected(0) })
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Proximidade", Modifier.weight(1f), style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
            TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Recolher" else "Endereço", color = tokens.foregroundPrimary) }
        }
        if (expanded) Surface(Modifier.padding(horizontal = 24.dp, vertical = 12.dp).fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Casa", style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                Text(addressError ?: address?.let { listOfNotNull(it.rua, it.numero.toString(), it.bairro, it.estado).joinToString(" · ") }
                    ?: "Cadastre um endereço de referência", style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = { editing = true }) { Text("Alterar endereço", color = tokens.foregroundPrimary) }
                    if (addressError != null) TextButton(onClick = { scope.launch { loadAddress() } }) { Text("Tentar novamente") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = registered, enabled = address != null && addressError == null, onCheckedChange = { registered = it })
                    Text("Filtrar pelo endereço cadastrado", style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary)
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 39.dp, topEnd = 39.dp))) {
            ProximityMap(points.mapNotNull { point ->
                val coordinate = point.latitude?.let { lat -> point.longitude?.let { lng -> NearbyCoordinate(lat, lng) } }
                    ?.takeIf { it.isValid() } ?: return@mapNotNull null
                NearbyPoint(point.id, point.nome, point.address, if (point.tipo == "descarte") NearbyCategory.Collection else NearbyCategory.Shopping,
                    "", 0f, 0f, coordinate)
            }, selectedPoint, { selectedPoint = it; showSaved = false; listVisible = true }, bottomContentInset = 150.dp,
                homeCoordinate = home, homeLabel = address?.let { "${it.rua.orEmpty()}, ${it.numero} · ${it.bairro}" }.orEmpty(),
                onHomeUnavailable = {
                    when {
                        addressError != null -> notice = addressError
                        address == null -> editing = true
                        homeLoading -> notice = "Localizando seu endereço cadastrado…"
                        else -> { notice = homeError; homeAttempt++ }
                    }
                })
            Row(Modifier.align(Alignment.TopCenter).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("descarte" to "Descarte", "compra" to "Compra").forEach { (value, label) ->
                    QuimiaChip(label = label, selected = type == value, centerContent = true, modifier = Modifier.weight(1f), onClick = { type = value })
                }
            }
            Surface(Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(28.dp), color = tokens.surfaceBackground, border = quimiaContrastBorder()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pontos disponíveis no mapa e na lista.", style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary)
                    QuimiaButton(text = if (loading) "Carregando pontos…" else if (error != null) "Ver erro e tentar novamente" else "Ver pontos (${total ?: 0})",
                        textColor = LightTokens.foregroundPrimary, iconSize = 0, espacamento = 0,
                        modifier = Modifier.fillMaxWidth(), onClick = { showSaved = false; listVisible = true })
                    TextButton(onClick = { showSaved = true; listVisible = true; scope.launch { loadSaved() } }) { Text("Pontos salvos", color = tokens.foregroundPrimary) }
                }
            }
        }
    }
    if (listVisible) ModalBottomSheet(onDismissRequest = { listVisible = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = tokens.surfaceBackground) {
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 600.dp), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Pontos de ${if (type == "compra") "compra" else "descarte"}", style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary) }
            if (loading) item { CircularProgressIndicator() }
            if (error != null) item {
                Text(error.orEmpty(), color = tokens.foregroundPrimary)
                TextButton(onClick = { load(reset = points.isEmpty()) }) { Text("Tentar novamente") }
            }
            if (!loading && error == null && (if (showSaved) saved.isEmpty() else points.isEmpty())) item { Text(if (showSaved) "Nenhum ponto salvo." else "Nenhum ponto encontrado para este filtro.", color = tokens.foregroundPrimary) }
            items(if (showSaved) saved else points, key = { it.id }) { point ->
                Surface(shape = RoundedCornerShape(28.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(point.nome, style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                        Text(point.address, style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
                        QuimiaButton(text = "Abrir aplicativo de mapas", iconSize = 0, espacamento = 0,
                            textColor = LightTokens.foregroundPrimary, enabled = point.mapsQuery != null,
                            modifier = Modifier.fillMaxWidth(), onClick = { openMaps(point) })
                        TextButton(enabled = savedReady && !savingPoint, onClick = {
                            savingPoint = true
                            scope.launch {
                                try {
                                    if (saved.any { it.id == point.id }) repository.removePoint(userId, point.id)
                                    else repository.savePoint(userId, point.id)
                                    loadSaved()
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (failure: Exception) { notice = authErrorMessage(failure) }
                                finally { savingPoint = false }
                            }
                        }) { Text(if (saved.any { it.id == point.id }) "Remover dos salvos" else "Salvar ponto", color = tokens.foregroundPrimary) }
                    }
                }
            }
            if (!showSaved && hasMore && !loading && error == null) item { TextButton(onClick = { load(false) }) { Text("Carregar mais") } }
        }
    }
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
    notice?.let { text -> AlertDialog(onDismissRequest = { notice = null }, text = { Text(text) },
        confirmButton = { TextButton(onClick = { notice = null }) { Text("Entendi") } }) }
}
