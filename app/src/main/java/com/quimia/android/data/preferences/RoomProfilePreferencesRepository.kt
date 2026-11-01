package com.quimia.android.data.preferences

import android.content.Context
import androidx.room.withTransaction
import com.quimia.android.data.local.QuimiaDatabase
import com.quimia.android.data.local.preferences.AccessibilityPreferencesEntity
import com.quimia.android.domain.preferences.ProfilePreferences
import com.quimia.android.domain.preferences.ProfilePreferencesRepository
import com.quimia.android.domain.preferences.ProfileTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RoomProfilePreferencesRepository(
    context: Context,
    private val database: QuimiaDatabase = QuimiaDatabase.getInstance(context),
) : ProfilePreferencesRepository {
    private val legacy = context.applicationContext.getSharedPreferences("quimia_accessibility", Context.MODE_PRIVATE)

    override suspend fun load(): ProfilePreferences = withContext(Dispatchers.IO) {
        // Checking and importing in one transaction prevents a second initialization from replacing newer values.
        val value = database.withTransaction {
            val dao = database.accessibilityPreferences()
            dao.read() ?: AccessibilityPreferencesEntity.from(readLegacy()).also { dao.write(it) }
        }.toPreferences()
        // Only remove old settings after SQLite commits. A failed import leaves the original settings intact.
        legacy.edit().remove("theme").remove("text_size_step").remove("dyslexic_font").remove("sound_effects").commit()
        value
    }

    override suspend fun save(value: ProfilePreferences) = withContext(Dispatchers.IO) {
        database.accessibilityPreferences().write(AccessibilityPreferencesEntity.from(value))
    }

    private fun readLegacy() = ProfilePreferences(
        theme = ProfileTheme.fromStored(legacy.getString("theme", null)),
        textSizeStep = legacy.getInt("text_size_step", 0).coerceIn(0, 2),
        dyslexicFont = legacy.getBoolean("dyslexic_font", false),
        soundEffects = legacy.getBoolean("sound_effects", true),
    )
}
