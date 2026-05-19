package com.elysium.softwork.feedback.data.store

import com.elysium.softwork.feedback.domain.model.Survey
import kotlinx.coroutines.flow.Flow

/**
 * Feedback data port. Phase 5 only models the read-side of the pending-surveys list. Future
 * milestones will add submission endpoints (e.g. `submitAnswers(...)`).
 */
interface SurveyStore {

    /**
     * Observes the list of surveys the worker still has to answer. The mocked implementation
     * emits a static catalogue once; a future network-backed implementation will keep this
     * shape and re-emit when the backend pushes a refresh.
     */
    fun getPendingSurveys(): Flow<List<Survey>>
}
