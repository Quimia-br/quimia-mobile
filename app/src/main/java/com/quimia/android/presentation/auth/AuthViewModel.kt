package com.quimia.android.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quimia.android.data.auth.FirebaseAuthService
import com.quimia.android.data.auth.GoogleOnboardingStore
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.screens.auth.GoogleOnboarding
import com.quimia.android.presentation.screens.profile.LocalProfileStore
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import com.quimia.android.utils.AnalyticsLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authService = FirebaseAuthService()
    private val onboardingStore = GoogleOnboardingStore(application)
    private val _googleOnboarding = MutableStateFlow<GoogleOnboarding?>(null)
    val googleOnboarding: StateFlow<GoogleOnboarding?> = _googleOnboarding
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
    val providerPhotoUrl: String? get() = authService.getCurrentUser()?.photoUrl?.toString()
    fun savedAddress(uid: String, email: String) = onboardingStore.addressFields(uid, email)

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (authService.isLoggedIn()) {
            val user = authService.getCurrentUser()
            if (user != null && user.providerData.any { it.providerId == "google.com" }) {
                val draft = currentGoogleProfile()
                if (draft == null) {
                    authService.logout()
                    _authState.value = AuthResult.Error("Não foi possível recuperar os dados da conta Google. Entre novamente.")
                    return
                }
                if (!onboardingStore.completed(draft)) {
                    _googleOnboarding.value = draft
                    _authState.value = AuthResult.Error("Complete seus dados para continuar.")
                    return
                }
            }
            _authState.value = AuthResult.Success(
                uid = user?.uid ?: "",
                email = user?.email
            )
        } else {
            _authState.value = AuthResult.Error("Não autenticado")
        }
    }

    fun loginWithEmail(email: String, password: String) {
        if (busy.value) return
        AnalyticsLogger.logLoginEvent("EMAIL", "STARTED")
        if (email.isBlank() || password.isBlank()) {
            AnalyticsLogger.logLoginEvent("EMAIL", "INVALID_INPUT")
            _authState.value = AuthResult.Error("Preencha e-mail e senha antes de entrar.")
            return
        }

        AnalyticsLogger.logLoginEvent("EMAIL", "SUCCESS")
        _authState.value = AuthResult.Success(uid = "mock-uid-123", email = email)
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

        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = authService.registerWithEmail(email, password)
            _authState.value = result
        }
    }

    fun loginWithGoogle(idToken: String) {
        AnalyticsLogger.logLoginEvent("GOOGLE", "STARTED")
        if (idToken.isBlank()) {
            AnalyticsLogger.logLoginEvent("GOOGLE", "INVALID_TOKEN")
            _authState.value = AuthResult.Error("Token do Google inválido.")
            return
        }

        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try {
                val result = authService.loginWithGoogle(idToken)
                if (result is AuthResult.Success) {
                    val draft = currentGoogleProfile() ?: error("O Google não retornou os dados da conta. Tente novamente.")
                    onboardingStore.markPending(draft.uid, draft.email)
                    _googleOnboarding.value = draft
                    _authState.value = AuthResult.Error("Complete seus dados para continuar.")
                } else message.value = (result as? AuthResult.Error)?.message ?: "Não foi possível entrar com Google."
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { message.value = error.message ?: "Não foi possível entrar com Google."
            } finally { busy.value = false }
        }
    }

    private fun currentGoogleProfile(): GoogleOnboarding? {
        val user = authService.getCurrentUser() ?: return null
        val email = user.email?.takeIf { it.isNotBlank() } ?: return null
        val stored = LocalProfileStore(getApplication<Application>(), user.uid, email)
        return GoogleOnboarding(user.uid, email, stored.displayName() ?: user.displayName.orEmpty(), stored.birthDate(), user.photoUrl?.toString())
    }

    fun editGoogle(name: String, birthDate: String) {
        if (busy.value) return
        _googleOnboarding.value = _googleOnboarding.value?.copy(name = name, birthDate = birthDate)
    }
    fun continueGoogle() {
        val draft = _googleOnboarding.value ?: return
        _googleOnboarding.value = draft.continueToAddress() ?: run { message.value = "Confira seu nome e data de nascimento."; return }
    }
    fun backGoogle() { _googleOnboarding.value = _googleOnboarding.value?.backToDetails() }
    fun cancelGoogle() { authService.logout(); _googleOnboarding.value = null; _authState.value = AuthResult.Error("Não autenticado") }
    fun finishGoogle(address: NearbyAddress? = null) {
        val draft = _googleOnboarding.value ?: return
        if (busy.value || !draft.valid || !draft.addressStep) return
        busy.value = true
        viewModelScope.launch {
            try {
                check(authService.getCurrentUser()?.uid == draft.uid) { "A conta Google mudou. Entre novamente." }
                withContext(Dispatchers.IO) { onboardingStore.complete(draft, address) }
                _googleOnboarding.value = null
                _authState.value = AuthResult.Success(draft.uid, draft.email)
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { message.value = error.message ?: "Não foi possível salvar seus dados. Tente novamente."
            } finally { busy.value = false }
        }
    }

    fun logout() {
        authService.logout()
        _googleOnboarding.value = null
        _authState.value = AuthResult.Error("Desconectado")
    }
}
