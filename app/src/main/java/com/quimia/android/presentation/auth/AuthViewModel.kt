package com.quimia.android.presentation.auth

import androidx.lifecycle.viewModelScope
import com.quimia.android.data.auth.FirebaseAuthService
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.base.BaseViewModel
import com.quimia.android.utils.AnalyticsLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : BaseViewModel<AuthResult>() {
    private val authService = FirebaseAuthService()

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (authService.isLoggedIn()) {
            val user = authService.getCurrentUser()
            _authState.value = AuthResult.Success(
                uid = user?.uid ?: "",
                email = user?.email
            )
        } else {
            _authState.value = AuthResult.Error("Não autenticado")
        }
    }

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthResult.Error("Preencha e-mail e senha antes de entrar.")
            return
        }

        launchAsync {
            _authState.value = AuthResult.Loading
            val result = authService.loginWithEmail(email, password)
            _authState.value = result
        }
    }

    fun registerWithEmail(email: String, password: String) {
        if (email.isBlank()) {
            _authState.value = AuthResult.Error("Informe um e-mail válido.")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthResult.Error("A senha precisa ter pelo menos 6 caracteres.")
            return
        }

        launchAsync {
            _authState.value = AuthResult.Loading
            val result = authService.registerWithEmail(email, password)
            _authState.value = result
        }
    }

    fun loginWithGoogle(idToken: String) {
        AnalyticsLogger.logLoginEvent("GOOGLE", "STARTED", "Token length: ${idToken.length}")
        if (idToken.isBlank()) {
            AnalyticsLogger.logLoginEvent("GOOGLE", "FAILED", "Token is blank")
            _authState.value = AuthResult.Error("Token do Google inválido.")
            return
        }

        launchAsync {
            _authState.value = AuthResult.Loading
            AnalyticsLogger.logLoginEvent("GOOGLE", "PROCESSING", "Sending to Firebase")
            val result = authService.loginWithGoogle(idToken)
            AnalyticsLogger.logLoginEvent("GOOGLE", if (result is AuthResult.Success) "SUCCESS" else "FAILED", "Result: $result")
            _authState.value = result
        }
    }

    fun logout() {
        authService.logout()
        _authState.value = AuthResult.Error("Desconectado")
    }
}
