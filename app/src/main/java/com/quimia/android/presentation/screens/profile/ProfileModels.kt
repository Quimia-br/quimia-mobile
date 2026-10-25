package com.quimia.android.presentation.screens.profile

import java.security.MessageDigest
import java.util.Locale

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
