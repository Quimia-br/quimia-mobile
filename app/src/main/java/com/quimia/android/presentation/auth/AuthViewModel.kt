package com.quimia.android.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.quimia.android.data.project.*
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.components.BirthDate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.*

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val client = ProjectClient.get(application)
    private val api = client.api
    private val mutableAuth = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState = mutableAuth.asStateFlow()
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
    var recoveryEmail = ""
        private set
    private var challenge: String? = null
    private var resetToken: String? = null
    private data class Registration(val name: String, val email: String, val password: String, val birthDate: String, val created: Boolean = false)
    private var registration: Registration? = null
    val hasRegistration get() = registration != null
    val hasRecoveryChallenge get() = challenge != null
    val hasResetGrant get() = resetToken != null

    init {
        viewModelScope.launch {
            client.sessions.session.collect { session ->
                mutableAuth.value = session?.let { AuthResult.Success(it.user.id, it.user.email) } ?: AuthResult.Error("Não autenticado")
            }
        }
    }

    private fun request(block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { block() } }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { message.value = apiErrorMessage(error) }
            finally { busy.value = false }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) { message.value = "Preencha e-mail e senha."; return }
        request { client.sessions.save(api.login(apiBody("email" to email.trim(), "senha" to password))) }
    }

    fun prepareRegistration(name: String, birthDate: String, email: String, password: String, onContinue: () -> Unit) {
        if (name.trim().length < 2 || email.isBlank() || password.length !in 8..72) {
            message.value = "Informe nome, e-mail e uma senha entre 8 e 72 caracteres."; return
        }
        val date = BirthDate.parse(birthDate)
        if (date == null || date >= BirthDate.today()) { message.value = "Informe uma data de nascimento anterior a hoje."; return }
        registration = Registration(name.trim(), email.trim(), password, birthDateToApi(birthDate))
        onContinue()
    }

    fun finishRegistration(address: ApiAddress? = null) {
        val draft = registration ?: run { message.value = "Preencha os dados do cadastro novamente."; return }
        request {
            val body = buildJsonObject {
                put("nome", draft.name); put("email", draft.email); put("senha", draft.password); put("dataNasc", draft.birthDate)
                if (address != null) put("localizacao", Json.encodeToJsonElement(address))
            }
            if (!draft.created) {
                api.register(body)
                registration = draft.copy(created = true)
            }
            client.sessions.save(api.login(apiBody("email" to draft.email, "senha" to draft.password)))
            registration = null
        }
    }

    fun forgotPassword(email: String, onSent: () -> Unit) = request {
        val next = api.forgot(apiBody("email" to email.trim()))
        recoveryEmail = email.trim(); challenge = next.challengeToken; resetToken = null
        withContext(Dispatchers.Main) { onSent() }
    }

    fun verifyRecoveryCode(code: String, onVerified: () -> Unit) = request {
        val token = challenge ?: error("Sem desafio de recuperação")
        resetToken = api.verifyCode(apiBody("email" to recoveryEmail, "code" to code, "challengeToken" to token)).resetToken
        withContext(Dispatchers.Main) { onVerified() }
    }

    fun resetPassword(password: String, onReset: () -> Unit) = request {
        api.reset(apiBody("resetToken" to (resetToken ?: error("Sem autorização de recuperação")), "novaSenha" to password))
        resetToken = null; challenge = null
        withContext(Dispatchers.Main) { onReset() }
    }

    fun loginWithGoogle(idToken: String) = request {
        val user = FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
            ?: error("Login social sem usuário")
        val firebaseToken = user.getIdToken(true).await().token ?: error("Login social sem token")
        client.sessions.save(api.firebase(apiBody("idToken" to firebaseToken)))
    }

    fun loginWithFirebaseToken(idToken: String) = request {
        client.sessions.save(api.firebase(apiBody("idToken" to idToken)))
    }

    fun logout() {
        registration = null; challenge = null; resetToken = null
        client.sessions.clear()
        runCatching { FirebaseAuth.getInstance().signOut() }
    }
}
