package com.quimia.android.data.local.preferences

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface AccessibilityPreferencesDao {
    @Query("SELECT * FROM accessibility_preferences WHERE id = 1")
    suspend fun read(): AccessibilityPreferencesEntity?

    @Upsert
    suspend fun write(value: AccessibilityPreferencesEntity)
}
