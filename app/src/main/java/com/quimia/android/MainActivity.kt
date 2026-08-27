package com.quimia.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.auth.AuthViewModel
import com.quimia.android.presentation.screens.HomeScreen
import com.quimia.android.presentation.screens.LoginScreen

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

    when (authState) {
        is AuthResult.Success -> {
            HomeScreen(
                onLogout = { authViewModel.logout() }
            )
        }
        else -> {
            LoginScreen(viewModel = authViewModel)
        }
    }
}
