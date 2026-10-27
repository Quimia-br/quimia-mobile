package com.quimia.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.auth.AuthViewModel
import com.quimia.android.presentation.screens.auth.login.Login1Screen
import com.quimia.android.presentation.screens.auth.login.LoginHomeScreen
import com.quimia.android.presentation.screens.auth.password.LoginPass1Screen
import com.quimia.android.presentation.screens.auth.password.LoginPass2Screen
import com.quimia.android.presentation.screens.auth.password.LoginPassCodeScreen
import com.quimia.android.presentation.screens.auth.password.LoginPassSuccesScreen
import com.quimia.android.presentation.screens.auth.registration.Cadastro1Screen
import com.quimia.android.presentation.screens.auth.registration.Cadastro2Screen
import com.quimia.android.presentation.screens.home.HomeScreen
import com.quimia.android.presentation.screens.catalog.CatalogScreen
import com.quimia.android.presentation.screens.proximity.ProximityScreen
import com.quimia.android.presentation.screens.shelf.ShelfScreen
import com.quimia.android.presentation.screens.rooms.QuimiaRoom
import com.quimia.android.presentation.screens.rooms.RoomListSaver
import com.quimia.android.presentation.screens.rooms.RoomsScreen
import com.quimia.android.presentation.screens.profile.*
import com.quimia.android.utils.AnalyticsLogger
import com.quimia.android.presentation.system.HideSystemNavigationBar
import theme.QuimiaTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.quimia.android.presentation.screens.kemi.KemiController
import com.quimia.android.presentation.screens.kemi.KemiControllerSaver
import com.quimia.android.presentation.screens.kemi.KemiScreen
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.quimia.android.presentation.screens.auth.AuthDestination
import com.quimia.android.data.project.*
import com.quimia.android.presentation.project.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.quimia.android.data.auth.GoogleSignInHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsLogger.logEvent("APP_STARTED")

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightNavigationBars = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val store = remember { ProfilePreferencesStore(this) }
            var preferences by remember { mutableStateOf(store.load()) }
            val dark = preferences.theme.usesDarkColors(isSystemInDarkTheme())
            val density = LocalDensity.current
            SideEffect {
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * preferences.textScale)) {
                QuimiaTheme(darkTheme = dark, highContrast = preferences.theme == ProfileTheme.HighContrast,
                    useDyslexicFont = preferences.dyslexicFont) {
                    HideSystemNavigationBar()
                    QuimiaApp(preferences) { preferences = it; store.save(it) }
                }
            }
        }
    }
}

