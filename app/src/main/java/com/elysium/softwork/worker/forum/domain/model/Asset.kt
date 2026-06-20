package com.elysium.softwork.worker.forum.domain.model

/**
 * A file attachment on a [Message] — the annotation-free bean for the `assets` endpoints
 * (the *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The parent message, file size, and file type arrive under
 * camelCase keys on the request and snake_case on the response, so both spellings coexist as
 * nullable fields. [is_viewable] / [is_readable] are response-only flags.
 *
 * @property asset_id primary key returned by every asset response.
 * @property messageId parent message on the **create request** (`messageId`).
 * @property message_id parent message on the **response** (`message_id`).
 * @property name file name.
 * @property url storage URL of the file.
 * @property fileSize size label on the **create request** (`fileSize`).
 * @property file_size size label on the **response** (`file_size`).
 * @property fileType type label on the **create request** (`fileType`).
 * @property file_type type label on the **response** (`file_type`).
 * @property is_viewable response-only: whether the asset can be previewed inline.
 * @property is_readable response-only: whether the asset can be opened.
 */
data class Asset(
    val asset_id: Long? = null,
    val messageId: Long? = null,
    val message_id: Long? = null,
    val name: String? = null,
    val url: String? = null,
    val fileSize: String? = null,
    val file_size: String? = null,
    val fileType: String? = null,
    val file_type: String? = null,
    val is_viewable: Boolean? = null,
    val is_readable: Boolean? = null,
)
