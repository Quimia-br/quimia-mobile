package com.quimia.android.presentation.screens.auth

import com.quimia.android.presentation.screens.profile.profileBirthDateError
import com.quimia.android.presentation.screens.profile.profileEmailError
import com.quimia.android.presentation.screens.profile.profileNameError
import com.quimia.android.presentation.components.BirthDate

class RegistrationDetails(val name: String, val birthDate: String, val email: String, val password: String) {
    override fun toString(): String = "RegistrationDetails([REDACTED])"
}

internal fun authNewPasswordError(value: String): String? = when {
    value.length < 8 -> "Use pelo menos 8 caracteres, com letra e número."
    value.toByteArray(Charsets.UTF_8).size > 72 -> "A senha pode ter no máximo 72 bytes. Use uma senha menor."
    !value.any(Char::isLetter) || !value.any(Char::isDigit) -> "A senha precisa ter letra e número."
    else -> null
}

internal fun authRegistrationBirthDateError(value: String, today: BirthDate = BirthDate.today()): String? {
    authBirthDateError(value)?.let { return it }
    val date = BirthDate.parse(value) ?: return "Informe uma data de nascimento válida."
    val beforeBirthday = today.month < date.month || (today.month == date.month && today.day < date.day)
    val age = today.year - date.year - if (beforeBirthday) 1 else 0
    return if (age < 16) "O cadastro exige idade mínima de 16 anos." else null
}

internal fun authRegistrationError(value: RegistrationDetails): String? = authNameError(value.name)
    ?: authRegistrationBirthDateError(value.birthDate) ?: authLoginInputError(value.email, value.password)
    ?: authNewPasswordError(value.password)

internal fun authApiBirthDate(value: String): String {
    val date = BirthDate.parse(value) ?: throw IllegalArgumentException("Data de nascimento inválida.")
    return "%04d-%02d-%02d".format(java.util.Locale.ROOT, date.year, date.month, date.day)
}

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
internal fun authLoginPasswordError(password: String): String? = authNewPasswordError(password)
internal fun authNameError(value: String): String? = profileNameError(value)
internal fun authBirthDateError(value: String): String? =
    if (value.isBlank()) "Informe sua data de nascimento." else profileBirthDateError(value)
internal fun authPasswordError(value: String): String? = authNewPasswordError(value)
internal fun authPasswordConfirmationError(password: String, confirmation: String): String? =
    if (confirmation != password) "As senhas precisam ser iguais." else authPasswordError(confirmation)

internal val BrazilianStates = listOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")
