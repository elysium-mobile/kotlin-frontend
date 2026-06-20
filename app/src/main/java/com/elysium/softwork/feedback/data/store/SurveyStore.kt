package com.elysium.softwork.feedback.data.store

import com.elysium.softwork.feedback.domain.model.QuestionSurvey
import com.elysium.softwork.feedback.domain.model.Survey
import com.elysium.softwork.feedback.domain.model.SurveyResponse
import kotlinx.coroutines.flow.Flow

/**
 * Feedback data port for the survey lifecycle (catalogue, questions, submissions).
 *
 * Read of the pending list stays a [Flow] so the UI re-renders on every catalogue refresh;
 * the detail-side operations are suspending and return [Result] so callers get a single
 * error channel — a `400 Bad Request` surfaces as a
 * [com.elysium.softwork.shared.data.network.BadRequestException] carrying the parsed
 * `field_errors`.
 */
interface SurveyStore {

    /** Streams the surveys the worker can answer (`GET /api/v1/surveys`). */
    fun getPendingSurveys(): Flow<List<Survey>>

    /** Fetches the dynamic question set for [surveyId] (`GET /api/v1/question-surveys`, filtered). */
    suspend fun getSurveyQuestions(surveyId: Long): Result<List<QuestionSurvey>>

    /**
     * Lists previous submissions for [surveyId]
     * (`GET /api/v1/survey-responses/survey/{surveyId}`).
     */
    suspend fun getSurveyResponses(surveyId: Long): Result<List<SurveyResponse>>

    /** Submits a worker's [response] (`POST /api/v1/survey-responses`). */
    suspend fun submitSurveyResponse(response: SurveyResponse): Result<SurveyResponse>
}
