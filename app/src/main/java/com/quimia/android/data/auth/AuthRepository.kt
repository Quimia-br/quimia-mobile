package com.quimia.android.data.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

sealed interface SessionState {
    data object Checking : SessionState
    data object SignedOut : SessionState
    data class Authenticated(val profile: UserProfile) : SessionState
    data class Unavailable(val message: String) : SessionState
}
class AuthFailure(message: String) : Exception(message)

class AuthRepository(private val api: QuimiaAuthApi, private val store: SessionStore,
    private val now: () -> Long = System::currentTimeMillis) {
    private val mutex = Mutex()
    private var tokens: StoredSession? = null
    private val mutableSession = MutableStateFlow<SessionState>(SessionState.Checking)
    val session = mutableSession.asStateFlow()

    suspend fun restore() = withContext(Dispatchers.IO) {
        mutex.withLock {
            mutableSession.value = SessionState.Checking
            try {
                tokens = store.read()
                if (tokens == null) mutableSession.value = SessionState.SignedOut
                else mutableSession.value = SessionState.Authenticated(profile())
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (error is AuthFailure) signOutLocally()
                else if (tokens != null) mutableSession.value = SessionState.Unavailable(authErrorMessage(error))
            }
        }
    }

    suspend fun login(email: String, password: String) = signIn { api.login(LoginRequest(email.trim(), password)) }
    suspend fun loginWithFirebaseToken(token: String) = signIn { api.firebase(FirebaseLoginRequest(token)) }
    private suspend fun signIn(request: suspend () -> MobileSessionResponse) = withContext(Dispatchers.IO) {
        mutex.withLock {
            signOutLocally()
            try {
                save(request())
                mutableSession.value = SessionState.Authenticated(profile())
            } catch (error: Exception) { signOutLocally(); throw error }
        }
    }

    suspend fun reloadProfile() = withContext(Dispatchers.IO) {
        mutex.withLock { mutableSession.value = SessionState.Authenticated(profile()) }
    }

    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                tokens?.let { api.logout("Bearer ${it.accessToken}") }
                true
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                false
            } finally { signOutLocally() }
        }
    }

    private suspend fun profile(): UserProfile {
        val result = authorized { api.me(it) }
        val expectedId = requireNotNull(tokens).userId
        if (result.id.isBlank() || result.nome.isBlank() || result.email.isBlank() || result.id != expectedId)
            throw AuthFailure("A API retornou um usuário incompatível com a sessão. Entre novamente.")
        return result
    }
    private fun save(response: MobileSessionResponse) {
        if (!response.tokenType.equals("Bearer", true) || response.accessToken.isBlank() || response.refreshToken.isBlank() ||
            response.expiresIn !in 1..31_536_000 || response.user.id.isBlank() || response.user.email.isBlank() || response.user.nome.isBlank())
            throw AuthFailure("A API retornou uma sessão inválida. Entre novamente.")
        if (tokens != null && tokens?.userId != response.user.id)
            throw AuthFailure("A API retornou uma sessão de outro usuário. Entre novamente.")
        val next = StoredSession(response.accessToken, response.refreshToken, now() + response.expiresIn * 1000, response.user.id)
        store.write(next)
        tokens = next
    }
    private fun signOutLocally() {
        tokens = null
        mutableSession.value = SessionState.SignedOut
        store.clear()
    }
    private suspend fun refresh() {
        val previous = tokens ?: throw AuthFailure("Entre para continuar.")
        try { save(api.refresh(RefreshRequest(previous.refreshToken))) }
        catch (error: Exception) {
            signOutLocally()
            if (error is CancellationException) throw error
            throw AuthFailure("Não foi possível renovar sua sessão. Entre novamente.")
        }
    }
    private suspend fun <T> authorized(request: suspend (String) -> T): T {
        val current = tokens ?: throw AuthFailure("Entre para continuar.")
        var refreshed = false
        if (current.expiresAt <= now() + 30_000) { refresh(); refreshed = true }
        try { return request("Bearer ${requireNotNull(tokens).accessToken}") }
        catch (error: HttpException) {
            if (error.code() != 401) throw error
            if (refreshed) { signOutLocally(); throw AuthFailure("Sua sessão expirou. Entre novamente.") }
        }
        refresh()
        try { return request("Bearer ${requireNotNull(tokens).accessToken}") }
        catch (error: HttpException) { if (error.code() == 401) signOutLocally(); throw error }
    }
}

fun authErrorMessage(error: Exception): String {
    if (error is AuthFailure) return error.message ?: "Entre novamente."
    if (error is HttpException) {
        val detail = runCatching { AuthApiFactory.json.parseToJsonElement(error.response()?.errorBody()?.string().orEmpty())
            .jsonObject["detail"]?.jsonPrimitive?.content }.getOrNull()
        return when {
            detail == "invalid_credentials" -> "E-mail ou senha incorretos."
            detail == "email_not_verified" -> "Verifique seu e-mail na conta Google antes de entrar."
            detail == "firebase_disabled" -> "O login Google está desativado no servidor. Tente entrar com e-mail."
            detail == "invalid_firebase_token" -> "O servidor não confirmou a conta Google. Tente entrar novamente."
            detail == "blocked" -> "Acesso temporariamente bloqueado por excesso de tentativas. Aguarde e tente novamente."
            error.code() == 401 -> "Não foi possível autenticar. Confira os dados ou entre novamente."
            error.code() == 403 -> "Acesso não permitido."
            error.code() == 429 -> "Muitas tentativas. Aguarde um pouco e tente novamente."
            error.code() in 400..499 -> "Confira os dados informados e tente novamente."
            else -> "O serviço está indisponível. Tente novamente em instantes."
        }
    }
    return if (error is IOException) "Não foi possível conectar. Confira sua conexão e tente novamente."
        else "Não foi possível concluir o login. Tente novamente."
}
