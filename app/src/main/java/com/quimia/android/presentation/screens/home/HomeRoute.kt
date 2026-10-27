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
    onMenuItemSelected: (Int) -> Unit, onProfileLoaded: (UserProfile) -> Unit, onSessionExpired: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val controller = remember(userId, context) {
        HomeController(HomeRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, HomeApi::class.java),
            AuthRepositoryProvider.get(context)), userId)
    }
    val state by controller.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    var selected by remember(userId) { mutableStateOf<HomeMixture?>(null) }
    fun refresh() { scope.launch { controller.refresh() } }
    DisposableEffect(owner, controller) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) refresh()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.profile) { state.profile?.let(onProfileLoaded) }
    LaunchedEffect(state.profileError, state.historyError, state.moreError) {
        if (listOf(state.profileError, state.historyError, state.moreError).contains(GenericErrorKind.SessionExpired)) onSessionExpired()
    }
    val photoUrl = homePhotoUrl(state.profile?.fotoUrl)
    val photo = rememberAsyncImagePainter(photoUrl)
    HomeScreen(avatarInitials = state.profile?.nome?.let(::profileInitials).orEmpty(),
        avatarPhoto = photo.takeUnless { photoUrl == null || it.state is AsyncImagePainter.State.Error },
        onSearch = onSearch, onAvatarClick = onAvatarClick, onFindDropOffClick = onFindDropOffClick,
        onMixturesClick = onMixturesClick, onShelfClick = onShelfClick, onMixClick = onMixturesClick,
        onMenuItemSelected = onMenuItemSelected, dropOffDescription = "Consulte no mapa",
        profileStatus = {
            if (state.profileLoading) HomeLoading("Carregando perfil…")
            state.profileError?.let { HomeLoadError(it, "seu perfil", ::refresh) }
        }, historyContent = {
            if (state.historyLoading) HomeLoading("Carregando histórico…")
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
        })
    selected?.let { HomeHistoryDetails(it) { selected = null } }
}
