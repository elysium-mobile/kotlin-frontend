package com.elysium.softwork.worker.forum.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.data.network.BadRequestException
import com.elysium.softwork.shared.utils.values.ReportArea
import com.elysium.softwork.shared.utils.values.ReportType
import com.elysium.softwork.worker.forum.application.usecase.SubmitForumReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the forum-report screen.
 *
 * Buffers the report form and projects the submission lifecycle into [state]. Report assembly
 * and the `user_account_id` binding are delegated to [SubmitForumReportUseCase]; the selected
 * irregularity [ReportType] maps to the backend `reason`, the free text to `description`, and
 * the date to `reportDate`. A backend `400` is parsed via [BadRequestException] into the
 * form's [ReportFormState.error].
 *
 * @param submitForumReport assembles and submits the report.
 * @param contextId identifier of the thread the report was opened from (navigation context;
 *   the backend `Report` does not store a post reference).
 */
class ForumReportViewModel(
    private val submitForumReport: SubmitForumReportUseCase,
    @Suppress("unused") private val contextId: String,
) : ViewModel() {

    /** Current state of the report form. */
    data class ReportFormState(
        val type: String = "",
        val area: String = "",
        val description: String = "",
        val date: String = "",
        val isAnonymous: Boolean = true,
        val isSubmitting: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false,
    ) {
        val isValid: Boolean
            get() = type.isNotBlank() && area.isNotBlank() && description.isNotBlank() && date.isNotBlank()
    }

    private val _state = MutableStateFlow(ReportFormState())
    val state: StateFlow<ReportFormState> = _state.asStateFlow()

    /** Areas selectable in the dropdown (key + localized label). */
    val areas: List<ReportArea> = ReportArea.entries

    /** Irregularity types selectable in the chip grid (key + localized label). */
    val reportTypes: List<ReportType> = ReportType.entries

    fun onTypeSelected(type: String) {
        _state.value = _state.value.copy(type = type)
    }

    fun onAreaSelected(area: String) {
        _state.value = _state.value.copy(area = area)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onDateChange(date: String) {
        _state.value = _state.value.copy(date = date)
    }

    /** Submits the report through the use case. No-ops when invalid or already in flight. */
    fun submitReport() {
        val current = _state.value
        if (!current.isValid || current.isSubmitting) return

        _state.value = _state.value.copy(isSubmitting = true, error = null)

        viewModelScope.launch {
            val result = submitForumReport(
                reason = current.type,
                description = current.description,
                reportDate = current.date,
            )
            _state.value = result.fold(
                onSuccess = { _state.value.copy(isSubmitting = false, isSuccess = true) },
                onFailure = { _state.value.copy(isSubmitting = false, error = resolveError(it)) },
            )
        }
    }

    /** Resets the success state after it's been handled by the UI. */
    fun consumeSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }

    private fun resolveError(throwable: Throwable): String = when (throwable) {
        is BadRequestException -> throwable.response.primaryFieldError() ?: GENERIC_ERROR
        else -> throwable.message ?: GENERIC_ERROR
    }

    companion object {
        private const val GENERIC_ERROR: String = "Could not submit the report"

        /**
         * Factory for [ForumReportViewModel].
         *
         * @param contextId the id of the thread the report was opened from.
         */
        fun provideFactory(contextId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                val locator = app.serviceLocator
                return ForumReportViewModel(
                    submitForumReport = SubmitForumReportUseCase(
                        store = locator.forumReportStore,
                        prefs = locator.sharedPrefsManager,
                    ),
                    contextId = contextId,
                ) as T
            }
        }
    }
}
