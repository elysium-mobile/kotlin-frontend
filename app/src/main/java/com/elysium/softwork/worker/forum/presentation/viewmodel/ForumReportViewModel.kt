package com.elysium.softwork.worker.forum.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
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
 * Buffers the report form and projects the submission lifecycle into [state]. Report
 * assembly and dispatch are delegated to [SubmitForumReportUseCase]; the ViewModel only
 * decides *when* to submit (validity + re-entrance gates).
 *
 * @param submitForumReport assembles and submits the report.
 * @param postId identifier of the post being reported, fixed for the screen's lifetime.
 */
class ForumReportViewModel(
    private val submitForumReport: SubmitForumReportUseCase,
    private val postId: String,
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

    /**
     * Areas selectable in the dropdown. Each entry pairs a locale-independent wire key
     * with a localized label resource — the screen renders the label via `stringResource`
     * and persists the key in [ReportFormState.area].
     */
    val areas: List<ReportArea> = ReportArea.entries

    /** Irregularity types selectable in the chip grid. Same key/label contract as [areas]. */
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
                postId = postId,
                typeKey = current.type,
                areaKey = current.area,
                description = current.description,
                date = current.date,
                isAnonymous = current.isAnonymous,
            )
            _state.value = result.fold(
                onSuccess = { _state.value.copy(isSubmitting = false, isSuccess = true) },
                onFailure = { _state.value.copy(isSubmitting = false, error = it.message) },
            )
        }
    }

    /** Resets the success state after it's been handled by the UI. */
    fun consumeSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }

    companion object {
        /**
         * Factory for [ForumReportViewModel].
         *
         * @param postId the ID of the post being reported.
         */
        fun provideFactory(postId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return ForumReportViewModel(
                    submitForumReport = SubmitForumReportUseCase(app.serviceLocator.forumReportStore),
                    postId = postId,
                ) as T
            }
        }
    }
}