@Composable
private fun QuimiaApp(preferences: ProfilePreferences, onPreferencesChange: (ProfilePreferences) -> Unit) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val project: ProjectViewModel = viewModel()
    val projectState by project.state.collectAsStateWithLifecycle()
    val authBusy by authViewModel.busy.collectAsStateWithLifecycle()
    val authError by authViewModel.message.collectAsStateWithLifecycle()
    val projectMessage by project.message.collectAsStateWithLifecycle()
    val requests = rememberCoroutineScope()
    var authRouteName by rememberSaveable { mutableStateOf(AuthDestination.Welcome.name) }
    val authDestination = AuthDestination.valueOf(authRouteName)
    val authScreens = rememberSaveableStateHolder()
    var recoveryEmail by rememberSaveable { mutableStateOf("") }
    var authNotice by rememberSaveable { mutableStateOf<String?>(null) }
    fun navigateAuth(destination: AuthDestination) { authRouteName = destination.name }
    var selectedMainTab by rememberSaveable { mutableStateOf(0) }
    val sessionUid = (authState as? AuthResult.Success)?.uid
    val sessionEmail = (authState as? AuthResult.Success)?.email.orEmpty()
    val context = LocalContext.current
    val client = remember(context) { ProjectClient.get(context) }
    val session by client.sessions.session.collectAsStateWithLifecycle()
    val displayName = projectState.profile?.nome ?: session?.user?.nome.orEmpty()
    val profileEmail = projectState.profile?.email ?: sessionEmail
    val photoUri = projectState.profile?.fotoUrl
    val birthDate = birthDateFromApi(projectState.profile?.dataNasc)
    val photoPainter = rememberAsyncImagePainter(photoUri)
    val photo = photoPainter.takeUnless { photoUri == null || it.state is AsyncImagePainter.State.Error }
    var showProfile by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(false) }
    val savedProductIds = projectState.shelf.map { it.id }
    val favoriteProductIds = emptyList<String>()
    val rooms = projectState.rooms
    var roomDraftProductId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var roomToOpenId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    val savedPointIds = emptyList<Int>()
    val referenceAddress = projectState.address?.fullAddress()
        ?: if ("address" in projectState.loaded) "Endereço não cadastrado" else "Endereço ainda não confirmado pela API"
    val referenceAddressFields = projectState.address?.toNearbyFields().orEmpty()
    var catalogQuery by rememberSaveable(sessionUid) { mutableStateOf("") }
    var showMixPicker by rememberSaveable(sessionUid) { mutableStateOf(false) }
    var mixSelection by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var showHistory by rememberSaveable(sessionUid) { mutableStateOf(false) }
    var historyProductId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var mixtureResult by remember { mutableStateOf<ApiMixture?>(null) }
    var showKemi by rememberSaveable(sessionUid) { mutableStateOf(false) }
    val kemiController = rememberSaveable(sessionUid, saver = KemiControllerSaver) { KemiController() }
    fun perform(operation: suspend () -> Unit) { requests.launch {
        try { operation() }
        catch (error: CancellationException) { throw error }
        catch (error: Exception) { authNotice = apiErrorMessage(error) }
    } }
    val google = remember(context) { GoogleSignInHelper(context) }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) requests.launch {
            val token = google.getIdTokenFromResult(result.data)
            if (token == null) authNotice = "O Google não retornou um token. Confira o cliente OAuth/Firebase do app."
            else authViewModel.loginWithGoogle(token)
        }
    }
    val onGoogle = {
        if (context.resources.getIdentifier("default_web_client_id", "string", context.packageName) == 0)
            authNotice = "O login Google exige configurar o cliente OAuth/Firebase deste app. A API recebe o ID token pelo endpoint /auth/firebase."
        else googleLauncher.launch(google.getSignInIntent())
    }
    val onMicrosoft = { authNotice = "O login Microsoft ainda não tem cliente OAuth configurado no app. A API não fornece uma rota direta de login Microsoft." }
    var unavailableTab by rememberSaveable { mutableStateOf<String?>(null) }
    val onMainTabSelected: (Int) -> Unit = { index ->
        if (index in 0..4) selectedMainTab = index
        else unavailableTab = listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja")
            .getOrElse(index) { "Esta tela" }
    }

    val currentScreen = when {
        authState is AuthResult.Success && showKemi -> "kemi"
        authState is AuthResult.Success && showProfile -> "profile"
        authState is AuthResult.Success -> when (selectedMainTab) { 1 -> "catalog"; 2 -> "shelf"; 3 -> "proximity"; 4 -> "rooms"; else -> "home" }
        else -> authDestination.screenName
    }

    LaunchedEffect(currentScreen) {
        AnalyticsLogger.logScreenView(currentScreen)
    }
    BackHandler(enabled = authState is AuthResult.Success && !showKemi && (showProfile || selectedMainTab in 2..4)) {
        if (showProfile) showProfile = false else selectedMainTab = 0
    }

    BackHandler(enabled = authState !is AuthResult.Success && authDestination != AuthDestination.Welcome) {
        navigateAuth(authDestination.back())
    }

    when (authState) {
        is AuthResult.Success -> {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().then(if (showKemi) Modifier.clearAndSetSemantics { } else Modifier)) {
                    if (showProfile) ProfileScreen(
                        displayName = displayName, email = profileEmail, preferences = preferences, birthDate = birthDate,
                        onPreferencesChange = onPreferencesChange, onBack = { showProfile = false }, photo = photo,
                        onDisplayNameChange = { name -> perform { project.updateProfileName(name) } },
                        onPhotoUriChange = { authNotice = ApiContractGaps.PhotoUpload },
                        onPhotoEditRequest = { authNotice = ApiContractGaps.PhotoUpload },
                        emailEditable = false, onSaveRemote = project::updateProfileDetails,
                        onLogout = {
                            showProfile = false; selectedMainTab = 0
                            navigateAuth(AuthDestination.Welcome)
                            AuthDestination.entries.forEach { authScreens.removeState(it.name) }
                            recoveryEmail = ""; authNotice = null
                            authViewModel.logout()
                        },
                    ) else when (selectedMainTab) {
                        1 -> CatalogScreen(
                            onMenuItemSelected = onMainTabSelected,
                            onAiClick = { showKemi = true },
                            initialSavedProductIds = savedProductIds,
                            products = projectState.products,
                            onSaveProducts = project::saveProducts,
                            onScannerUnavailable = { authNotice = ApiContractGaps.LabelScan },
                            onBarcodeSearch = project::lookupBarcode, initialQuery = catalogQuery,
                        )
                        2 -> ShelfScreen(
                            savedIds = savedProductIds,
                            onSavedIdsChange = {}, onSaveProducts = project::saveProducts,
                            favoriteIds = favoriteProductIds,
                            onFavoriteIdsChange = { authNotice = ApiContractGaps.Favorites },
                            onMenuItemSelected = onMainTabSelected,
                            onAddProduct = { selectedMainTab = 1 },
                            onAiClick = { showKemi = true },
                            onMixProduct = { mixSelection = listOf(it); showMixPicker = true },
                            onViewMixtures = { historyProductId = it; showHistory = true },
                            onCreateRoom = { roomToOpenId = null; roomDraftProductId = it; selectedMainTab = 4 },
                            rooms = rooms,
                            catalog = (projectState.products + projectState.shelf).distinctBy { it.id },
                            substanceItems = projectState.substances,
                            onUnsupportedAction = { authNotice = it }, loadProtection = project::productSafety,
                            onOpenProductRoom = { productId -> project.openProductRoom(productId,
                                onFound = { roomToOpenId = it; roomDraftProductId = null; selectedMainTab = 4 },
                                onMissing = { roomDraftProductId = productId; roomToOpenId = null; selectedMainTab = 4 }) },
                            onOpenRoom = { roomDraftProductId = null; roomToOpenId = it; selectedMainTab = 4 },
                        )
                        4 -> RoomsScreen(
                            rooms = rooms, onRoomsChange = {}, savedProductIds = savedProductIds,
                            catalog = (projectState.products + projectState.shelf).distinctBy { it.id },
                            onCreateRemote = project::createRoom, onRemoveRemote = project::removeRoomProduct,
                            onRequestError = { authNotice = it }, productRange = 2..20,
                            onMenuItemSelected = onMainTabSelected, onAiClick = { showKemi = true },
                            onOpenCatalog = { selectedMainTab = 1 }, initialProductId = roomDraftProductId,
                            onInitialProductConsumed = { roomDraftProductId = null },
                            initialRoomId = roomToOpenId,
                            onInitialRoomConsumed = { roomToOpenId = null },
                        )
                        3 -> ProximityScreen(
                            onMenuItemSelected = onMainTabSelected,
                            initialSavedPointIds = savedPointIds,
                            onSavedPointIdsChange = { authNotice = ApiContractGaps.SavedPoints },
                            initialAddress = referenceAddress,
                            initialAddressFields = referenceAddressFields,
                            apiPoints = projectState.points,
                            onSaveRemote = { project.updateAddressValue(it.toApiAddress()) },
                        )
                        else -> HomeScreen(
                            avatarInitials = profileInitials(displayName), avatarPhoto = photo,
                            onAvatarClick = { showProfile = true },
                            onMenuItemSelected = onMainTabSelected,
                            onFindDropOffClick = { selectedMainTab = 3 },
                            onShelfClick = { selectedMainTab = 2 },
                            onAiClick = { showKemi = true },
                            onSearch = { catalogQuery = it; selectedMainTab = 1 },
                            onMixClick = { mixSelection = emptyList(); showMixPicker = true },
                            onMixturesClick = { historyProductId = null; showHistory = true },
                            dropOffStatus = "Não disponível na API",
                            historyContent = { ApiHistoryContent(projectState.mixtures,
                                available = "history" in projectState.loaded && "history" !in projectState.errors) },
                        )
                    }
                }
                if (showKemi) {
                    KemiScreen(
                        controller = kemiController,
                        responder = project::replyToKemi, historyNotice = ApiContractGaps.ChatHistory,
                        displayName = displayName.takeUnless { it == "Seu nome" }?.substringBefore(' '),
                        onClose = { showKemi = false },
                        onHome = { showKemi = false; selectedMainTab = 0 },
                        onSignInAgain = {
                            showKemi = false; showProfile = false; selectedMainTab = 0
                            navigateAuth(AuthDestination.Login)
                            AuthDestination.entries.forEach { authScreens.removeState(it.name) }
                            recoveryEmail = ""; authNotice = null
                            authViewModel.logout()
                        },
                    )
                }
                unavailableTab?.let { name ->
                    AlertDialog(
                        onDismissRequest = { unavailableTab = null },
                        title = { Text(name) },
                        text = { Text("Esta tela ainda não está disponível.") },
                        confirmButton = { TextButton(onClick = { unavailableTab = null }) { Text("Entendi") } },
                    )
                }
            }
        }
        else -> {
            authScreens.SaveableStateProvider(authDestination.name) {
                when (authDestination) {
                    AuthDestination.Welcome -> LoginHomeScreen(onStart = { navigateAuth(AuthDestination.Login) })
                    AuthDestination.Login -> Login1Screen(
                        onContinue = { email, password -> authViewModel.loginWithEmail(email, password) },
                        onCreateAccount = { navigateAuth(AuthDestination.Registration) },
                        onForgotPassword = { navigateAuth(AuthDestination.RecoveryEmail) },
                        onGoogleClick = onGoogle, onMicrosoftClick = onMicrosoft,
                    )
                    AuthDestination.Registration -> Cadastro1Screen(
                        onContinue = { name, date, email, password ->
                            authViewModel.prepareRegistration(name, date, email, password) { navigateAuth(AuthDestination.Address) }
                        },
                        onLogin = { navigateAuth(AuthDestination.Login) },
                        onGoogleClick = onGoogle, onMicrosoftClick = onMicrosoft,
                    )
                    AuthDestination.Address -> Cadastro2Screen(
                        onBack = { navigateAuth(AuthDestination.Registration) },
                        onSkip = { authViewModel.finishRegistration() },
                        onEnter = { address ->
                            try { authViewModel.finishRegistration(address.toApiAddress()) }
                            catch (error: Exception) { authNotice = apiErrorMessage(error) }
                        },
                    )
                    AuthDestination.RecoveryEmail -> LoginPass1Screen(
                        onBack = { navigateAuth(AuthDestination.Login) },
                        onEmailContinue = { email ->
                            recoveryEmail = email
                            authViewModel.forgotPassword(email) { navigateAuth(AuthDestination.RecoveryCode) }
                        },
                    )
                    AuthDestination.RecoveryCode -> LoginPassCodeScreen(
                        email = recoveryEmail,
                        onBack = { navigateAuth(AuthDestination.RecoveryEmail) },
                        onResend = { authViewModel.forgotPassword(recoveryEmail) {} },
                        onConfirm = { code -> authViewModel.verifyRecoveryCode(code) { navigateAuth(AuthDestination.NewPassword) } },
                    )
                    AuthDestination.NewPassword -> LoginPass2Screen(
                        onBack = { navigateAuth(AuthDestination.RecoveryCode) },
                        onConfirm = { password -> authViewModel.resetPassword(password) { navigateAuth(AuthDestination.RecoverySuccess) } },
                    )
                    AuthDestination.RecoverySuccess -> LoginPassSuccesScreen(onStart = { navigateAuth(AuthDestination.Login) })
                }
            }
        }
    }
    if (showMixPicker && authState is AuthResult.Success) ApiMixturePicker(projectState.shelf, mixSelection,
        onSelection = { mixSelection = it }, onClose = { showMixPicker = false }, onConfirm = { perform {
            mixtureResult = project.mix(mixSelection[0], mixSelection[1]); showMixPicker = false
        } })
    if (showHistory && authState is AuthResult.Success) AlertDialog(onDismissRequest = { showHistory = false },
        title = { Text("Histórico de misturas") }, text = { ApiHistoryContent(projectState.mixtures.filter {
            historyProductId == null || it.produtoA.id.toString() == historyProductId || it.produtoB.id.toString() == historyProductId
        }, available = "history" in projectState.loaded && "history" !in projectState.errors) },
        confirmButton = { TextButton(onClick = { showHistory = false }) { Text("Fechar") } })
    mixtureResult?.let { result -> AlertDialog(onDismissRequest = { mixtureResult = null },
        title = { Text(result.produtoA.nome + " + " + result.produtoB.nome) },
        text = { Text(listOfNotNull(result.resultado, result.severidade, result.descricaoRisco).joinToString("\n\n")) },
        confirmButton = { TextButton(onClick = { mixtureResult = null }) { Text("Fechar") } }) }
    val notice = authNotice ?: authError ?: projectMessage
    fun clearNotice() { authNotice = null; authViewModel.message.value = null; project.message.value = null }
    notice?.let {
        AlertDialog(onDismissRequest = ::clearNotice, title = { Text("Quimia · API do projeto") },
            text = { Text(it) }, confirmButton = { TextButton(onClick = ::clearNotice) { Text("Entendi") } })
    }
    val section = if (showProfile) "profile" else when (selectedMainTab) {
        1 -> "catalog"; 2 -> "shelf"; 3 -> "points"; 4 -> "rooms"; else -> "history"
    }
    ApiRequestOverlay(busy = authBusy || projectState.pendingWrites > 0,
        loading = authState is AuthResult.Success && section in projectState.loading,
        error = if (authState is AuthResult.Success && !showKemi) projectState.errors[section] else null,
        onRetry = { project.retry(section) })
    BackHandler(enabled = authBusy || projectState.pendingWrites > 0) {}
}
