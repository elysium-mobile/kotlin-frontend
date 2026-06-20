package com.elysium.softwork.feedback.domain.model

/**
 * A single question belonging to a [Survey] — the annotation-free bean for the
 * `question-surveys` endpoints (the *Bean / Pragmatic Shortcut*).
 *
 * Property names mirror the backend wire keys exactly so Gson maps them by reflection
 * without `@SerializedName`. The backend's mixed naming strategy returns snake_case while the
 * create request expects camelCase, so the asymmetric keys coexist as nullable fields and
 * each endpoint fills only its subset:
 * - **text**: request key `textQuestion`, response key `text_question`.
 * - **type**: request key `questionType`, response key `question_type`.
 * - **owning survey**: request key `surveyId`, response key `survey_id`.
 *
 * @property question_survey_id primary key returned by every question response.
 * @property textQuestion question text on the **create request** (`textQuestion`).
 * @property text_question question text on the **response** (`text_question`).
 * @property questionType answer type on the **create request** (`questionType`).
 * @property question_type answer type on the **response** (`question_type`).
 * @property surveyId owning survey on the **create request** (`surveyId`).
 * @property survey_id owning survey on the **response** (`survey_id`); used to filter the
 *   question set for a given survey.
 */
data class QuestionSurvey(
    val question_survey_id: Long? = null,
    val textQuestion: String? = null,
    val text_question: String? = null,
    val questionType: String? = null,
    val question_type: String? = null,
    val surveyId: Long? = null,
    val survey_id: Long? = null,
)
