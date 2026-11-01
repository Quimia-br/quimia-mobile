package com.quimia.android.presentation.screens.profile

import android.content.Context
import com.quimia.android.presentation.components.isLegacyBirthDate

class LocalProfileStore(context: Context, uid: String, email: String) {
    private val preferences = context.applicationContext.getSharedPreferences("quimia_local_profile", Context.MODE_PRIVATE)
    private val scope = profileStorageScope(uid, email)

    fun displayName(): String? = preferences.getString("$scope.name", null)?.takeIf { profileNameError(it) == null }
    fun photoUri(): String? = storedPhotoUri(preferences.getString("$scope.photo", null))
    fun email(): String? = preferences.getString("$scope.email", null)?.takeIf { profileEmailError(it) == null }
    fun birthDate(): String = preferences.getString("$scope.birth_date", "").orEmpty()
        .takeIf { profileBirthDateError(it) == null || isLegacyBirthDate(it) }.orEmpty()
    fun saveDetails(value: ProfileDetails) {
        require(profileNameError(value.name) == null && profileEmailError(value.email) == null && profileBirthDateError(value.birthDate) == null)
        preferences.edit().putString("$scope.name", normalizedProfileName(value.name))
            .putString("$scope.email", value.email.trim()).putString("$scope.birth_date", value.birthDate.trim()).apply()
    }
    fun saveDisplayName(name: String) {
        require(profileNameError(name) == null)
        preferences.edit().putString("$scope.name", normalizedProfileName(name)).apply()
    }
    fun savePhotoUri(uri: String) {
        require(storedPhotoUri(uri) != null)
        preferences.edit().putString("$scope.photo", uri).apply()
    }
}
