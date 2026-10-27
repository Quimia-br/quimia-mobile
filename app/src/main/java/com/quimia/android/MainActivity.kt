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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
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
import com.quimia.android.presentation.screens.mixtures.*
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import com.quimia.android.presentation.screens.shelf.sampleShelfSubstances
import kotlinx.coroutines.delay
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import com.quimia.android.data.auth.GoogleSignInHelper
import com.quimia.android.presentation.screens.auth.registration.GoogleDetailsScreen
import com.quimia.android.presentation.screens.proximity.NearbyAddress
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
    val googleDraft by authViewModel.googleOnboarding.collectAsStateWithLifecycle()
    val authBusy by authViewModel.busy.collectAsStateWithLifecycle()
    val authBusyLabel by authViewModel.busyLabel.collectAsStateWithLifecycle()
    val sessionProblem by authViewModel.sessionProblem.collectAsStateWithLifecycle()
    val nextAuthDestination by authViewModel.nextDestination.collectAsStateWithLifecycle()
    val recoveryEmail by authViewModel.recoveryEmail.collectAsStateWithLifecycle()
    val recoveryCodeError by authViewModel.recoveryCodeError.collectAsStateWithLifecycle()
    val recoveryRevision by authViewModel.recoveryRevision.collectAsStateWithLifecycle()
    val authMessage by authViewModel.message.collectAsStateWithLifecycle()
    var choosingGoogle by remember { mutableStateOf(false) }
    var authRouteName by rememberSaveable { mutableStateOf(AuthDestination.Welcome.name) }
    val authDestination = AuthDestination.valueOf(authRouteName)
    val authScreens = rememberSaveableStateHolder()
    var authNotice by rememberSaveable { mutableStateOf<String?>(null) }
    fun navigateAuth(destination: AuthDestination) { authRouteName = destination.name }
    var selectedMainTab by rememberSaveable { mutableStateOf(0) }
    val sessionUid = (authState as? AuthResult.Success)?.uid
    val sessionEmail = (authState as? AuthResult.Success)?.email.orEmpty()
    val context = LocalContext.current
    val authScope = rememberCoroutineScope()
    val google = remember(context) { GoogleSignInHelper(context) }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        choosingGoogle = false
        if (result.resultCode == Activity.RESULT_OK || result.data != null) {
            try {
                val token = google.accountFromResult(result.data)?.idToken
                    ?: error("Não foi possível confirmar sua conta Google. Tente novamente.")
                authViewModel.loginWithGoogle(token)
            } catch (error: com.google.android.gms.common.api.ApiException) {
                if (error.statusCode != com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                    authNotice = "Não foi possível entrar com Google. Tente novamente. Código: ${error.statusCode}."
                }
            } catch (error: Exception) { authNotice = error.message ?: "Não foi possível entrar com Google. Tente novamente." }
        }
    }
    val onGoogle: () -> Unit = {
        if (!choosingGoogle && !authBusy) {
            choosingGoogle = true
            authScope.launch {
                try { googleLauncher.launch(google.chooseAccountIntent())
                } catch (cancelled: CancellationException) { choosingGoogle = false; throw cancelled
                } catch (error: Exception) { choosingGoogle = false; authNotice = error.message ?: "Não foi possível abrir o acesso Google." }
            }
        }
    }
    LaunchedEffect(googleDraft?.uid, googleDraft?.addressStep) {
        googleDraft?.let { navigateAuth(it.destination) }
    }
    LaunchedEffect(nextAuthDestination) {
        nextAuthDestination?.let { navigateAuth(it); authViewModel.nextDestination.value = null }
    }
    LaunchedEffect(authDestination, authBusy, googleDraft?.uid, nextAuthDestination) {
        if (!authBusy && nextAuthDestination == null && authState !is AuthResult.Success) when {
            authDestination == AuthDestination.Address && !authViewModel.hasRegistration -> navigateAuth(AuthDestination.Registration)
            authDestination == AuthDestination.RecoveryCode && !authViewModel.hasRecoveryChallenge -> navigateAuth(AuthDestination.RecoveryEmail)
            authDestination == AuthDestination.NewPassword && !authViewModel.hasRecoveryGrant -> navigateAuth(AuthDestination.RecoveryEmail)
            authDestination in listOf(AuthDestination.GoogleDetails, AuthDestination.GoogleAddress) && googleDraft == null -> navigateAuth(AuthDestination.Login)
            else -> Unit
        }
    }
    LaunchedEffect(authMessage) {
        authMessage?.let { authNotice = it; authViewModel.message.value = null }
    }
    val profileStore = remember(sessionUid, sessionEmail) { LocalProfileStore(context, sessionUid.orEmpty(), sessionEmail) }
    var displayName by rememberSaveable(sessionUid, sessionEmail) {
        mutableStateOf(profileStore.displayName() ?: authViewModel.accountName ?: sessionEmail.substringBefore('@').ifBlank { "Seu nome" })
    }
    var photoUri by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(profileStore.photoUri() ?: authViewModel.providerPhotoUrl) }
    var profileEmail by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(profileStore.email() ?: sessionEmail) }
    var birthDate by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(profileStore.birthDate().ifBlank { authViewModel.accountBirthDate }) }
    val photoPainter = rememberAsyncImagePainter(photoUri)
    val photo = photoPainter.takeUnless { photoUri == null || it.state is AsyncImagePainter.State.Error }
    var showProfile by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(false) }
    var savedProductIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var favoriteProductIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var rooms by rememberSaveable(sessionUid, stateSaver = RoomListSaver) { mutableStateOf(emptyList<QuimiaRoom>()) }
    var roomDraftProductId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var roomToOpenId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var savedPointIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<Int>()) }
    val storedGoogleAddress = remember(sessionUid, sessionEmail) { authViewModel.savedAddress(sessionUid.orEmpty(), sessionEmail) }
    var referenceAddress by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(storedGoogleAddress.takeIf { it.size == 7 }?.let {
        NearbyAddress(it[0], it[1], it[2], it[3], it[4], it[5], it[6]).formatted()
    } ?: "Defina seu endereço de referência") }
    var referenceAddressFields by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(storedGoogleAddress) }
    var showKemi by rememberSaveable(sessionUid) { mutableStateOf(false) }
    var showMixtures by rememberSaveable(sessionUid) { mutableStateOf(false) }
    var openMixtureHistory by rememberSaveable(sessionUid) { mutableStateOf(false) }
    var mixtureHistoryFilter by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var demoMixtureIndex by rememberSaveable(sessionUid) { mutableStateOf(0) }
    val mixtureStore = remember(sessionUid, sessionEmail) { MixtureHistoryStore(context, sessionUid.orEmpty(), sessionEmail) }
    val mixtures = rememberSaveable(sessionUid, sessionEmail, saver = MixtureControllerSaver) {
        MixtureController(MixtureState(history = mixtureStore.load()))
    }
    LaunchedEffect(mixtures.state.history) { if (sessionUid != null) mixtureStore.save(mixtures.state.history) }
    fun openMixtures(id: String? = null, history: Boolean = false) {
        mixtures.cancel(); mixtures.closeResult()
        val ingredient = sampleCatalogProducts.firstOrNull { it.id == id }?.mixtureIngredient()
            ?: sampleShelfSubstances.firstOrNull { it.id == id }?.let { MixtureIngredient(it.id, it.name, MixtureIngredientKind.Substance) }
        if (ingredient != null && !history) mixtures.startWith(ingredient)
        mixtureHistoryFilter = if (history) id else null
        openMixtureHistory = history
        showMixtures = true
    }
    val kemiController = rememberSaveable(sessionUid, saver = KemiControllerSaver) { KemiController() }
    var unavailableTab by rememberSaveable { mutableStateOf<String?>(null) }
    val onMainTabSelected: (Int) -> Unit = { index ->
        if (index in 0..4) selectedMainTab = index
        else unavailableTab = listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja")
            .getOrElse(index) { "Esta tela" }
    }

    val currentScreen = when {
        authState is AuthResult.Success && showMixtures -> "mixtures"
        authState is AuthResult.Success && showKemi -> "kemi"
        authState is AuthResult.Success && showProfile -> "profile"
        authState is AuthResult.Success -> when (selectedMainTab) { 1 -> "catalog"; 2 -> "shelf"; 3 -> "proximity"; 4 -> "rooms"; else -> "home" }
        else -> authDestination.screenName
    }

    LaunchedEffect(currentScreen) {
        AnalyticsLogger.logScreenView(currentScreen)
    }
    BackHandler(enabled = authState is AuthResult.Success && !showKemi && !showMixtures && (showProfile || selectedMainTab in 2..4)) {
        if (showProfile) showProfile = false else selectedMainTab = 0
    }

    BackHandler(enabled = authState !is AuthResult.Success && authDestination != AuthDestination.Welcome) {
        if (!authBusy && !choosingGoogle) when (authDestination) {
            AuthDestination.GoogleDetails -> { authViewModel.cancelGoogle(); navigateAuth(AuthDestination.Login) }
            AuthDestination.GoogleAddress -> authViewModel.backGoogle()
            else -> navigateAuth(authDestination.back())
        }
    }

    LaunchedEffect(sessionUid) {
        if (sessionUid != null) {
            selectedMainTab = 0
            AuthDestination.entries.forEach { authScreens.removeState(it.name) }
            authScreens.removeState(AuthDestination.GoogleDetails.name + sessionUid)
            authScreens.removeState(AuthDestination.GoogleAddress.name + sessionUid)
        }
    }

    when (authState) {
        is AuthResult.Success -> {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().then(if (showKemi || showMixtures) Modifier.clearAndSetSemantics { } else Modifier)) {
                    if (showProfile) ProfileScreen(
                        displayName = displayName, email = profileEmail, preferences = preferences, birthDate = birthDate,
                        onPreferencesChange = onPreferencesChange, onBack = { showProfile = false }, photo = photo,
                        onDisplayNameChange = { displayName = it; profileStore.saveDisplayName(it) },
                        onPhotoUriChange = { photoUri = it; profileStore.savePhotoUri(it) },
                        onDetailsChange = { details ->
                            profileStore.saveDetails(details)
                            displayName = details.name; profileEmail = details.email; birthDate = details.birthDate
                        },
                        onLogout = {
                            showProfile = false; showMixtures = false; selectedMainTab = 0
                            navigateAuth(AuthDestination.Welcome)
                            AuthDestination.entries.forEach { authScreens.removeState(it.name) }
                            authNotice = null
                            authViewModel.logout()
                        },
                    ) else when (selectedMainTab) {
                        1 -> CatalogScreen(
                            onMenuItemSelected = onMainTabSelected,
                            onAiClick = { showKemi = true },
                            initialSavedProductIds = savedProductIds,
                            onSavedProductIdsChange = { savedProductIds = it },
                        )
                        2 -> ShelfScreen(
                            savedIds = savedProductIds,
                            onSavedIdsChange = { savedProductIds = it },
                            favoriteIds = favoriteProductIds,
                            onFavoriteIdsChange = { favoriteProductIds = it },
                            onMenuItemSelected = onMainTabSelected,
                            onAddProduct = { selectedMainTab = 1 },
                            onAiClick = { showKemi = true },
                            onMixProduct = { openMixtures(it) },
                            onViewMixtures = { openMixtures(it, history = true) },
                            onCreateRoom = { roomToOpenId = null; roomDraftProductId = it; selectedMainTab = 4 },
                            rooms = rooms,
                            onOpenRoom = { roomDraftProductId = null; roomToOpenId = it; selectedMainTab = 4 },
                        )
                        4 -> RoomsScreen(
                            rooms = rooms, onRoomsChange = { rooms = it }, savedProductIds = savedProductIds,
                            onMenuItemSelected = onMainTabSelected, onAiClick = { showKemi = true },
                            onOpenCatalog = { selectedMainTab = 1 }, initialProductId = roomDraftProductId,
                            onInitialProductConsumed = { roomDraftProductId = null },
                            initialRoomId = roomToOpenId,
                            onInitialRoomConsumed = { roomToOpenId = null },
                        )
                        3 -> ProximityScreen(
                            onMenuItemSelected = onMainTabSelected,
                            initialSavedPointIds = savedPointIds,
                            onSavedPointIdsChange = { savedPointIds = it },
                            initialAddress = referenceAddress,
                            onAddressChange = { referenceAddress = it },
                            initialAddressFields = referenceAddressFields,
                            onAddressFieldsChange = { referenceAddressFields = it },
                        )
                        else -> HomeScreen(
                            avatarInitials = profileInitials(displayName), avatarPhoto = photo,
                            onAvatarClick = { showProfile = true },
                            onMenuItemSelected = onMainTabSelected,
                            onFindDropOffClick = { selectedMainTab = 3 },
                            onShelfClick = { selectedMainTab = 2 },
                            onAiClick = { showKemi = true },
                            onMixturesClick = { openMixtures() }, onMixClick = { openMixtures() },
                            mixtureHistory = mixtures.state.history,
                            onMixtureHistoryClick = { result -> openMixtures(history = true); mixtures.openResult(result.id) },
                        )
                    }
                }
                if (showMixtures) MixturesScreen(
                    controller = mixtures, savedProductIds = savedProductIds,
                    onClose = { showMixtures = false },
                    onOpenCatalog = { showMixtures = false; selectedMainTab = 1 },
                    initialHistory = openMixtureHistory, historyProductId = mixtureHistoryFilter,
                    demonstration = true,
                    analyzer = { request ->
                        delay(800)
                        val scenarios = listOf(MixtureRisk.Dangerous, MixtureRisk.Safe, MixtureRisk.Caution, MixtureRisk.Extreme)
                        val risk = scenarios[demoMixtureIndex.mod(scenarios.size)]
                        demoMixtureIndex = (demoMixtureIndex + 1).mod(scenarios.size)
                        MixtureReply.Evaluated(MixtureResult(request.id, request.ingredients, risk, System.currentTimeMillis(), demonstration = true))
                    },
                )
                if (showKemi) {
                    KemiScreen(
                        controller = kemiController,
                        displayName = displayName.takeUnless { it == "Seu nome" }?.substringBefore(' '),
                        onClose = { showKemi = false },
                        onHome = { showKemi = false; selectedMainTab = 0 },
                        onSignInAgain = {
                            showKemi = false; showProfile = false; selectedMainTab = 0
                            navigateAuth(AuthDestination.Login)
                            AuthDestination.entries.forEach { authScreens.removeState(it.name) }
                            authNotice = null
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
            authScreens.SaveableStateProvider(authDestination.name + if (authDestination == AuthDestination.GoogleDetails || authDestination == AuthDestination.GoogleAddress) googleDraft?.uid.orEmpty() else "") {
                when (authDestination) {
                    AuthDestination.Welcome -> LoginHomeScreen(onStart = { navigateAuth(AuthDestination.Login) })
                    AuthDestination.Login -> Login1Screen(
                        onContinue = { email, password -> if (!choosingGoogle && !authBusy) authViewModel.loginWithEmail(email, password) },
                        onCreateAccount = { navigateAuth(AuthDestination.Registration) },
                        onForgotPassword = { navigateAuth(AuthDestination.RecoveryEmail) },
                        onGoogleClick = onGoogle,
                    )
                    AuthDestination.Registration -> Cadastro1Screen(
                        onDetailsContinue = authViewModel::prepareRegistration,
                        onLogin = { navigateAuth(AuthDestination.Login) },
                        onGoogleClick = onGoogle,
                    )
                    AuthDestination.Address -> Cadastro2Screen(
                        onBack = { navigateAuth(AuthDestination.Registration) },
                        onSkip = { authViewModel.finishRegistration() },
                        onAddressContinue = { authViewModel.finishRegistration(it) },
                    )
                    AuthDestination.GoogleDetails -> googleDraft?.let { draft -> GoogleDetailsScreen(
                        profile = draft, onChange = authViewModel::editGoogle, onContinue = authViewModel::continueGoogle,
                        onLogin = { authViewModel.cancelGoogle(); navigateAuth(AuthDestination.Login) },
                    ) }
                    AuthDestination.GoogleAddress -> Cadastro2Screen(
                        onBack = authViewModel::backGoogle, onSkip = { authViewModel.finishGoogle() },
                        onAddressContinue = { authViewModel.finishGoogle(it) },
                    )
                    AuthDestination.RecoveryEmail -> LoginPass1Screen(
                        onBack = { navigateAuth(AuthDestination.Login) },
                        onEmailContinue = authViewModel::forgotPassword,
                    )
                    AuthDestination.RecoveryCode -> LoginPassCodeScreen(
                        email = recoveryEmail,
                        onBack = { navigateAuth(AuthDestination.RecoveryEmail) },
                        onResend = authViewModel::resendRecoveryCode,
                        onConfirm = authViewModel::verifyRecoveryCode,
                        error = recoveryCodeError, onCodeChange = authViewModel::clearRecoveryCodeError,
                        resendRevision = recoveryRevision,
                    )
                    AuthDestination.NewPassword -> LoginPass2Screen(
                        onBack = { navigateAuth(AuthDestination.RecoveryCode) },
                        onPasswordConfirm = authViewModel::resetPassword,
                    )
                    AuthDestination.RecoverySuccess -> LoginPassSuccesScreen(onStart = { navigateAuth(AuthDestination.Login) })
                }
            }
        }
    }
    if (authBusy || choosingGoogle) androidx.compose.material3.AlertDialog(
        onDismissRequest = {}, confirmButton = {},
        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        text = { androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
            androidx.compose.material3.CircularProgressIndicator(Modifier.size(24.dp))
            Text(if (choosingGoogle) "Abrindo acesso Google…" else authBusyLabel)
        } },
    )
    if (!authBusy && sessionProblem != null) AlertDialog(
        onDismissRequest = {}, title = { Text("Não foi possível conferir sua sessão") },
        text = { Text(sessionProblem.orEmpty()) },
        confirmButton = { TextButton(onClick = authViewModel::retrySession) { Text("Tentar novamente") } },
        dismissButton = { TextButton(onClick = { navigateAuth(AuthDestination.Login); authViewModel.logout() }) { Text("Ir para login") } },
    )
    authNotice?.let { notice ->
        AlertDialog(onDismissRequest = { authNotice = null }, title = { Text("Autenticação") },
            text = { Text(notice) }, confirmButton = { TextButton(onClick = { authNotice = null }) { Text("Entendi") } })
    }
}
