package com.elysium.softwork.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.application.usecase.LoadAnonymityPreferencesUseCase
import com.elysium.softwork.shared.application.usecase.SaveAnonymityPreferencesUseCase
import com.elysium.softwork.shared.domain.identity.AnonymityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI state holder for the protected-identity screen.
 *
 * Hosts the in-memory edit buffer: the initial snapshot is loaded once through
 * [LoadAnonymityPreferencesUseCase]; toggle handlers mutate the buffer only.
 * Persistence happens explicitly when the user taps "Save preferences" → [save],
 * which delegates the full snapshot to [SaveAnonymityPreferencesUseCase]. This matches
 * the explicit-save UX of the screen and lets the user back out of unintended changes
 * by simply navigating back.
 *
 * @param loadPreferences reads the persisted snapshot once on construction.
 * @param savePreferences persists the buffered snapshot atomically.
 */
class AnonymityViewModel(
    loadPreferences: LoadAnonymityPreferencesUseCase,
    private val savePreferences: SaveAnonymityPreferencesUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<AnonymityPreferences> = MutableStateFlow(loadPreferences())
    val state: StateFlow<AnonymityPreferences> = _state.asStateFlow()

    fun setGlobal(value: Boolean) {
        _state.value = _state.value.copy(global = value)
    }

    fun setForum(value: Boolean) {
        _state.value = _state.value.copy(forum = value)
    }

    fun setSurveys(value: Boolean) {
        _state.value = _state.value.copy(surveys = value)
    }

    fun setReports(value: Boolean) {
        _state.value = _state.value.copy(reports = value)
    }

    /** Flushes the in-memory buffer through the save use case. Idempotent. */
    fun save() {
        savePreferences(_state.value)
    }

    companion object {
        /** Factory that assembles the use cases from the application service locator. */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                val prefs = application.serviceLocator.sharedPrefsManager
                return AnonymityViewModel(
                    loadPreferences = LoadAnonymityPreferencesUseCase(prefs),
                    savePreferences = SaveAnonymityPreferencesUseCase(prefs),
                ) as T
            }
        }
    }
}
