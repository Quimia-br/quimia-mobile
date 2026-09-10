package com.quimia.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.auth.AuthViewModel
import com.quimia.android.presentation.screens.auth.Login1Screen
import com.quimia.android.presentation.screens.auth.LoginHomeScreen
import com.quimia.android.presentation.screens.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    when (authState) {
        is AuthResult.Success -> {
            HomeScreen(onLogout = { authViewModel.logout() })
        }
        else -> {
            if (showLoginHome) {
                LoginHomeScreen(
                    onStart = {
                        showLoginHome = false
                    }
                )
            } else {
                Login1Screen(onContinue = { /* handle continue or navigate further */ })
            }
        }
    }
}
