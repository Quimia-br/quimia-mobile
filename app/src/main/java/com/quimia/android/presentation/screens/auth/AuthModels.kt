package com.quimia.android.presentation.screens.auth

import com.quimia.android.presentation.screens.profile.profileBirthDateError
import com.quimia.android.presentation.screens.profile.profileEmailError
import com.quimia.android.presentation.screens.profile.profileNameError

enum class AuthDestination(val screenName: String) {
    Welcome("login_welcome"), Login("login"), Registration("registration"),
    Address("registration_details"), RecoveryEmail("password_recovery_email"),
    RecoveryCode("password_recovery_code"), NewPassword("password_recovery_new_password"),
    RecoverySuccess("password_recovery_success");

    fun back(): AuthDestination = when (this) {
        Welcome, Login -> Welcome
        Registration, RecoveryEmail, RecoverySuccess -> Login
        Address -> Registration
        RecoveryCode -> RecoveryEmail
        NewPassword -> RecoveryCode
    }
}

internal fun authEmailError(value: String): String? = profileEmailError(value.trim())
internal fun authNameError(value: String): String? = profileNameError(value)
internal fun authBirthDateError(value: String): String? =
    if (value.isBlank()) "Informe sua data de nascimento." else profileBirthDateError(value)
internal fun authPasswordError(value: String): String? =
    if (value.length !in 8..72) "A senha precisa ter entre 8 e 72 caracteres." else null
internal fun authPasswordConfirmationError(password: String, confirmation: String): String? =
    if (confirmation != password) "As senhas precisam ser iguais." else authPasswordError(confirmation)

internal val BrazilianStates = listOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")
