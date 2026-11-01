package com.quimia.android.domain.preferences

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
    val soundEffects: Boolean = true,
    val mixtureNotifications: Boolean = false,
    val shelfReminders: Boolean = false,
) {
    val textScale: Float get() = 1f + textSizeStep.coerceIn(0, 2) * .2f
}

interface ProfilePreferencesRepository {
    suspend fun load(): ProfilePreferences
    suspend fun save(value: ProfilePreferences)
}
