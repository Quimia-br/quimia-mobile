package com.quimia.android.presentation.screens.mixtures

import android.content.Context
import com.quimia.android.presentation.screens.profile.profileStorageScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MixtureHistoryStore(context: Context, uid: String, email: String) {
    private val preferences = context.applicationContext.getSharedPreferences("quimia_mixture_history", Context.MODE_PRIVATE)
    private val key = profileStorageScope(uid, email)

    fun load(): List<MixtureResult> = runCatching {
        Json.decodeFromString<List<MixtureResult>>(preferences.getString(key, "[]").orEmpty())
            .filter { it.ingredients.size == 2 }.distinctBy { it.id }.sortedByDescending { it.createdAt }
    }.getOrDefault(emptyList())

    fun save(history: List<MixtureResult>) {
        preferences.edit().putString(key, Json.encodeToString(history)).apply()
    }
}
