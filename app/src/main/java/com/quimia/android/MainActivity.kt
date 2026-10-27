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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.quimia.android.presentation.screens.auth.AuthDestination

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
    var authRouteName by rememberSaveable { mutableStateOf(AuthDestination.Welcome.name) }
    val authDestination = AuthDestination.valueOf(authRouteName)
    val authScreens = rememberSaveableStateHolder()
    var recoveryEmail by rememberSaveable { mutableStateOf("") }
    var authNotice by rememberSaveable { mutableStateOf<String?>(null) }
    fun navigateAuth(destination: AuthDestination) { authRouteName = destination.name }
    val registrationUnavailable = {
        authNotice = "O cadastro ainda é visual nesta branch. Nenhuma conta ou endereço foi enviado. A integração de autenticação permanece separada."
    }
    var selectedMainTab by rememberSaveable { mutableStateOf(0) }
    val sessionUid = (authState as? AuthResult.Success)?.uid
    val sessionEmail = (authState as? AuthResult.Success)?.email.orEmpty()
    val context = LocalContext.current
    val profileStore = remember(sessionUid, sessionEmail) { LocalProfileStore(context, sessionUid.orEmpty(), sessionEmail) }
    var displayName by rememberSaveable(sessionUid, sessionEmail) {
        mutableStateOf(profileStore.displayName() ?: sessionEmail.substringBefore('@').ifBlank { "Seu nome" })
    }
    var photoUri by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(profileStore.photoUri()) }
    var profileEmail by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(profileStore.email() ?: sessionEmail) }
    var birthDate by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(profileStore.birthDate()) }
    val photoPainter = rememberAsyncImagePainter(photoUri)
    val photo = photoPainter.takeUnless { photoUri == null || it.state is AsyncImagePainter.State.Error }
    var showProfile by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(false) }
    var savedProductIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var favoriteProductIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var rooms by rememberSaveable(sessionUid, stateSaver = RoomListSaver) { mutableStateOf(emptyList<QuimiaRoom>()) }
    var roomDraftProductId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var roomToOpenId by rememberSaveable(sessionUid) { mutableStateOf<String?>(null) }
    var savedPointIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<Int>()) }
    var referenceAddress by rememberSaveable(sessionUid) { mutableStateOf("Defina seu endereço de referência") }
    var referenceAddressFields by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var showAiNotice by rememberSaveable { mutableStateOf(false) }
    var unavailableTab by rememberSaveable { mutableStateOf<String?>(null) }
    val onMainTabSelected: (Int) -> Unit = { index ->
        if (index in 0..4) selectedMainTab = index
        else unavailableTab = listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja")
            .getOrElse(index) { "Esta tela" }
    }

    val currentScreen = when {
        authState is AuthResult.Success && showProfile -> "profile"
        authState is AuthResult.Success -> when (selectedMainTab) { 1 -> "catalog"; 2 -> "shelf"; 3 -> "proximity"; 4 -> "rooms"; else -> "home" }
        else -> authDestination.screenName
    }

    LaunchedEffect(currentScreen) {
        AnalyticsLogger.logScreenView(currentScreen)
    }
    BackHandler(enabled = authState is AuthResult.Success && (showProfile || selectedMainTab in 2..4)) {
        if (showProfile) showProfile = false else selectedMainTab = 0
    }

    BackHandler(enabled = authState !is AuthResult.Success && authDestination != AuthDestination.Welcome) {
        navigateAuth(authDestination.back())
    }

    when (authState) {
        is AuthResult.Success -> {
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
                    showProfile = false; selectedMainTab = 0
                    navigateAuth(AuthDestination.Welcome)
                    AuthDestination.entries.forEach { authScreens.removeState(it.name) }
                    recoveryEmail = ""; authNotice = null
                    authViewModel.logout()
                },
            ) else when (selectedMainTab) {
                1 -> CatalogScreen(
                    onMenuItemSelected = onMainTabSelected,
                    onAiClick = { showAiNotice = true },
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
                    onAiClick = { showAiNotice = true },
                    onMixProduct = { unavailableTab = "Misturas" },
                    onViewMixtures = { unavailableTab = "Misturas" },
                    onCreateRoom = { roomToOpenId = null; roomDraftProductId = it; selectedMainTab = 4 },
                    rooms = rooms,
                    onOpenRoom = { roomDraftProductId = null; roomToOpenId = it; selectedMainTab = 4 },
                )
                4 -> RoomsScreen(
                    rooms = rooms, onRoomsChange = { rooms = it }, savedProductIds = savedProductIds,
                    onMenuItemSelected = onMainTabSelected, onAiClick = { showAiNotice = true },
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
                    onAiClick = { showAiNotice = true },
                )
            }
            if (showAiNotice) {
                AlertDialog(
                    onDismissRequest = { showAiNotice = false },
                    title = { Text("Assistente de IA") },
                    text = { Text("O acesso ao assistente está preparado, mas ainda não há um serviço de IA conectado neste app.") },
                    confirmButton = { TextButton(onClick = { showAiNotice = false }) { Text("Entendi") } },
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
        else -> {
            authScreens.SaveableStateProvider(authDestination.name) {
                when (authDestination) {
                    AuthDestination.Welcome -> LoginHomeScreen(onStart = { navigateAuth(AuthDestination.Login) })
                    AuthDestination.Login -> Login1Screen(
                        onContinue = { email, password -> authViewModel.loginWithEmail(email, password) },
                        onCreateAccount = { navigateAuth(AuthDestination.Registration) },
                        onForgotPassword = { navigateAuth(AuthDestination.RecoveryEmail) },
                    )
                    AuthDestination.Registration -> Cadastro1Screen(
                        onContinue = { navigateAuth(AuthDestination.Address) },
                        onLogin = { navigateAuth(AuthDestination.Login) },
                    )
                    AuthDestination.Address -> Cadastro2Screen(
                        onBack = { navigateAuth(AuthDestination.Registration) },
                        onSkip = registrationUnavailable, onEnter = registrationUnavailable,
                    )
                    AuthDestination.RecoveryEmail -> LoginPass1Screen(
                        onBack = { navigateAuth(AuthDestination.Login) },
                        onEmailContinue = { email ->
                            recoveryEmail = email
                            navigateAuth(AuthDestination.RecoveryCode)
                            authNotice = "Recuperação em demonstração nesta branch: nenhum e-mail foi enviado. O envio e a validação do código dependem da integração de autenticação."
                        },
                    )
                    AuthDestination.RecoveryCode -> LoginPassCodeScreen(
                        email = recoveryEmail,
                        onBack = { navigateAuth(AuthDestination.RecoveryEmail) },
                        onResend = { authNotice = "O reenvio de códigos ainda não está conectado nesta branch." },
                        onConfirm = { navigateAuth(AuthDestination.NewPassword) },
                    )
                    AuthDestination.NewPassword -> LoginPass2Screen(
                        onBack = { navigateAuth(AuthDestination.RecoveryCode) },
                        onConfirm = { authNotice = "Nenhuma senha foi alterada. A redefinição ainda depende da integração de autenticação, mantida fora desta branch." },
                    )
                    AuthDestination.RecoverySuccess -> LoginPassSuccesScreen(onStart = { navigateAuth(AuthDestination.Login) })
                }
            }
        }
    }
    authNotice?.let { notice ->
        AlertDialog(onDismissRequest = { authNotice = null }, title = { Text("Autenticação") },
            text = { Text(notice) }, confirmButton = { TextButton(onClick = { authNotice = null }) { Text("Entendi") } })
    }
}
