package com.elysium.softwork.worker.forum.domain.model

/**
 * A content/conduct report — the annotation-free bean for the `reports` endpoints (the
 * *Bean / Pragmatic Shortcut*). Replaces the former flat `ForumReport`.
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The reporting account, date, and area arrive under camelCase
 * keys on the request and snake_case on the response, so both spellings coexist as nullable
 * fields.
 *
 * @property report_id primary key returned by every report response.
 * @property reason short categorized reason for the report.
 * @property description detailed explanation.
 * @property userAccountId reporting account on the **create request** (`userAccountId`).
 * @property user_account_id reporting account on the **response** (`user_account_id`).
 * @property reportDate incident/report date on the **create request** (`reportDate`).
 * @property report_date incident/report date on the **response** (`report_date`).
 * @property areaCompanyId reported area on the **create request** (`areaCompanyId`).
 * @property area_company_id reported area on the **response** (`area_company_id`).
 */
data class Report(
    val report_id: Long? = null,
    val reason: String? = null,
    val description: String? = null,
    val userAccountId: Long? = null,
    val user_account_id: Long? = null,
    val reportDate: String? = null,
    val report_date: String? = null,
    val areaCompanyId: Long? = null,
    val area_company_id: Long? = null,
)
