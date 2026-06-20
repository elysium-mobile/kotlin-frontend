package com.elysium.softwork.worker.forum.domain.model

/**
 * A forum container — the annotation-free bean for the `forums` endpoints (the
 * *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The owning company arrives under different keys on the request
 * (`companyId`) vs the response (`company_id`), so both coexist as nullable fields.
 *
 * @property forum_id primary key returned by every forum response.
 * @property title forum headline.
 * @property description forum description.
 * @property companyId owning company on the **create request** (`companyId`).
 * @property company_id owning company on the **response** (`company_id`).
 */
data class Forum(
    val forum_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val companyId: Long? = null,
    val company_id: Long? = null,
)
