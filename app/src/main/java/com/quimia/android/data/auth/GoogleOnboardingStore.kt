package com.quimia.android.data.auth

import android.content.Context
import com.quimia.android.presentation.screens.auth.GoogleOnboarding
import com.quimia.android.presentation.screens.profile.LocalProfileStore
import com.quimia.android.presentation.screens.profile.ProfileDetails
import com.quimia.android.presentation.screens.profile.profileStorageScope
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class GoogleOnboardingStore(private val context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("quimia_google_onboarding", Context.MODE_PRIVATE)
    fun completed(profile: GoogleOnboarding): Boolean = profile.valid && preferences.getBoolean(profileStorageScope(profile.uid, profile.email) + ".completed", false)
    fun pending(uid: String, email: String): Boolean {
        val key = profileStorageScope(uid, email) + ".completed"
        return preferences.contains(key) && !preferences.getBoolean(key, false)
    }
    fun clearPending(uid: String, email: String) {
        if (pending(uid, email)) preferences.edit().remove(profileStorageScope(uid, email) + ".completed").apply()
    }
    fun markPending(uid: String, email: String) {
        preferences.edit().putBoolean(profileStorageScope(uid, email) + ".completed", false).apply()
    }
    fun complete(profile: GoogleOnboarding, address: NearbyAddress?) {
        require(profile.valid && profile.addressStep)
        require(address == null || address.validationError() == null)
        LocalProfileStore(context, profile.uid, profile.email).saveDetails(ProfileDetails(profile.name, profile.birthDate, profile.email))
        val scope = profileStorageScope(profile.uid, profile.email)
        val editor = preferences.edit().putBoolean("$scope.completed", true)
        if (address != null) editor.putString("$scope.address", Json.encodeToString(listOf(address.postalCode, address.state,
            address.city, address.district, address.street, address.number, address.complement)))
        check(editor.commit()) { "Não foi possível salvar seus dados neste aparelho. Tente novamente." }
    }
    fun cacheAddress(uid: String, email: String, address: NearbyAddress) {
        val scope = profileStorageScope(uid, email)
        check(preferences.edit().putString("$scope.address", Json.encodeToString(listOf(address.postalCode, address.state,
            address.city, address.district, address.street, address.number, address.complement))).commit()) {
            "A conta foi criada, mas não foi possível guardar o endereço neste aparelho. Entre novamente."
        }
    }
    fun addressFields(uid: String, email: String): List<String> = runCatching {
        Json.decodeFromString<List<String>>(preferences.getString(profileStorageScope(uid, email) + ".address", "[]").orEmpty()).takeIf { it.size == 7 }.orEmpty()
    }.getOrDefault(emptyList())
}
