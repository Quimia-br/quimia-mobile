package com.quimia.android.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quimia.android.data.auth.*
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.screens.auth.GoogleOnboarding
import com.quimia.android.presentation.screens.auth.authLoginInputError
import com.quimia.android.presentation.screens.profile.LocalProfileStore
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepositoryProvider.get(application)
    private val firebase = FirebaseAuthService()
    private val onboardingStore = GoogleOnboardingStore(application)
    private val _googleOnboarding = MutableStateFlow<GoogleOnboarding?>(null)
    val googleOnboarding: StateFlow<GoogleOnboarding?> = _googleOnboarding
    val busy = MutableStateFlow(false)
    val busyLabel = MutableStateFlow("")
    val message = MutableStateFlow<String?>(null)
    val sessionProblem = MutableStateFlow<String?>(null)
    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState
    private var currentProfile: UserProfile? = null
    val providerPhotoUrl: String? get() = currentProfile?.fotoUrl
    val accountName: String? get() = currentProfile?.nome
    val accountBirthDate: String get() = displayBirthDate(currentProfile?.dataNasc)
    fun savedAddress(uid: String, email: String) = onboardingStore.addressFields(uid, email)

    init { retrySession() }

    private fun request(label: String, block: suspend () -> Unit) {
        if (busy.value) return
        busyLabel.value = label
        busy.value = true
        viewModelScope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { message.value = authErrorMessage(error) }
            finally { busy.value = false }
        }
    }

    fun retrySession() = request("Conferindo sua sessão…") {
        sessionProblem.value = null
        repository.restore()
        when (val state = repository.session.value) {
            is SessionState.Authenticated -> {
                currentProfile = state.profile
                if (onboardingStore.pending(state.profile.id, state.profile.email)) {
                    _googleOnboarding.value = googleProfile(state.profile)
                    _authState.value = AuthResult.Error("Complete seus dados para continuar.")
                } else unlock(state.profile)
            }
            is SessionState.Unavailable -> {
                _authState.value = AuthResult.Error(state.message)
                sessionProblem.value = state.message
            }
            else -> _authState.value = AuthResult.Error("Não autenticado")
        }
    }

    fun loginWithEmail(email: String, password: String) {
        authLoginInputError(email, password)?.let { message.value = it; return }
        request("Entrando na sua conta…") {
            firebase.logout()
            repository.login(email, password)
            val profile = authenticatedProfile()
            onboardingStore.clearPending(profile.id, profile.email)
            _googleOnboarding.value = null
            unlock(profile)
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (idToken.isBlank()) { message.value = "Não foi possível confirmar a conta Google."; return }
        request("Entrando com Google…") {
            val result = firebase.loginWithGoogle(idToken)
            if (result !is AuthResult.Success) {
                message.value = (result as? AuthResult.Error)?.message ?: "Não foi possível entrar com Google."
                return@request
            }
            try {
                repository.loginWithFirebaseToken(firebase.backendIdToken())
                val profile = authenticatedProfile()
                currentProfile = profile
                withContext(Dispatchers.IO) { onboardingStore.markPending(profile.id, profile.email) }
                _googleOnboarding.value = googleProfile(profile)
                _authState.value = AuthResult.Error("Complete seus dados para continuar.")
            } catch (error: Exception) {
                firebase.logout()
                withContext(NonCancellable) { repository.logout() }
                throw error
            }
        }
    }

    private fun authenticatedProfile() = (repository.session.value as? SessionState.Authenticated)?.profile
        ?: throw AuthFailure("A API não confirmou sua sessão. Entre novamente.")

    private fun unlock(profile: UserProfile) {
        currentProfile = profile
        sessionProblem.value = null
        _authState.value = AuthResult.Success(profile.id, profile.email)
    }
    private fun googleProfile(profile: UserProfile): GoogleOnboarding {
        val stored = LocalProfileStore(getApplication<Application>(), profile.id, profile.email)
        return GoogleOnboarding(profile.id, profile.email, stored.displayName() ?: profile.nome,
            stored.birthDate().ifBlank { displayBirthDate(profile.dataNasc) }, profile.fotoUrl)
    }
    fun editGoogle(name: String, birthDate: String) {
        if (!busy.value) _googleOnboarding.value = _googleOnboarding.value?.copy(name = name, birthDate = birthDate)
    }
    fun continueGoogle() {
        if (busy.value) return
        val draft = _googleOnboarding.value ?: return
        _googleOnboarding.value = draft.continueToAddress() ?: run { message.value = "Confira seu nome e data de nascimento."; return }
    }
    fun backGoogle() { if (!busy.value) _googleOnboarding.value = _googleOnboarding.value?.backToDetails() }
    fun cancelGoogle() = logout()
    fun finishGoogle(address: NearbyAddress? = null) {
        val draft = _googleOnboarding.value ?: return
        if (!draft.valid || !draft.addressStep) return
        request("Salvando seus dados neste aparelho…") {
            val profile = authenticatedProfile()
            check(profile.id == draft.uid) { "A conta mudou. Entre novamente." }
            withContext(Dispatchers.IO) { onboardingStore.complete(draft, address) }
            _googleOnboarding.value = null
            unlock(profile)
        }
    }
    fun logout() = request("Saindo da sua conta…") {
        try { repository.logout() }
        finally {
            firebase.logout()
            _googleOnboarding.value = null
            currentProfile = null
            sessionProblem.value = null
            _authState.value = AuthResult.Error("Não autenticado")
        }
    }
}

internal fun displayBirthDate(value: String?): String {
    val parts = value?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }?.split('-') ?: return ""
    return "${parts[2]}/${parts[1]}/${parts[0]}"
}
