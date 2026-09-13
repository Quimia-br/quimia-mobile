package com.quimia.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.auth.AuthViewModel
import com.quimia.android.presentation.screens.auth.Cadastro1Screen
import com.quimia.android.presentation.screens.auth.Cadastro2Screen
import com.quimia.android.presentation.screens.auth.Login1Screen
import com.quimia.android.presentation.screens.auth.LoginHomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightNavigationBars = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            QuimiaApp()
        }
    }
}

@Composable
private fun QuimiaApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    var showLoginHome by rememberSaveable { mutableStateOf(true) }
    var showCadastro1 by rememberSaveable { mutableStateOf(false) }
    var showCadastro2 by rememberSaveable { mutableStateOf(false) }

    when (authState) {
        is AuthResult.Success -> {
            LoginHomeScreen()
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
            } else {
                Login1Screen(
                    onContinue = { /* handle continue or navigate further */ },
                    onCreateAccount = {
                        showCadastro1 = true
                    }
                )
            }
        }
    }
}
