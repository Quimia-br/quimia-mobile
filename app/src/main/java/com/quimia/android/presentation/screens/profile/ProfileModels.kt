package com.quimia.android.presentation.screens.profile

import java.security.MessageDigest
import java.util.Locale
import com.quimia.android.presentation.components.completeBirthDateError

data class ProfileDetails(val name: String, val birthDate: String, val email: String)

internal fun profileBirthDateError(value: String): String? = completeBirthDateError(value)

internal fun formatProfileBirthDate(value: String): String {
    val digits = value.filter(Char::isDigit).take(8)
    return when { digits.length > 4 -> digits.take(2) + "/" + digits.substring(2, 4) + "/" + digits.drop(4)
        digits.length > 2 -> digits.take(2) + "/" + digits.drop(2)
        else -> digits }
}

internal fun profileEmailError(value: String): String? =
    if (value.trim().length <= 254 && value.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) null else "Informe um e-mail válido."

typealias ProfileTheme = com.quimia.android.domain.preferences.ProfileTheme
typealias ProfilePreferences = com.quimia.android.domain.preferences.ProfilePreferences

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
