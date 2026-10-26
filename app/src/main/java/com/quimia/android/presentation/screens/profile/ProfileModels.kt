package com.quimia.android.presentation.screens.profile

import java.security.MessageDigest
import java.util.Locale

data class ProfileDetails(val name: String, val birthDate: String, val email: String)

internal fun profileBirthDateError(value: String): String? {
    if (value.isBlank()) return null
    if (!value.matches(Regex("\\d{2}/\\d{2}"))) return "Informe a data no formato dia/mês."
    val day = value.take(2).toInt()
    val month = value.takeLast(2).toInt()
    val daysPerMonth = listOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    return if (month in 1..12 && day in 1..daysPerMonth[month - 1]) null else "Informe um dia e mês válidos."
}

internal fun formatProfileBirthDate(value: String): String {
    val digits = value.filter(Char::isDigit).take(4)
    return if (digits.length > 2) digits.take(2) + "/" + digits.drop(2) else digits
}

internal fun profileEmailError(value: String): String? =
    if (value.trim().length <= 254 && value.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) null else "Informe um e-mail válido."

enum class ProfileTheme(val label: String) {
    Light("Claro"), Dark("Escuro"), HighContrast("Alto contraste"), System("Sistema");

    fun usesDarkColors(systemDark: Boolean): Boolean = when (this) {
        Light -> false
        Dark, HighContrast -> true
        System -> systemDark
    }

    companion object {
        fun fromStored(value: String?): ProfileTheme = entries.firstOrNull { it.name == value } ?: Light
    }
}

data class ProfilePreferences(
    val theme: ProfileTheme = ProfileTheme.Light,
    val textSizeStep: Int = 0,
    val dyslexicFont: Boolean = false,
) {
    val textScale: Float get() = 1f + textSizeStep.coerceIn(0, 2) * .2f
}

internal fun normalizedProfileName(value: String): String = value.trim().replace(Regex("\\s+"), " ")

internal fun profileNameError(value: String): String? = when {
    normalizedProfileName(value).isEmpty() -> "Informe seu nome."
    normalizedProfileName(value).length > 80 -> "Use até 80 caracteres no nome."
    else -> null
}

fun profileInitials(value: String): String = value.trim().split(Regex("\\s+"))
    .mapNotNull { it.firstOrNull(Char::isLetterOrDigit)?.uppercaseChar() }.take(2).joinToString("").ifEmpty { "Q" }

internal fun profileStorageScope(uid: String, email: String): String = MessageDigest.getInstance("SHA-256")
    .digest((uid + "\u0000" + email.trim().lowercase(Locale.ROOT)).toByteArray(Charsets.UTF_8))
    .joinToString("") { "%02x".format(it) }

internal fun storedPhotoUri(value: String?): String? = value?.takeIf { it.startsWith("content://") && it.length > "content://".length }
