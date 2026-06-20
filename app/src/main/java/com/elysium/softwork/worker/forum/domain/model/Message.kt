package com.elysium.softwork.worker.forum.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An individual message within a [Thread] — and the offline-first cache row for replies.
 *
 * Serializer-agnostic: property names match the backend wire keys exactly, so Gson resolves
 * them by reflection without `@SerializedName`. The request/response naming asymmetries
 * (`userAccountId`/`user_account_id`, `contentMessage`/`content_message`,
 * `threadId`/`thread_id`) coexist as nullable fields and a given endpoint fills only its
 * subset.
 *
 * **Documented Room exception (mirrors the former `Post`).** This entity doubles as the
 * `messages` cache row. Per the established cache convention, the primary key [message_id]
 * is a **non-null** `Long` defaulting to `0L` (Room rejects nullable primary keys); Gson
 * overwrites the default with the real id during deserialization. All other columns stay
 * nullable to match the wire.
 *
 * @property message_id primary key (cache row id + backend `message_id`).
 * @property userAccountId author on the **create request** (`userAccountId`).
 * @property user_account_id author on the **response** (`user_account_id`).
 * @property contentMessage body on the **create request** (`contentMessage`).
 * @property content_message body on the **response** (`content_message`).
 * @property threadId owning thread on the **create request** (`threadId`).
 * @property thread_id owning thread on the **response** (`thread_id`); used to filter the
 *   message set for a given thread.
 */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val message_id: Long = 0L,
    val userAccountId: Long? = null,
    val user_account_id: Long? = null,
    val contentMessage: String? = null,
    val content_message: String? = null,
    val threadId: Long? = null,
    val thread_id: Long? = null,
)
