package com.quimia.android.data.local.preferences

import androidx.room.Entity
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
) {
    fun toPreferences() = ProfilePreferences(
        ProfileTheme.fromStored(theme), textSizeStep.coerceIn(0, 2), dyslexicFont, soundEffects,
    )

    companion object {
        fun from(value: ProfilePreferences) = AccessibilityPreferencesEntity(
            theme = value.theme.name, textSizeStep = value.textSizeStep.coerceIn(0, 2),
            dyslexicFont = value.dyslexicFont, soundEffects = value.soundEffects,
        )
    }
}
