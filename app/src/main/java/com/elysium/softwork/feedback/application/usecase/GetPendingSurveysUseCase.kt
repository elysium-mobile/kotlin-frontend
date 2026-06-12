package com.elysium.softwork.feedback.application.usecase

import com.elysium.softwork.feedback.data.store.SurveyStore
import com.elysium.softwork.feedback.domain.model.Survey
import kotlinx.coroutines.flow.Flow

/**
 * Streams the catalogue of surveys the worker has not answered yet.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store survey data port.
 */
class GetPendingSurveysUseCase(private val store: SurveyStore) {

    /** @return flow of pending surveys, re-emitting on every catalogue change. */
    operator fun invoke(): Flow<List<Survey>> = store.getPendingSurveys()
}
