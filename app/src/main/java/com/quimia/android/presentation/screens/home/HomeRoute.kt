package com.quimia.android.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import buttons.QuimiaButton
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.home.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.profile.profileInitials
import kotlinx.coroutines.launch
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
internal fun HomeRoute(userId: String, onSearch: (String) -> Unit, onAvatarClick: () -> Unit,
    onFindDropOffClick: () -> Unit, onMixturesClick: () -> Unit, onShelfClick: () -> Unit,
    onMenuItemSelected: (Int) -> Unit, onAiClick: () -> Unit = {}, mixtureRevision: Int = 0, onProfileLoaded: (UserProfile) -> Unit, onSessionExpired: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val auth = remember(context) { AuthRepositoryProvider.get(context) }
    val session by auth.session.collectAsStateWithLifecycle()
    val repository = remember(userId, context, auth) { HomeRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, HomeApi::class.java), auth) }
    val controller = remember(userId, repository) { HomeController(repository, userId) }
    var pointLoading by remember(userId) { mutableStateOf(true) }
    var pointLabel by remember(userId) { mutableStateOf("Carregando…") }
    var lastPoint by remember(userId) { mutableStateOf<HomeSavedPoint?>(null) }
    var pointCoordinate by remember(userId) { mutableStateOf<com.quimia.android.presentation.screens.proximity.NearbyCoordinate?>(null) }
    val pointRepository = remember(auth) { com.quimia.android.data.proximity.ProximityRepository(
        AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, com.quimia.android.data.proximity.ProximityApi::class.java), auth) }
    val state by controller.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    var selected by remember(userId) { mutableStateOf<HomeMixture?>(null) }
    fun refresh() {
        scope.launch { controller.refresh() }
        scope.launch {
            pointLoading = true
            try {
                val point = repository.lastPoint(userId)
                lastPoint = point; pointLabel = point?.nome.orEmpty()
                pointCoordinate = point?.latitude?.let { lat -> point.longitude?.let { lng -> com.quimia.android.presentation.screens.proximity.NearbyCoordinate(lat, lng) } }?.takeIf { it.isValid() }
                if (point != null && pointCoordinate == null) {
                    val full = pointRepository.saved(userId).firstOrNull { it.id == point.id }
                    pointCoordinate = full?.latitude?.let { lat -> full.longitude?.let { lng -> com.quimia.android.presentation.screens.proximity.NearbyCoordinate(lat, lng) } }?.takeIf { it.isValid() }
                }
            }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { pointLabel = "Não foi possível carregar" }
            finally { pointLoading = false }
        }
    }
    DisposableEffect(owner, controller) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) refresh()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(mixtureRevision) { if (mixtureRevision > 0) controller.refresh() }
    LaunchedEffect(state.profile) { state.profile?.let(onProfileLoaded) }
    LaunchedEffect(state.profileError, state.historyError, state.moreError) {
        if (listOf(state.profileError, state.historyError, state.moreError).contains(GenericErrorKind.SessionExpired)) onSessionExpired()
    }
    val availableProfile = homeVisibleProfile(userId, state.profile, (session as? SessionState.Authenticated)?.profile)
    val photoUrl = homePhotoUrl(availableProfile?.fotoUrl)
    val photo = com.quimia.android.presentation.components.rememberProfilePhoto(userId, photoUrl)
    val avatarLoading = availableProfile == null || (photoUrl != null && (photo.state is AsyncImagePainter.State.Empty || photo.state is AsyncImagePainter.State.Loading))
    val screenLoading = com.quimia.android.presentation.components.rememberEntryLoading(
        state.profileLoading || state.historyLoading || pointLoading, userId)
    com.quimia.android.presentation.components.ReportDestinationLoading("home", screenLoading)
    theme.QuimiaSkeleton(screenLoading) {
    HomeScreen(avatarLoading = avatarLoading, avatarInitials = availableProfile?.nome?.let(::profileInitials).orEmpty(),
        avatarPhoto = photo.takeUnless { photoUrl == null || it.state is AsyncImagePainter.State.Error },
        onSearch = onSearch, onAvatarClick = onAvatarClick, onFindDropOffClick = onFindDropOffClick,
        onMixturesClick = onMixturesClick, onShelfClick = onShelfClick, onMixClick = onMixturesClick,
        onMenuItemSelected = onMenuItemSelected, onAiClick = onAiClick, dropOffDescription = pointLabel, dropOffLoading = false,
        dropOffMap = lastPoint?.let { point -> { HomePointMap(point.id, point.nome, pointCoordinate, onFindDropOffClick) } },
        profileStatus = {
            if (!screenLoading) state.profileError?.let { HomeLoadError(it, "seu perfil", ::refresh) }
        }, historyContent = {
            if (screenLoading) HomeLoading("Carregando histórico…")
            else {
            if (state.historyLoading && state.history.isEmpty()) HomeLoading("Carregando histórico…")
            state.historyError?.let { HomeLoadError(it, "o histórico", ::refresh) }
            if (!state.historyLoading && state.historyError == null && state.history.isEmpty()) {
                HomeEmptyHistory(onMixturesClick)
            }
            state.history.forEach { item -> HomeApiHistoryCard(item) { selected = item } }
            if (state.moreLoading) HomeLoading("Carregando mais misturas…")
            state.moreError?.let { HomeLoadError(it, "mais misturas") { scope.launch { controller.loadMore() } } }
            if (state.nextPage != null && !state.moreLoading && !state.historyLoading && state.historyError == null && state.moreError == null) {
                QuimiaButton(text = "Carregar mais", onClick = { scope.launch { controller.loadMore() } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(48.dp), iconSize = 0, espacamento = 0)
            }
            }
        })
    }
    selected?.let { HomeHistoryDetails(it) { selected = null } }
}
