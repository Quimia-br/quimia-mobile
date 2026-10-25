package com.quimia.android.presentation.screens.profile

import android.content.Context

class ProfilePreferencesStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("quimia_accessibility", Context.MODE_PRIVATE)

    fun load() = ProfilePreferences(
        theme = ProfileTheme.fromStored(preferences.getString("theme", null)),
        textSizeStep = preferences.getInt("text_size_step", 0).coerceIn(0, 2),
        dyslexicFont = preferences.getBoolean("dyslexic_font", false),
    )

    fun save(value: ProfilePreferences) {
        preferences.edit().putString("theme", value.theme.name)
            .putInt("text_size_step", value.textSizeStep.coerceIn(0, 2))
            .putBoolean("dyslexic_font", value.dyslexicFont).apply()
    }
}

class LocalProfileStore(context: Context, uid: String, email: String) {
    private val preferences = context.applicationContext.getSharedPreferences("quimia_local_profile", Context.MODE_PRIVATE)
    private val scope = profileStorageScope(uid, email)

    fun displayName(): String? = preferences.getString("$scope.name", null)?.takeIf { profileNameError(it) == null }
    fun photoUri(): String? = storedPhotoUri(preferences.getString("$scope.photo", null))
    fun saveDisplayName(name: String) {
        require(profileNameError(name) == null)
        preferences.edit().putString("$scope.name", normalizedProfileName(name)).apply()
    }
    fun savePhotoUri(uri: String) {
        require(storedPhotoUri(uri) != null)
        preferences.edit().putString("$scope.photo", uri).apply()
    }
}
