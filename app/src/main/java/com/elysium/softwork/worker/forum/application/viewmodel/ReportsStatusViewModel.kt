package com.elysium.softwork.worker.forum.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.worker.forum.domain.model.ForumReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State holder for the reports-status screen (the new home "My reports" destination).
 *
 * Triggers a [ForumReportStore.list] call on construction and exposes the result through a
 * single [UiState] stream so the screen can render loading / empty / error / list states
 * without juggling multiple flags.
 */
class ReportsStatusViewModel(
    private val store: ForumReportStore,
) : ViewModel() {

    /** Coarse UI state for the reports list. */
    sealed interface UiState {
        data object Loading : UiState
        data class Ready(val reports: List<ForumReport>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Re-fetches the reports list. Safe to call from a pull-to-refresh affordance. */
    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = store.list().fold(
                onSuccess = { UiState.Ready(it) },
                onFailure = { UiState.Error(it.message ?: GENERIC_ERROR) },
            )
        }
    }

    companion object {
        private const val GENERIC_ERROR: String = "Could not load reports"

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SoftWorkApplication
                return ReportsStatusViewModel(store = app.serviceLocator.forumReportStore) as T
            }
        }
    }
}
