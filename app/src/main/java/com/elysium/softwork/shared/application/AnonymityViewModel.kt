package com.elysium.softwork.shared.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import com.elysium.softwork.shared.domain.identity.AnonymityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hosts the in-memory edit buffer for the protected-identity screen.
 *
 * Initial state is loaded from [SharedPrefsManager] on construction; toggle handlers update
 * the in-memory buffer only. Persistence happens explicitly when the user taps
 * "Guardar preferencias" → [save]. This matches the explicit-save UX of the screen and
 * lets the user back out of unintended changes by simply navigating back.
 */
class AnonymityViewModel(private val prefs: SharedPrefsManager) : ViewModel() {

    private val _state: MutableStateFlow<AnonymityPreferences> = MutableStateFlow(loadFromPrefs())
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

    /** Flushes the in-memory buffer to [SharedPrefsManager]. Idempotent — safe to call twice. */
    fun save() {
        val snapshot: AnonymityPreferences = _state.value
        prefs.putBoolean(SharedPrefsManager.KEY_GLOBAL_ANONYMITY, snapshot.global)
        prefs.putBoolean(SharedPrefsManager.KEY_FORUM_ANONYMITY, snapshot.forum)
        prefs.putBoolean(SharedPrefsManager.KEY_SURVEYS_ANONYMITY, snapshot.surveys)
        prefs.putBoolean(SharedPrefsManager.KEY_REPORTS_ANONYMITY, snapshot.reports)
    }

    private fun loadFromPrefs(): AnonymityPreferences = AnonymityPreferences(
        global = prefs.getBoolean(SharedPrefsManager.KEY_GLOBAL_ANONYMITY),
        forum = prefs.getBoolean(SharedPrefsManager.KEY_FORUM_ANONYMITY),
        surveys = prefs.getBoolean(SharedPrefsManager.KEY_SURVEYS_ANONYMITY),
        reports = prefs.getBoolean(SharedPrefsManager.KEY_REPORTS_ANONYMITY),
    )

    companion object {
        /** Resolves the [SharedPrefsManager] from the application service locator. */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return AnonymityViewModel(application.serviceLocator.sharedPrefsManager) as T
            }
        }
    }
}
