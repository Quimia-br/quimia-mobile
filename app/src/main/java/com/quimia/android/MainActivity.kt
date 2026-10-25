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
import com.quimia.android.presentation.screens.profile.*
import com.quimia.android.utils.AnalyticsLogger
import com.quimia.android.presentation.system.HideSystemNavigationBar
import theme.QuimiaTheme

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
    var showLoginHome by rememberSaveable { mutableStateOf(true) }
    var showLoginPass1 by rememberSaveable { mutableStateOf(false) }
    var showLoginPassCode by rememberSaveable { mutableStateOf(false) }
    var showLoginPass2 by rememberSaveable { mutableStateOf(false) }
    var showLoginPassSucces by rememberSaveable { mutableStateOf(false) }
    var showCadastro1 by rememberSaveable { mutableStateOf(false) }
    var showCadastro2 by rememberSaveable { mutableStateOf(false) }
    var selectedMainTab by rememberSaveable { mutableStateOf(0) }
    val sessionUid = (authState as? AuthResult.Success)?.uid
    val sessionEmail = (authState as? AuthResult.Success)?.email.orEmpty()
    val context = LocalContext.current
    val profileStore = remember(sessionUid, sessionEmail) { LocalProfileStore(context, sessionUid.orEmpty(), sessionEmail) }
    var displayName by rememberSaveable(sessionUid, sessionEmail) {
        mutableStateOf(profileStore.displayName() ?: sessionEmail.substringBefore('@').ifBlank { "Seu nome" })
    }
    var photoUri by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(profileStore.photoUri()) }
    val photoPainter = rememberAsyncImagePainter(photoUri)
    val photo = photoPainter.takeUnless { photoUri == null || it.state is AsyncImagePainter.State.Error }
    var showProfile by rememberSaveable(sessionUid, sessionEmail) { mutableStateOf(false) }
    var savedProductIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var favoriteProductIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var savedPointIds by rememberSaveable(sessionUid) { mutableStateOf(emptyList<Int>()) }
    var referenceAddress by rememberSaveable(sessionUid) { mutableStateOf("Defina seu endereço de referência") }
    var referenceAddressFields by rememberSaveable(sessionUid) { mutableStateOf(emptyList<String>()) }
    var showAiNotice by rememberSaveable { mutableStateOf(false) }
    var unavailableTab by rememberSaveable { mutableStateOf<String?>(null) }
    val onMainTabSelected: (Int) -> Unit = { index ->
        if (index in 0..3) selectedMainTab = index
        else unavailableTab = listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja")
            .getOrElse(index) { "Esta tela" }
    }

    val currentScreen = when {
        authState is AuthResult.Success && showProfile -> "profile"
        authState is AuthResult.Success -> when (selectedMainTab) { 1 -> "catalog"; 2 -> "shelf"; 3 -> "proximity"; else -> "home" }
        showLoginHome -> "login_welcome"
        showCadastro2 -> "registration_details"
        showCadastro1 -> "registration"
        showLoginPass1 -> "password_recovery_email"
        showLoginPassCode -> "password_recovery_code"
        showLoginPass2 -> "password_recovery_new_password"
        showLoginPassSucces -> "password_recovery_success"
        else -> "login"
    }

    LaunchedEffect(currentScreen) {
        AnalyticsLogger.logScreenView(currentScreen)
    }
    BackHandler(enabled = authState is AuthResult.Success && (showProfile || selectedMainTab in 2..3)) {
        if (showProfile) showProfile = false else selectedMainTab = 0
    }

    when (authState) {
        is AuthResult.Success -> {
            if (showProfile) ProfileScreen(
                displayName = displayName, email = sessionEmail, preferences = preferences,
                onPreferencesChange = onPreferencesChange, onBack = { showProfile = false }, photo = photo,
                onDisplayNameChange = { displayName = it; profileStore.saveDisplayName(it) },
                onPhotoUriChange = { photoUri = it; profileStore.savePhotoUri(it) },
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
                    onCreateRoom = { unavailableTab = "Cômodos" },
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
            if (showLoginHome) {
                LoginHomeScreen(
                    onStart = {
                        showLoginHome = false
                    }
                )
            } else if (showCadastro2) {
                Cadastro2Screen(
                    onBack = {
                        showCadastro2 = false
                    }
                )
            } else if (showCadastro1) {
                Cadastro1Screen(
                    onContinue = {
                        showCadastro2 = true
                    }
                )
            } else if (showLoginPass1) {
                LoginPass1Screen(
                    onBack = {
                        showLoginPass1 = false
                    },
                    onContinue = {
                        showLoginPass1 = false
                        showLoginPassCode = true
                    }
                )
            } else if (showLoginPassCode) {
                LoginPassCodeScreen(
                    onBack = {
                        showLoginPassCode = false
                        showLoginPass1 = true
                    },
                    onConfirm = {
                        showLoginPassCode = false
                        showLoginPass2 = true
                    }
                )
            } else if (showLoginPass2) {
                LoginPass2Screen(
                    onBack = {
                        showLoginPass2 = false
                        showLoginPassCode = true
                    },
                    onConfirm = {
                        showLoginPass2 = false
                        showLoginPassSucces = true
                    }
                )
            } else if (showLoginPassSucces) {
                LoginPassSuccesScreen(
                    onStart = {
                        showLoginPassSucces = false
                        showLoginHome = true
                    }
                )
            } else {
                Login1Screen(
                    onContinue = { email, password ->
                        authViewModel.loginWithEmail(email, password)
                    },
                    onCreateAccount = {
                        showCadastro1 = true
                    },
                    onForgotPassword = {
                        showLoginPass1 = true
                    }
                )
            }
        }
    }
}
