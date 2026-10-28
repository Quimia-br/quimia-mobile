package com.quimia.android.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quimia.android.data.auth.*
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.screens.auth.*
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepositoryProvider.get(application)
    private val initial = InitialAuthFlow(repository)
    private val firebase = FirebaseAuthService()
    private val onboardingStore = GoogleOnboardingStore(application)
    private val _googleOnboarding = MutableStateFlow<GoogleOnboarding?>(null)
    val googleOnboarding: StateFlow<GoogleOnboarding?> = _googleOnboarding
    val busy = MutableStateFlow(false)
    val busyLabel = MutableStateFlow("")
    val message = MutableStateFlow<String?>(null)
    val sessionProblem = MutableStateFlow<String?>(null)
    val nextDestination = MutableStateFlow<AuthDestination?>(null)
    val recoveryEmail = MutableStateFlow("")
    val recoveryCodeError = MutableStateFlow<String?>(null)
    val recoveryRevision = MutableStateFlow(0)
    val hasRegistration get() = initial.hasRegistration
    val hasRecoveryChallenge get() = initial.hasChallenge
    val hasRecoveryGrant get() = initial.hasGrant
    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState
    private var currentProfile: UserProfile? = null
    val providerPhotoUrl: String? get() = currentProfile?.fotoUrl
    val accountName: String? get() = currentProfile?.nome
    val accountBirthDate: String get() = displayBirthDate(currentProfile?.dataNasc)
    fun savedAddress(uid: String, email: String) = onboardingStore.addressFields(uid, email)

    init { retrySession() }

    private fun request(label: String, onError: (Exception) -> Unit = { message.value = authErrorMessage(it) }, block: suspend () -> Unit) {
        if (busy.value) return
        busyLabel.value = label
        busy.value = true
        viewModelScope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { onError(error) }
            finally { busy.value = false }
        }
    }

    fun prepareRegistration(details: RegistrationDetails) {
        if (busy.value) return
        try { initial.prepareRegistration(details); nextDestination.value = AuthDestination.Address }
        catch (error: Exception) { message.value = authErrorMessage(error) }
    }
    fun finishRegistration(address: NearbyAddress? = null) = request("Criando sua conta…") {
        val apiAddress = address?.let(::initialApiAddress)
        firebase.logout()
        val profile = initial.finishRegistration(apiAddress)
        currentProfile = profile
        if (address != null) withContext(Dispatchers.IO) { onboardingStore.cacheAddress(profile.id, profile.email, address) }
        withContext(Dispatchers.IO) { onboardingStore.markCompleted(profile.id, profile.email) }
        unlock(profile)
    }
    fun forgotPassword(email: String) = request("Solicitando código de recuperação…") {
        initial.requestCode(email)
        recoveryEmail.value = initial.recoveryEmail
        recoveryCodeError.value = null
        recoveryRevision.value++
        nextDestination.value = AuthDestination.RecoveryCode
    }
    fun resendRecoveryCode() { forgotPassword(recoveryEmail.value) }
    fun verifyRecoveryCode(code: String) = request("Conferindo o código…", onError = {
        if (initial.hasChallenge) recoveryCodeError.value = authErrorMessage(it)
        else { message.value = authErrorMessage(it); nextDestination.value = AuthDestination.RecoveryEmail }
    }) {
        initial.verifyCode(code)
        recoveryCodeError.value = null
        nextDestination.value = AuthDestination.NewPassword
    }
    fun resetPassword(password: String) = request("Alterando sua senha…") {
        initial.resetPassword(password)
        nextDestination.value = AuthDestination.RecoverySuccess
    }
    fun clearRecoveryCodeError() { recoveryCodeError.value = null }

    fun retrySession() = request("Conferindo sua sessão…") {
        sessionProblem.value = null
        repository.restore()
        when (val state = repository.session.value) {
            is SessionState.Authenticated -> {
                currentProfile = state.profile
                if (onboardingStore.pending(state.profile.id, state.profile.email)) {
                    enterGoogle(state.profile)
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
            withContext(Dispatchers.IO) { onboardingStore.markCompleted(profile.id, profile.email) }
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
                enterGoogle(profile)
            } catch (error: Exception) {
                firebase.logout()
                withContext(NonCancellable) { repository.logout() }
                throw error
            }
        }
    }

    private fun authenticatedProfile() = (repository.session.value as? SessionState.Authenticated)?.profile
        ?: throw AuthFailure("A API não confirmou sua sessão. Entre novamente.")

    private suspend fun enterGoogle(profile: UserProfile) {
        currentProfile = profile
        val destination = googleEntryDestination(profile, onboardingStore.completed(profile.id, profile.email),
            onboardingStore.pending(profile.id, profile.email), onboardingStore.hasCheckpoint(profile.id, profile.email))
        if (destination == null) {
            withContext(Dispatchers.IO) { onboardingStore.markCompleted(profile.id, profile.email) }
            unlock(profile)
        } else {
            withContext(Dispatchers.IO) { onboardingStore.markPending(profile.id, profile.email) }
            _googleOnboarding.value = googleProfile(profile).copy(addressStep = destination == AuthDestination.GoogleAddress)
            _authState.value = AuthResult.Error("Complete seus dados para continuar.")
        }
    }

    private fun unlock(profile: UserProfile) {
        initial.clear()
        _googleOnboarding.value = null
        nextDestination.value = null
        currentProfile = profile
        sessionProblem.value = null
        _authState.value = AuthResult.Success(profile.id, profile.email)
    }
    private fun googleProfile(profile: UserProfile): GoogleOnboarding {
        return GoogleOnboarding(profile.id, profile.email, profile.nome,
            displayBirthDate(profile.dataNasc), profile.fotoUrl)
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
        request("Salvando seus dados na API…") {
            val profile = authenticatedProfile()
            check(profile.id == draft.uid) { "A conta mudou. Entre novamente." }
            val apiAddress = address?.let(::initialApiAddress)
            val updated = repository.completeInitialProfile(draft.name.trim(), authApiBirthDate(draft.birthDate))
            if (apiAddress != null) repository.saveInitialAddress(apiAddress)
            withContext(Dispatchers.IO) { onboardingStore.complete(draft.copy(name = updated.nome,
                birthDate = displayBirthDate(updated.dataNasc)), address) }
            _googleOnboarding.value = null
            unlock(updated)
        }
    }
    fun logout() = request("Saindo da sua conta…") {
        try { repository.logout() }
        finally {
            firebase.logout()
            _googleOnboarding.value = null
            currentProfile = null
            initial.clear()
            recoveryEmail.value = ""
            recoveryCodeError.value = null
            nextDestination.value = null
            sessionProblem.value = null
            _authState.value = AuthResult.Error("Não autenticado")
        }
    }
}

internal fun displayBirthDate(value: String?): String {
    val parts = value?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }?.split('-') ?: return ""
    return "${parts[2]}/${parts[1]}/${parts[0]}"
}
