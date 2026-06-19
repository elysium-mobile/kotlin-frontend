package com.elysium.softwork.feedback.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.feedback.application.usecase.GetPendingSurveysUseCase
import com.elysium.softwork.feedback.application.usecase.SubmitSurveyResponseUseCase
import com.elysium.softwork.feedback.domain.model.Survey
import com.elysium.softwork.shared.data.network.BadRequestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the pending-surveys screen.
 *
 * Subscribes to [GetPendingSurveysUseCase] on construction and surfaces the latest snapshot
 * through the read-only [surveys] stream. The screen can submit a response via [submitResponse];
 * a backend `400 Bad Request` is caught, its [BadRequestException] payload parsed, and the
 * offending `field_errors` message routed into the read-only [errorMessage] state for inline
 * display. No business logic lives here — submission assembly belongs to the use case.
 *
 * @param getPendingSurveys streams the catalogue of unanswered surveys.
 * @param submitSurveyResponse pushes a worker's response to the backend.
 */
class PendingSurveysViewModel(
    getPendingSurveys: GetPendingSurveysUseCase,
    private val submitSurveyResponse: SubmitSurveyResponseUseCase,
) : ViewModel() {

    private val _surveys: MutableStateFlow<List<Survey>> = MutableStateFlow(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys.asStateFlow()

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    /** Latest backend validation / load error, or `null` when none. Cleared via [consumeError]. */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSubmitting: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /** `true` while a submission round-trip is in flight; gates re-entrant submits. */
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                getPendingSurveys().collect { list -> _surveys.value = list }
            }.onFailure { throwable -> _errorMessage.value = resolveError(throwable) }
        }
    }

    /**
     * Submits a response for [surveyId]. No-ops while another submission is in flight. On a
     * `400` the parsed field-validation message lands on [errorMessage].
     */
    fun submitResponse(surveyId: Long, commentary: String, cause: String) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val result = submitSurveyResponse(surveyId = surveyId, commentary = commentary, cause = cause)
            result.onFailure { throwable -> _errorMessage.value = resolveError(throwable) }
            _isSubmitting.value = false
        }
    }

    /** Clears the surfaced error after the UI has shown it. */
    fun consumeError() {
        _errorMessage.value = null
    }

    /**
     * Maps a failure to a user-facing message. A [BadRequestException] yields the parsed
     * `field_errors` message (e.g. a malformed submission); anything else yields the
     * exception message or a generic fallback.
     */
    private fun resolveError(throwable: Throwable): String = when (throwable) {
        is BadRequestException -> throwable.response.primaryFieldError() ?: GENERIC_ERROR
        else -> throwable.message ?: GENERIC_ERROR
    }

    companion object {
        private const val GENERIC_ERROR: String = "Unexpected error"

        /**
         * Factory that assembles the use cases from the application service locator.
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
                val locator = application.serviceLocator
                return PendingSurveysViewModel(
                    getPendingSurveys = GetPendingSurveysUseCase(locator.surveyStore),
                    submitSurveyResponse = SubmitSurveyResponseUseCase(
                        store = locator.surveyStore,
                        prefs = locator.sharedPrefsManager,
                    ),
                ) as T
            }
        }
    }
}
