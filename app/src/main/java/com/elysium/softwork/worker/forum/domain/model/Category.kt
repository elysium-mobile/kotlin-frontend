package com.elysium.softwork.worker.forum.domain.model

/**
 * A forum category — the annotation-free bean for the `categories` endpoints (the
 * *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The owning forum arrives under `forumId` on the request and
 * `forum_id` on the response, so both coexist as nullable fields.
 *
 * @property category_id primary key returned by every category response.
 * @property title category headline.
 * @property description category description.
 * @property forumId owning forum on the **create request** (`forumId`).
 * @property forum_id owning forum on the **response** (`forum_id`).
 */
data class Category(
    val category_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val forumId: Long? = null,
    val forum_id: Long? = null,
)
