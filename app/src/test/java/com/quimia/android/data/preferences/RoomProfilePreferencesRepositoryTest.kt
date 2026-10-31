package com.quimia.android.data.preferences

import android.content.Context
import androidx.room.Room
import com.quimia.android.data.local.QuimiaDatabase
import com.quimia.android.data.local.preferences.AccessibilityPreferencesEntity
import com.quimia.android.domain.preferences.ProfilePreferences
import com.quimia.android.domain.preferences.ProfileTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class RoomProfilePreferencesRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: QuimiaDatabase
    private lateinit var databaseName: String
    private val saved = ProfilePreferences(ProfileTheme.HighContrast, 2, dyslexicFont = true, soundEffects = false)

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        databaseName = "preferences-${UUID.randomUUID()}.db"
        context.getSharedPreferences("quimia_accessibility", Context.MODE_PRIVATE).edit().clear().commit()
        openDatabase()
    }

    private fun openDatabase() {
        database = Room.databaseBuilder(context, QuimiaDatabase::class.java, databaseName).build()
    }

    @After fun cleanup() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test fun firstLaunchCreatesTheDefaultRowInSQLite() = runBlocking {
        val repository = RoomProfilePreferencesRepository(context, database)
        assertEquals(ProfilePreferences(), repository.load())
        assertEquals(ProfilePreferences(), database.accessibilityPreferences().read()?.toPreferences())
        assertTrue(context.getDatabasePath(databaseName).isFile)
    }

    @Test fun importsEveryLegacySettingBeforeRemovingTheOldKeys() = runBlocking {
        val legacy = context.getSharedPreferences("quimia_accessibility", Context.MODE_PRIVATE)
        legacy.edit().putString("theme", "HighContrast").putInt("text_size_step", 2)
            .putBoolean("dyslexic_font", true).putBoolean("sound_effects", false)
            .putString("unrelated", "keep").commit()
        assertEquals(saved, RoomProfilePreferencesRepository(context, database).load())
        assertEquals(saved, database.accessibilityPreferences().read()?.toPreferences())
        assertFalse(legacy.contains("theme"))
        assertFalse(legacy.contains("text_size_step"))
        assertFalse(legacy.contains("dyslexic_font"))
        assertFalse(legacy.contains("sound_effects"))
        assertEquals("keep", legacy.getString("unrelated", null))
    }

    @Test fun reopeningTheDatabaseRestoresAllSettingsIncludingDisabledSounds() = runBlocking {
        val repository = RoomProfilePreferencesRepository(context, database)
        repository.load()
        repository.save(saved)
        database.close()
        openDatabase()
        assertEquals(saved, RoomProfilePreferencesRepository(context, database).load())
    }

    @Test fun oldPreferencesNeverOverwriteAnExistingSQLiteRecord() = runBlocking {
        database.accessibilityPreferences().write(AccessibilityPreferencesEntity.from(saved))
        context.getSharedPreferences("quimia_accessibility", Context.MODE_PRIVATE).edit()
            .putString("theme", "Light").putBoolean("sound_effects", true).commit()
        assertEquals(saved, RoomProfilePreferencesRepository(context, database).load())
    }

    @Test fun failedImportKeepsTheLegacySettingsForRetry() = runBlocking {
        val legacy = context.getSharedPreferences("quimia_accessibility", Context.MODE_PRIVATE)
        legacy.edit().putString("theme", "Dark").putBoolean("sound_effects", false).commit()
        database.close()
        try {
            RoomProfilePreferencesRepository(context, database).load()
            fail("A closed database must not count as a successful import")
        } catch (_: IllegalStateException) { }
        assertEquals("Dark", legacy.getString("theme", null))
        assertFalse(legacy.getBoolean("sound_effects", true))
    }

    @Test fun unknownStoredThemesFallBackAndTextSizeIsClamped() = runBlocking {
        database.accessibilityPreferences().write(AccessibilityPreferencesEntity(
            theme = "RemovedTheme", textSizeStep = 999, dyslexicFont = true, soundEffects = false,
        ))
        assertEquals(saved.copy(theme = ProfileTheme.Light), RoomProfilePreferencesRepository(context, database).load())
    }
}
