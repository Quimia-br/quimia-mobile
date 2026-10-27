package com.quimia.android.presentation.screens.auth

import com.quimia.android.presentation.screens.profile.profileBirthDateError
import com.quimia.android.presentation.screens.profile.profileEmailError
import com.quimia.android.presentation.screens.profile.profileNameError

enum class AuthDestination(val screenName: String) {
    Welcome("login_welcome"), Login("login"), Registration("registration"),
    Address("registration_details"), RecoveryEmail("password_recovery_email"),
    RecoveryCode("password_recovery_code"), NewPassword("password_recovery_new_password"),
    RecoverySuccess("password_recovery_success"), GoogleDetails("google_details"), GoogleAddress("google_address");

    fun back(): AuthDestination = when (this) {
        Welcome, Login -> Welcome
        Registration, RecoveryEmail, RecoverySuccess -> Login
        Address -> Registration
        RecoveryCode -> RecoveryEmail
        NewPassword -> RecoveryCode
        GoogleDetails -> Login
        GoogleAddress -> GoogleDetails
    }
}

internal fun authEmailError(value: String): String? = profileEmailError(value.trim())
internal fun authLoginInputError(email: String, password: String): String? = when {
    authEmailError(email) != null || email.trim().length > 255 -> "Informe um e-mail válido."
    else -> authLoginPasswordError(password)
}
internal fun authLoginPasswordError(password: String): String? = when {
    password.isBlank() -> "Informe sua senha."
    password.length > 72 -> "A senha pode ter no máximo 72 caracteres."
    else -> null
}
internal fun authNameError(value: String): String? = profileNameError(value)
internal fun authBirthDateError(value: String): String? =
    if (value.isBlank()) "Informe sua data de nascimento." else profileBirthDateError(value)
internal fun authPasswordError(value: String): String? =
    if (value.length < 6) "A senha precisa ter pelo menos 6 caracteres." else null
internal fun authPasswordConfirmationError(password: String, confirmation: String): String? =
    if (confirmation != password) "As senhas precisam ser iguais." else authPasswordError(confirmation)

internal val BrazilianStates = listOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")
