package com.quimia.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.quimia.android.utils.AnalyticsLogger
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
            QuimiaTheme(darkTheme = false) {
                QuimiaApp()
            }
        }
    }
}

@Composable
private fun QuimiaApp() {
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
    var showAiNotice by rememberSaveable { mutableStateOf(false) }
    var unavailableTab by rememberSaveable { mutableStateOf<String?>(null) }
    val onMainTabSelected: (Int) -> Unit = { index ->
        if (index == 0 || index == 1) selectedMainTab = index
        else unavailableTab = listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja")
            .getOrElse(index) { "Esta tela" }
    }

    val currentScreen = when {
        authState is AuthResult.Success -> if (selectedMainTab == 1) "catalog" else "home"
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

    when (authState) {
        is AuthResult.Success -> {
            if (selectedMainTab == 1) {
                CatalogScreen(
                    onMenuItemSelected = onMainTabSelected,
                    onAiClick = { showAiNotice = true },
                )
            } else {
                HomeScreen(
                    onMenuItemSelected = onMainTabSelected,
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
