package com.quimia.android.presentation.auth

import com.quimia.android.data.auth.*
import com.quimia.android.presentation.screens.auth.*
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import com.quimia.android.data.address.normalizedPostalCode

internal fun initialApiAddress(value: NearbyAddress): AuthAddress {
    value.validationError()?.let { throw AuthFailure(it) }
    val number = value.number.toIntOrNull()?.takeIf { it > 0 }
        ?: throw AuthFailure("Informe um número de endereço inteiro maior que zero, como a API exige.")
    if (listOf(value.district, value.street, value.complement).any { it.length > 255 })
        throw AuthFailure("Bairro, rua e complemento podem ter no máximo 255 caracteres.")
    return AuthAddress(normalizedPostalCode(value.postalCode), value.state.uppercase(), value.district.trim(), value.street.trim().ifBlank { null },
        number, value.complement.trim().ifBlank { null })
}

internal class InitialAuthFlow(private val repository: AuthRepository, private val now: () -> Long = System::currentTimeMillis) {
    private class Registration(val details: RegistrationDetails, var createdId: String? = null, var address: AuthAddress? = null)
    private class Challenge(val email: String, val token: String, val expiresAt: Long)
    private class Grant(val token: String, val expiresAt: Long)
    private var registration: Registration? = null
    private var challenge: Challenge? = null
    private var grant: Grant? = null
    val hasRegistration get() = registration != null
    val hasChallenge get() = challenge?.expiresAt?.let { it > now() } == true
    val hasGrant get() = grant?.expiresAt?.let { it > now() } == true
    val recoveryEmail get() = challenge?.email.orEmpty()

    fun prepareRegistration(details: RegistrationDetails) {
        authRegistrationError(details)?.let { throw AuthFailure(it) }
        registration = Registration(details)
    }
    suspend fun finishRegistration(address: AuthAddress?): UserProfile {
        val draft = registration ?: throw AuthFailure("Preencha os dados do cadastro novamente.")
        if (draft.createdId == null) {
            val value = draft.details
            val created = repository.register(RegisterRequest(value.name.trim(), value.email.trim(), value.password,
                authApiBirthDate(value.birthDate), address))
            if (created.id.isBlank() || !created.email.equals(value.email.trim(), true))
                throw AuthFailure("A API não confirmou a conta criada. Tente fazer login.")
            draft.createdId = created.id
            draft.address = address
        } else if (address != draft.address) {
            throw AuthFailure("A conta já foi criada com os dados anteriores. Faça login para continuar; o novo endereço não foi enviado.")
        }
        repository.login(draft.details.email, draft.details.password)
        val profile = (repository.session.value as? SessionState.Authenticated)?.profile
        if (profile == null || profile.id != draft.createdId) {
            repository.logout()
            throw AuthFailure("A API não confirmou a sessão da conta criada. Faça login novamente.")
        }
        registration = null
        return profile
    }

    suspend fun requestCode(email: String) {
        authEmailError(email)?.let { throw AuthFailure(it) }
        grant = null
        val result = repository.requestRecovery(email)
        if (result.challengeToken.isBlank() || result.expiresIn !in 1..86_400)
            throw AuthFailure("A API não retornou um desafio de recuperação válido. Tente novamente.")
        challenge = Challenge(email.trim(), result.challengeToken, now() + result.expiresIn * 1000)
    }
    suspend fun verifyCode(code: String) {
        val current = challenge?.takeIf { it.expiresAt > now() }
            ?: throw AuthFailure("O código expirou. Solicite um novo código.")
        if (!code.matches(Regex("[0-9]{4}"))) throw AuthFailure("Informe os 4 números do código.")
        val result = repository.verifyRecovery(current.email, code, current.token)
        if (result.resetToken.isBlank() || result.expiresIn !in 1..86_400)
            throw AuthFailure("A API não autorizou a redefinição da senha. Solicite outro código.")
        grant = Grant(result.resetToken, now() + result.expiresIn * 1000)
        challenge = null // The backend consumes the code when issuing the reset grant.
    }
    suspend fun resetPassword(password: String) {
        val current = grant?.takeIf { it.expiresAt > now() }
            ?: throw AuthFailure("A autorização expirou. Solicite um novo código.")
        authNewPasswordError(password)?.let { throw AuthFailure(it) }
        repository.resetPassword(current.token, password)
        clearRecovery()
    }
    fun clearRegistration() { registration = null }
    fun clearRecovery() { challenge = null; grant = null }
    fun clear() { clearRegistration(); clearRecovery() }
}
