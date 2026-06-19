package com.elysium.softwork.feedback.domain.model

/**
 * A worker's submission to a [Survey] — the annotation-free bean for the `survey-responses`
 * endpoints (the *Bean / Pragmatic Shortcut*).
 *
 * Property names mirror the backend wire keys exactly so Gson maps them by reflection without
 * `@SerializedName`. The create request uses camelCase while the response comes back in
 * snake_case, so both spellings of each concept coexist as nullable fields:
 * - **survey**: request key `surveyId`, response key `survey_id`.
 * - **employee profile**: request key `employeeProfileId`, response key `employee_profile_id`.
 * - **submission date**: request key `submittedAt`, response key `submitted_at`.
 *
 * When building a POST body, populate the camelCase request keys (`surveyId`,
 * `employeeProfileId`, `submittedAt`) plus [commentary] / [cause]; the snake_case fields are
 * filled on the way back.
 *
 * @property survey_response_id primary key returned for a stored submission.
 * @property surveyId target survey on the **create request** (`surveyId`).
 * @property survey_id target survey on the **response** (`survey_id`).
 * @property employeeProfileId author profile on the **create request** (`employeeProfileId`).
 * @property employee_profile_id author profile on the **response** (`employee_profile_id`).
 * @property submittedAt submission date on the **create request** (`submittedAt`).
 * @property submitted_at submission date on the **response** (`submitted_at`).
 * @property commentary free-text feedback (consistent request/response key).
 * @property cause categorized reason (consistent request/response key).
 */
data class SurveyResponse(
    val survey_response_id: Long? = null,
    val surveyId: Long? = null,
    val survey_id: Long? = null,
    val employeeProfileId: Long? = null,
    val employee_profile_id: Long? = null,
    val submittedAt: String? = null,
    val submitted_at: String? = null,
    val commentary: String? = null,
    val cause: String? = null,
)
