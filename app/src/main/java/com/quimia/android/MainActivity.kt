package com.quimia.android

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding

import motion.QuimiaScreenDissolve
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Alignment
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
import com.quimia.android.presentation.screens.home.HomeRoute
import com.quimia.android.presentation.screens.catalog.CatalogRoute
import com.quimia.android.presentation.screens.proximity.ProximityRoute
import com.quimia.android.presentation.screens.shelf.ShelfRoute
import com.quimia.android.presentation.screens.rooms.RoomsRoute
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
import com.quimia.android.data.notifications.NotificationOpenRequest
import com.quimia.android.data.notifications.notificationOpenRequest
import com.quimia.android.data.notifications.NOTIFICATION_OWNER
import com.quimia.android.data.notifications.NOTIFICATION_DESTINATION
import com.quimia.android.domain.notifications.NotificationDestination

class MainActivity : ComponentActivity() {
    private var notificationRequest by mutableStateOf<NotificationOpenRequest?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationRequest = notificationOpenRequest(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationRequest = notificationOpenRequest(intent)
        volumeControlStream = android.media.AudioManager.STREAM_MUSIC
        AnalyticsLogger.logEvent("APP_STARTED")

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightNavigationBars = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val preferencesFactory = remember { ProfilePreferencesViewModel.factory(applicationContext) }
            val preferencesViewModel: ProfilePreferencesViewModel = viewModel(factory = preferencesFactory)
            val settings by preferencesViewModel.state.collectAsStateWithLifecycle()
            val preferences = settings.preferences
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
                    com.quimia.android.presentation.system.QuimiaPerformance {
                    com.quimia.android.presentation.system.QuimiaSoundProvider(preferences.soundEffects) {
                        com.quimia.android.presentation.components.LaunchSplash {
                            if (settings.ready) {
                                QuimiaApp(preferences, preferencesViewModel::update, notificationRequest) {
                                    notificationRequest = null
                                    intent.removeExtra(NOTIFICATION_OWNER)
                                    intent.removeExtra(NOTIFICATION_DESTINATION)
                                }
                            } else if (settings.error != null) {
                                com.quimia.android.presentation.screens.errors.GenericErrorScreen(
                                    kind = com.quimia.android.presentation.screens.errors.GenericErrorKind.Unknown,
                                    description = settings.error.orEmpty(), onClose = ::finish,
                                    onAction = { preferencesViewModel.reload() },
                                )
                            }
                        }
                        if (settings.ready && settings.error != null) {
                            com.quimia.android.presentation.components.QuimiaFeedbackDialog(
                                title = "Não foi possível salvar suas preferências", description = settings.error,
                                kind = com.quimia.android.presentation.components.QuimiaFeedbackKind.Error,
                                onDismiss = preferencesViewModel::dismissError, action = "Tentar novamente",
                                onConfirm = preferencesViewModel::retrySave, secondaryAction = "Fechar")
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuimiaApp(preferences: ProfilePreferences, onPreferencesChange: (ProfilePreferences) -> Unit,
    notificationRequest: NotificationOpenRequest? = null, onNotificationHandled: () -> Unit = {}) {
    com.quimia.android.presentation.system.NotificationEffects(preferences)
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
    LaunchedEffect(sessionUid, authViewModel.providerPhotoUrl) {
        sessionUid?.let { com.quimia.android.presentation.components.prefetchProfilePhoto(context, it, authViewModel.providerPhotoUrl) }
    }
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
                    authNotice = com.quimia.android.data.auth.googleSignInErrorMessage(error.statusCode)
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
        mutableStateOf(authViewModel.accountName ?: sessionEmail.substringBefore('@').ifBlank { "Seu nome" })
    }
    var photoUri by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(authViewModel.providerPhotoUrl) }
    var profileEmail by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(sessionEmail) }
    var birthDate by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(authViewModel.accountBirthDate) }
    val photoPainter = rememberAsyncImagePainter(photoUri)
    val photo = photoPainter.takeUnless { photoUri == null || it.state is AsyncImagePainter.State.Error }
    var showProfile by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(false) }
    var homeSearch by rememberSaveable(sessionUid) { mutableStateOf("") }
    var favoriteProductIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
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
    var mixtureNavigationKey by rememberSaveable(sessionUid) { mutableStateOf(0) }
    var openMixtureHistory by rememberSaveable(sessionUid) { mutableStateOf(false) }
    var mixtureHistoryFilter by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var initialMixtureProductId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var homeMixtureRevision by rememberSaveable(sessionUid) { mutableStateOf(0) }
    fun openMixtures(id: String? = null, history: Boolean = false) {
        initialMixtureProductId = if (history) null else id
        mixtureHistoryFilter = if (history) id else null
        openMixtureHistory = history
        showMixtures = true
    }
    val kemiController = rememberSaveable(sessionUid, saver = KemiControllerSaver) { KemiController() }
    var unavailableTab by rememberSaveable { mutableStateOf<String?>(null) }
    val onMainTabSelected: (Int) -> Unit = { index ->
        homeSearch = ""
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
    BackHandler(enabled = authState is AuthResult.Success && !showKemi && !showMixtures && (showProfile || selectedMainTab in 1..4)) {
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

    LaunchedEffect(notificationRequest, sessionUid) {
        if (notificationRequest != null && sessionUid != null) {
            if (notificationRequest.ownerId == sessionUid) {
                showProfile = false; showKemi = false; showMixtures = false
                when (notificationRequest.destination) {
                    NotificationDestination.Home -> selectedMainTab = 0
                    NotificationDestination.Shelf -> selectedMainTab = 2
                    NotificationDestination.MixtureHistory -> { mixtureNavigationKey++; openMixtures(history = true) }
                }
            }
            onNotificationHandled()
        }
    }

    val enteringAccount = authBusy && authDestination in setOf(AuthDestination.Login, AuthDestination.Address,
        AuthDestination.GoogleDetails, AuthDestination.GoogleAddress)
    com.quimia.android.presentation.components.DestinationLoadingHost(
        destination = currentScreen, userId = sessionUid,
        accountLoading = authState is AuthResult.Loading || enteringAccount,
        navigationVisible = authState is AuthResult.Success && !showProfile && !showKemi && !showMixtures && selectedMainTab in setOf(0, 1, 2, 4),
    ) { brandedLoading ->
    when (authState) {
        is AuthResult.Success -> motion.QuimiaNavigationMotion(selectedMainTab) {
            Box(Modifier.fillMaxSize()) {
                QuimiaScreenDissolve(
                    target = if (showProfile) -1 else selectedMainTab,
                    modifier = Modifier.fillMaxSize().then(if (showKemi || showMixtures) Modifier.clearAndSetSemantics { } else Modifier),
                    hasNavigation = { it in setOf(0, 1, 2, 4) },
                    navigation = { tab -> MainNavigation(tab, onMainTabSelected, onAiClick = { showKemi = true }) },
                ) { destination ->
                    if (destination == -1) ProfileRoute(
                        userId = sessionUid.orEmpty(), preferences = preferences,
                        onPreferencesChange = onPreferencesChange, onBack = { showProfile = false },
                        onUpdated = { updated ->
                            displayName = updated.nome; profileEmail = updated.email
                            photoUri = updated.fotoUrl
                        },
                        onLogout = {
                            showProfile = false; showMixtures = false; selectedMainTab = 0
                            navigateAuth(AuthDestination.Login); authViewModel.logout()
                        },
                    ) else when (destination) {
                        1 -> CatalogRoute(
                            userId = sessionUid.orEmpty(),
                            initialQuery = homeSearch,
                            onMenuItemSelected = onMainTabSelected,
                            onAiClick = { showKemi = true },
                            onSessionExpired = {
                                selectedMainTab = 0; showProfile = false; showMixtures = false
                                navigateAuth(AuthDestination.Login); authViewModel.logout()
                                authNotice = "Sua sessão expirou. Entre novamente para continuar."
                            },
                        )
                        2 -> ShelfRoute(
                            userId = sessionUid.orEmpty(),
                            onMenuItemSelected = onMainTabSelected,
                            onAddProduct = { selectedMainTab = 1 }, onMixProduct = { openMixtures(it) },
                            onAiClick = { showKemi = true },
                            onCreateRoom = { roomToOpenId = null; roomDraftProductId = it; selectedMainTab = 4 },
                            onOpenRoom = { roomDraftProductId = null; roomToOpenId = it; selectedMainTab = 4 },
                            onSessionExpired = {
                                selectedMainTab = 0; showProfile = false; showMixtures = false
                                navigateAuth(AuthDestination.Login); authViewModel.logout()
                                authNotice = "Sua sessão expirou. Entre novamente para continuar."
                            },
                        )
                        4 -> RoomsRoute(
                            userId = sessionUid.orEmpty(),
                            onMenuItemSelected = onMainTabSelected, onAiClick = { showKemi = true },
                            onOpenCatalog = { selectedMainTab = 1 }, initialProductId = roomDraftProductId,
                            initialRoomId = roomToOpenId,
                            onInitialConsumed = { roomDraftProductId = null; roomToOpenId = null },
                            onSessionExpired = {
                                selectedMainTab = 0; showProfile = false; showMixtures = false
                                navigateAuth(AuthDestination.Login); authViewModel.logout()
                                authNotice = "Sua sessão expirou. Entre novamente para continuar."
                            },
                        )
                        3 -> ProximityRoute(
                            userId = sessionUid.orEmpty(), onMenuItemSelected = onMainTabSelected,
                            onSessionExpired = { navigateAuth(AuthDestination.Login); authViewModel.logout() },
                        )
                        else -> HomeRoute(
                            userId = sessionUid.orEmpty(),
                            onSearch = { homeSearch = it.trim(); selectedMainTab = 1 },
                            onAvatarClick = { showProfile = true },
                            onMenuItemSelected = onMainTabSelected,
                            onFindDropOffClick = { selectedMainTab = 3 },
                            onShelfClick = { selectedMainTab = 2 },
                            onMixturesClick = { openMixtures() }, onAiClick = { showKemi = true }, mixtureRevision = homeMixtureRevision,
                            onProfileLoaded = { profile ->
                                displayName = profile.nome; profileEmail = profile.email; photoUri = profile.fotoUrl
                                birthDate = com.quimia.android.presentation.auth.displayBirthDate(profile.dataNasc)
                            },
                            onSessionExpired = {
                                selectedMainTab = 0; showProfile = false; showMixtures = false
                                navigateAuth(AuthDestination.Login); authViewModel.logout()
                                authNotice = "Sua sessão expirou. Entre novamente para continuar."
                            },
                        )
                    }
                }
                QuimiaScreenDissolve(
                    target = when { showKemi -> "kemi"; showMixtures -> "mixtures"; else -> "none" },
                    modifier = Modifier.fillMaxSize(),
                ) { overlay ->
                    if (overlay == "mixtures") androidx.compose.runtime.key(mixtureNavigationKey) { MixturesRoute(
                        userId = sessionUid.orEmpty(), initialProductId = initialMixtureProductId,
                        initialHistory = openMixtureHistory, historyProductId = mixtureHistoryFilter,
                        notificationsEnabled = preferences.mixtureNotifications,
                        onClose = { showMixtures = false }, onRecorded = { homeMixtureRevision++ },
                        onOpenCatalog = { showMixtures = false; selectedMainTab = 1 },
                        onSessionExpired = {
                            showMixtures = false; showProfile = false; selectedMainTab = 0
                            navigateAuth(AuthDestination.Login); authViewModel.logout()
                        },
                    ) }
                    if (overlay == "kemi") {
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
                }
                unavailableTab?.let { name ->
                    com.quimia.android.presentation.components.QuimiaFeedbackDialog(
                        title = name, description = "Esta tela ainda não está disponível.", onDismiss = { unavailableTab = null })
                }
            }
        }
        else -> {
            QuimiaScreenDissolve(authDestination, Modifier.fillMaxSize()) { destination ->
                authScreens.SaveableStateProvider(destination.name + if (destination == AuthDestination.GoogleDetails || destination == AuthDestination.GoogleAddress) googleDraft?.uid.orEmpty() else "") {
                    when (destination) {
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
    }
    androidx.activity.compose.BackHandler(enabled = authBusy || choosingGoogle) { }
    if ((authBusy || choosingGoogle) && !brandedLoading) androidx.compose.ui.window.Dialog(
        onDismissRequest = {}, properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        com.quimia.android.presentation.system.HideSystemNavigationBar()
        val progressWindow = (androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        SideEffect { progressWindow?.setDimAmount(0f) }
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
            toast.QuimiaToast(message = if (choosingGoogle) "Abrindo acesso Google…" else authBusyLabel,
                type = toast.QuimiaToastType.Loading)
        }
    }
    if (!authBusy && sessionProblem != null) com.quimia.android.presentation.components.QuimiaFeedbackDialog(
        title = "Não foi possível conferir sua sessão", description = sessionProblem,
        kind = com.quimia.android.presentation.components.QuimiaFeedbackKind.Error,
        onDismiss = {}, dismissible = false, action = "Tentar novamente", onConfirm = authViewModel::retrySession,
        secondaryAction = "Ir para login", onSecondary = { navigateAuth(AuthDestination.Login); authViewModel.logout() },
    )
    authNotice?.let { notice ->
        com.quimia.android.presentation.components.QuimiaFeedbackDialog(title = "Não foi possível continuar",
            description = notice, kind = com.quimia.android.presentation.components.QuimiaFeedbackKind.Error,
            onDismiss = { authNotice = null })
    }
    }
}

/** Keeps the menu opaque while the two destination bodies dissolve behind it. */
@Composable
private fun MainNavigation(tab: Int, onSelected: (Int) -> Unit, onAiClick: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        menuBar.QuimiaMenuBarBackdrop(Modifier.align(Alignment.BottomCenter))
        menuBar.QuimiaMenuBar(
            modifier = Modifier.align(Alignment.BottomCenter)
                .widthIn(max = if (tab == 2 || tab == 4) 840.dp else 600.dp).fillMaxWidth()
                .navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = menuBar.QuimiaMenuBarBottomSpacing),
            selectedIndex = tab, onItemSelected = onSelected, fabSpacing = if (tab == 2) 25.dp else 24.dp,
            fab = { buttons.QuimiaFAB(
                modifier = if (tab == 1) Modifier.padding(end = 5.dp) else Modifier,
                onClick = onAiClick, contentDescription = "Abrir assistente de IA",
            ) },
        )
    }
}
