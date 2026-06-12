package com.elysium.softwork.feedback.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.feedback.application.usecase.GetPendingSurveysUseCase
import com.elysium.softwork.feedback.domain.model.Survey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the pending-surveys screen.
 *
 * Subscribes to [GetPendingSurveysUseCase] on construction and surfaces the latest
 * snapshot through the read-only [surveys] stream for the Compose layer to render.
 * No business logic lives here.
 *
 * @param getPendingSurveys streams the catalogue of unanswered surveys.
 */
class PendingSurveysViewModel(getPendingSurveys: GetPendingSurveysUseCase) : ViewModel() {

    private val _surveys: MutableStateFlow<List<Survey>> = MutableStateFlow(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys.asStateFlow()

    init {
        viewModelScope.launch {
            getPendingSurveys().collect { list -> _surveys.value = list }
        }
    }

    companion object {
        /**
         * Factory that assembles the use case from the application service locator.
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
                return PendingSurveysViewModel(
                    getPendingSurveys = GetPendingSurveysUseCase(application.serviceLocator.surveyStore),
                ) as T
            }
        }
    }
}
