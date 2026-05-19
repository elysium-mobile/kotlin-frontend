package com.elysium.softwork.feedback.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.feedback.data.store.SurveyStore
import com.elysium.softwork.feedback.domain.model.Survey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Hosts the list of pending surveys for the Feedback bounded context. Subscribes to
 * [SurveyStore.getPendingSurveys] on construction and surfaces the latest snapshot through
 * [surveys] for the Compose layer to render.
 */
class PendingSurveysViewModel(private val surveyStore: SurveyStore) : ViewModel() {

    private val _surveys: MutableStateFlow<List<Survey>> = MutableStateFlow(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys.asStateFlow()

    init {
        viewModelScope.launch {
            surveyStore.getPendingSurveys().collect { list -> _surveys.value = list }
        }
    }

    companion object {
        /**
         * Factory that pulls the [SurveyStore] from the [SoftWorkApplication] service locator.
         *
         * Use it inside Composables:
         * ```
         * val viewModel: PendingSurveysViewModel =
         *     viewModel(factory = PendingSurveysViewModel.Factory)
         * ```
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SoftWorkApplication
                return PendingSurveysViewModel(application.serviceLocator.surveyStore) as T
            }
        }
    }
}
