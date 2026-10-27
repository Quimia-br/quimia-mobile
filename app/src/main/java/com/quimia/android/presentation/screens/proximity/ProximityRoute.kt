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
    val tokens = quimiaColorTokens()
    var type by rememberSaveable(userId) { mutableStateOf("descarte") }
    var registered by rememberSaveable(userId) { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(true) }
    var address by remember(userId) { mutableStateOf<AuthAddress?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var listVisible by rememberSaveable { mutableStateOf(false) }
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
    suspend fun loadAddress() {
        try { address = repository.address(userId); addressError = null }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { addressError = authErrorMessage(failure) }
    }
    LaunchedEffect(userId, type, registered) { load() }
    LaunchedEffect(userId) { loadAddress() }
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
            // The API has no coordinates. Never place sample markers for real points.
            ProximityMap(emptyList(), null, {}, bottomContentInset = 150.dp)
            Row(Modifier.align(Alignment.TopCenter).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("descarte" to "Descarte", "compra" to "Compra").forEach { (value, label) ->
                    QuimiaChip(label = label, selected = type == value, centerContent = true, modifier = Modifier.weight(1f), onClick = { type = value })
                }
            }
            Surface(Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(28.dp), color = tokens.surfaceBackground, border = quimiaContrastBorder()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Os pontos ainda não têm coordenadas na API. Consulte os endereços na lista.",
                        style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary)
                    QuimiaButton(text = if (loading) "Carregando pontos…" else if (error != null) "Ver erro e tentar novamente" else "Ver pontos (${total ?: 0})",
                        textColor = LightTokens.foregroundPrimary, iconSize = 0, espacamento = 0,
                        modifier = Modifier.fillMaxWidth(), onClick = { listVisible = true })
                }
            }
        }
    }
    if (listVisible && error == null) ModalBottomSheet(onDismissRequest = { listVisible = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = tokens.surfaceBackground) {
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 600.dp), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Pontos de ${if (type == "compra") "compra" else "descarte"}", style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary) }
            if (loading) item { CircularProgressIndicator() }
            if (error != null) item {
                Text(error.orEmpty(), color = tokens.foregroundPrimary)
                TextButton(onClick = { load(reset = points.isEmpty()) }) { Text("Tentar novamente") }
            }
            if (!loading && error == null && points.isEmpty()) item { Text("Nenhum ponto encontrado para este filtro.", color = tokens.foregroundPrimary) }
            items(points, key = { it.id }) { point ->
                Surface(shape = RoundedCornerShape(28.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(point.nome, style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                        Text(point.address, style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
                        QuimiaButton(text = "Abrir aplicativo de mapas", iconSize = 0, espacamento = 0,
                            textColor = LightTokens.foregroundPrimary, enabled = point.mapsQuery != null,
                            modifier = Modifier.fillMaxWidth(), onClick = { openMaps(point) })
                        TextButton(onClick = { notice = "A API ainda não permite salvar pontos por usuário. Nenhum ponto foi salvo." }) {
                            Text("Salvar ponto", color = tokens.foregroundPrimary)
                        }
                    }
                }
            }
            if (hasMore && !loading && error == null) item { TextButton(onClick = { load(false) }) { Text("Carregar mais") } }
        }
    }
    if (listVisible && error != null) GenericErrorScreen(errorKind,
        onClose = { listVisible = false }, description = if (errorKind == GenericErrorKind.Unknown) error.orEmpty() else errorKind.description,
        onAction = { action -> when (action) {
            GenericErrorAction.Retry -> load(reset = points.isEmpty())
            GenericErrorAction.SignIn -> onSessionExpired()
            GenericErrorAction.Home -> onMenuItemSelected(0)
        } })
    if (editing) NearbyAddressSheet(onDismiss = { editing = false }, onSave = { editing = false; if (registered) load() },
        initialAddress = address?.let { NearbyAddress(it.cep, it.estado, "", it.bairro, it.rua.orEmpty(), it.numero.toString(), it.complemento.orEmpty()) },
        saveRemote = { value -> address = repository.saveAddress(userId, value.apiAddress()); addressError = null })
    notice?.let { text -> AlertDialog(onDismissRequest = { notice = null }, text = { Text(text) },
        confirmButton = { TextButton(onClick = { notice = null }) { Text("Entendi") } }) }
}
