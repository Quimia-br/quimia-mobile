package com.quimia.android.presentation.screens.profile

import com.quimia.android.domain.preferences.ProfilePreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePreferencesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun loadsStoredPreferencesBeforeAllowingChanges() = runTest(dispatcher) {
        val repository = MemoryPreferences(ProfilePreferences(theme = ProfileTheme.Dark, soundEffects = false))
        val model = ProfilePreferencesViewModel(repository)
        assertFalse(model.state.value.ready)
        model.update(ProfilePreferences())
        advanceUntilIdle()
        assertTrue(model.state.value.ready)
        assertEquals(repository.value, model.state.value.preferences)
        assertFalse(model.state.value.preferences.soundEffects)
    }

    @Test fun rapidSliderAndThemeChangesPersistTheFinalCompleteValue() = runTest(dispatcher) {
        val repository = MemoryPreferences()
        val model = ProfilePreferencesViewModel(repository)
        advanceUntilIdle()
        repeat(30) { model.update(ProfilePreferences(textSizeStep = it % 3)) }
        val final = ProfilePreferences(ProfileTheme.HighContrast, 2, dyslexicFont = true, soundEffects = false)
        model.update(final)
        advanceUntilIdle()
        assertEquals(final, repository.value)
        assertEquals(final, model.state.value.preferences)
    }

    @Test fun aFailedSaveIsVisibleAndCanBeRetried() = runTest(dispatcher) {
        val repository = MemoryPreferences()
        val model = ProfilePreferencesViewModel(repository)
        advanceUntilIdle()
        repository.failSave = true
        model.update(ProfilePreferences(theme = ProfileTheme.Dark))
        advanceUntilIdle()
        assertNotNull(model.state.value.error)
        assertEquals(ProfileTheme.Light, repository.value.theme)
        repository.failSave = false
        model.retrySave()
        advanceUntilIdle()
        assertNull(model.state.value.error)
        assertEquals(ProfileTheme.Dark, repository.value.theme)
    }

    @Test fun aFailedLoadDoesNotOverwriteSavedPreferencesWithDefaults() = runTest(dispatcher) {
        val repository = MemoryPreferences(ProfilePreferences(theme = ProfileTheme.Dark)).apply { failLoad = true }
        val model = ProfilePreferencesViewModel(repository)
        advanceUntilIdle()
        assertFalse(model.state.value.ready)
        assertNotNull(model.state.value.error)
        model.update(ProfilePreferences())
        advanceUntilIdle()
        assertEquals(ProfileTheme.Dark, repository.value.theme)
        repository.failLoad = false
        model.reload()
        advanceUntilIdle()
        assertTrue(model.state.value.ready)
        assertEquals(ProfileTheme.Dark, model.state.value.preferences.theme)
    }

    private class MemoryPreferences(var value: ProfilePreferences = ProfilePreferences()) : ProfilePreferencesRepository {
        var failLoad = false
        var failSave = false
        override suspend fun load(): ProfilePreferences {
            check(!failLoad)
            return value
        }
        override suspend fun save(value: ProfilePreferences) {
            check(!failSave)
            this.value = value
        }
    }
}
