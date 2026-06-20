package com.elysium.softwork.feedback.data.network

import com.elysium.softwork.feedback.domain.model.QuestionSurvey
import com.elysium.softwork.feedback.domain.model.Survey
import com.elysium.softwork.feedback.domain.model.SurveyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit contract for the live Feedback endpoints of the FlowWork Spring Boot API.
 *
 * The same annotation-free domain beans ([Survey], [QuestionSurvey], [SurveyResponse]) carry
 * both request bodies and response payloads (the bean shortcut) — no DTOs. All paths are
 * **relative**; the host + `/` base lives in `BuildConfig.BACKEND_BASE_URL` (resolved by
 * `ApiClient`), and `AuthInterceptor` attaches the bearer token automatically.
 */
interface SurveyWebService {

    // region Surveys
    /** Lists every survey. Backs the pending-surveys feed. */
    @GET("api/v1/surveys")
    suspend fun getSurveys(): Response<List<Survey>>

    /** Fetches a single survey by its `survey_id`. */
    @GET("api/v1/surveys/{id}")
    suspend fun getSurvey(@Path("id") id: Long): Response<Survey>
    // endregion

    // region Question Surveys
    /** Lists every survey question; filtered client-side by `survey_id` for a given survey. */
    @GET("api/v1/question-surveys")
    suspend fun getQuestionSurveys(): Response<List<QuestionSurvey>>

    /** Fetches a single question by its `question_survey_id`. */
    @GET("api/v1/question-surveys/{id}")
    suspend fun getQuestionSurvey(@Path("id") id: Long): Response<QuestionSurvey>

    /** Creates a new survey question. */
    @POST("api/v1/question-surveys")
    suspend fun createQuestionSurvey(@Body question: QuestionSurvey): Response<QuestionSurvey>
    // endregion

    // region Survey Responses
    /** Submits a worker's survey response. */
    @POST("api/v1/survey-responses")
    suspend fun createSurveyResponse(@Body response: SurveyResponse): Response<SurveyResponse>

    /** Lists every survey response. */
    @GET("api/v1/survey-responses")
    suspend fun getSurveyResponses(): Response<List<SurveyResponse>>

    /** Lists the responses submitted for a specific survey. */
    @GET("api/v1/survey-responses/survey/{surveyId}")
    suspend fun getSurveyResponsesBySurvey(@Path("surveyId") surveyId: Long): Response<List<SurveyResponse>>
    // endregion
}
