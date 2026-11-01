package com.quimia.android.data.local.preferences

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.quimia.android.domain.preferences.ProfilePreferences
import com.quimia.android.domain.preferences.ProfileTheme

/** Device-wide interface settings; account data and session tokens do not belong in this table. */
@Entity(tableName = "accessibility_preferences")
internal data class AccessibilityPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val theme: String,
    val textSizeStep: Int,
    val dyslexicFont: Boolean,
    val soundEffects: Boolean,
    @ColumnInfo(defaultValue = "0") val mixtureNotifications: Boolean = false,
    @ColumnInfo(defaultValue = "0") val shelfReminders: Boolean = false,
) {
    fun toPreferences() = ProfilePreferences(
        ProfileTheme.fromStored(theme), textSizeStep.coerceIn(0, 2), dyslexicFont, soundEffects,
        mixtureNotifications, shelfReminders,
    )

    companion object {
        fun from(value: ProfilePreferences) = AccessibilityPreferencesEntity(
            theme = value.theme.name, textSizeStep = value.textSizeStep.coerceIn(0, 2),
            dyslexicFont = value.dyslexicFont, soundEffects = value.soundEffects,
            mixtureNotifications = value.mixtureNotifications, shelfReminders = value.shelfReminders,
        )
    }
}
