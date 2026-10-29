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
    suspend fun register(body: RegisterRequest): UserProfile = withContext(Dispatchers.IO) { api.register(body) }
    suspend fun requestRecovery(email: String): RecoveryChallenge = withContext(Dispatchers.IO) {
        api.forgot(RecoveryEmailRequest(email.trim()))
    }
    suspend fun verifyRecovery(email: String, code: String, challenge: String): RecoveryGrant = withContext(Dispatchers.IO) {
        api.verifyCode(RecoveryCodeRequest(email, code, challenge))
    }
    suspend fun resetPassword(token: String, password: String) = withContext(Dispatchers.IO) {
        api.reset(ResetPasswordRequest(token, password))
    }
    suspend fun completeInitialProfile(name: String, birthDate: String, expectedUserId: String? = null): UserProfile = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (expectedUserId != null && tokens?.userId != expectedUserId) throw AuthFailure("Entre novamente para continuar.")
            val result = authorized { api.completeProfile(it, InitialProfileRequest(name, birthDate)) }
            if (result.id != tokens?.userId || result.nome != name || result.email.isBlank() || result.dataNasc != birthDate)
                throw AuthFailure("A API não confirmou os dados do perfil. Tente novamente.")
            mutableSession.value = SessionState.Authenticated(result)
            result
        }
    }
    suspend fun saveInitialAddress(address: AuthAddress, expectedUserId: String? = null): AuthAddress = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (expectedUserId != null && tokens?.userId != expectedUserId) throw AuthFailure("Entre novamente para continuar.")
            val saved = authorized { api.saveAddress(it, address) }
            if (saved.cep.replace("-", "") != address.cep.replace("-", "") || saved.estado != address.estado ||
                saved.bairro != address.bairro || saved.rua != address.rua || saved.numero != address.numero ||
                saved.complemento.orEmpty() != address.complemento.orEmpty() ||
                !confirmedCoordinate(address.latitude, saved.latitude) || !confirmedCoordinate(address.longitude, saved.longitude))
                throw AuthFailure("A API não confirmou o endereço informado. Tente novamente.")
            saved
        }
    }
    private suspend fun signIn(request: suspend () -> MobileSessionResponse) = withContext(Dispatchers.IO) {
        mutex.withLock {
            signOutLocally()
            try {
                save(request())
                mutableSession.value = SessionState.Authenticated(profile())
            } catch (error: Exception) { signOutLocally(); throw error }
        }
    }

    suspend fun reloadProfile(expectedUserId: String? = null) = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (expectedUserId != null && tokens?.userId != expectedUserId) throw AuthFailure("Entre novamente para continuar.")
            mutableSession.value = SessionState.Authenticated(profile())
        }
    }

    suspend fun <T> withSession(expectedUserId: String, request: suspend (String) -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (tokens?.userId != expectedUserId || expectedUserId.isBlank()) throw AuthFailure("Entre novamente para continuar.")
            val result = authorized(request)
            if (tokens?.userId != expectedUserId) throw AuthFailure("A sessão mudou. Entre novamente.")
            result
        }
    }

    suspend fun <T> withSession(expectedUserId: String, request: suspend (String) -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (tokens?.userId != expectedUserId || expectedUserId.isBlank()) throw AuthFailure("Entre novamente para continuar.")
            val result = authorized(request)
            if (tokens?.userId != expectedUserId) throw AuthFailure("A sessão mudou. Entre novamente.")
            result
        }
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

private fun confirmedCoordinate(expected: Double?, actual: Double?): Boolean =
    if (expected == null) actual == null else expected.isFinite() && actual != null && actual.isFinite() && kotlin.math.abs(expected - actual) <= .000001

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
            detail == "email_in_use" -> "Este e-mail já tem cadastro. Faça login ou recupere sua senha."
            detail == "underage" -> "A API permite cadastro somente a partir dos 18 anos."
            detail == "weak_password" -> "Escolha uma senha menos comum, com pelo menos 8 caracteres, letra e número."
            detail == "password_too_long" -> "A senha ultrapassa o limite de 72 bytes. Use uma senha menor."
            detail == "invalid_recovery_code" -> "Código inválido, expirado ou já utilizado. Confira o código ou solicite outro."
            detail == "invalid_reset_token" -> "A autorização para alterar a senha expirou ou já foi usada. Solicite outro código."
            error.code() == 401 -> "Não foi possível autenticar. Confira os dados ou entre novamente."
            error.code() == 403 -> "Acesso não permitido."
            error.code() == 429 -> "Muitas tentativas. Aguarde um pouco e tente novamente."
            error.code() in 400..499 -> "Confira os dados informados e tente novamente."
            else -> "O serviço está indisponível. Tente novamente em instantes."
        }
    }
    return if (error is IOException) "Não foi possível conectar. Confira sua conexão e tente novamente."
        else "Não foi possível concluir a operação. Tente novamente."
}
