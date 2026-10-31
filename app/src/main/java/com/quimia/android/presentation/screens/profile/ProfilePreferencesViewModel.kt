package com.quimia.android.presentation.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.quimia.android.data.preferences.RoomProfilePreferencesRepository
import com.quimia.android.domain.preferences.ProfilePreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class ProfilePreferencesState(
    val preferences: ProfilePreferences = ProfilePreferences(),
    val ready: Boolean = false,
    val error: String? = null,
)

internal class ProfilePreferencesViewModel(private val repository: ProfilePreferencesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfilePreferencesState())
    val state = mutableState.asStateFlow()
    private val updates = Channel<ProfilePreferences>(Channel.CONFLATED)
    private var loadJob: Job? = null

    init {
        reload()
        // One writer keeps fast slider/theme changes ordered; the latest complete settings always win.
        viewModelScope.launch {
            for (preferences in updates) try {
                repository.save(preferences)
                mutableState.update { if (it.preferences == preferences) it.copy(error = null) else it }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update {
                    if (it.preferences == preferences) it.copy(error = "Não foi possível salvar os ajustes neste aparelho. Tente novamente.") else it
                }
            }
        }
    }

    fun reload() {
        if (state.value.ready || loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            mutableState.update { it.copy(error = null) }
            try {
                mutableState.value = ProfilePreferencesState(repository.load(), ready = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(error = "Não foi possível carregar os ajustes salvos neste aparelho.") }
            }
        }
    }

    fun update(value: ProfilePreferences) {
        if (!state.value.ready) return
        val normalized = value.copy(textSizeStep = value.textSizeStep.coerceIn(0, 2))
        mutableState.update { it.copy(preferences = normalized, error = null) }
        updates.trySend(normalized)
    }

    fun dismissError() { mutableState.update { it.copy(error = null) } }
    fun retrySave() { if (state.value.ready) update(state.value.preferences) }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val application = context.applicationContext
            return viewModelFactory { initializer { ProfilePreferencesViewModel(RoomProfilePreferencesRepository(application)) } }
        }
    }
}
