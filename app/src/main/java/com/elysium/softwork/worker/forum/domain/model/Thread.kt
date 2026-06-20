package com.elysium.softwork.worker.forum.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A forum discussion thread — the core topic entity, and the offline-first cache row.
 *
 * Serializer-agnostic: property names match the backend wire keys exactly, so Gson resolves
 * them by reflection without `@SerializedName`. The request/response naming asymmetries
 * (`areaCompanyId`/`area_company_id`, `lastMessage`/`last_message`, `categoryId`/`category_id`,
 * `messageCount`/`message_count`) coexist as nullable fields and a given endpoint fills only
 * its subset.
 *
 * **Documented Room exception (mirrors the former `Post`).** This entity doubles as the
 * `threads` cache row so the feed renders offline-first without a per-emission mapper. Per
 * the established cache convention, the primary key [thread_id] is a **non-null** `Long`
 * defaulting to `0L` (Room rejects nullable primary keys); Gson overwrites the default with
 * the real id during deserialization. All other columns stay nullable to match the wire.
 *
 * @property thread_id primary key (cache row id + backend `thread_id`).
 * @property title thread headline shown in the feed.
 * @property areaCompanyId owning area on the **create request** (`areaCompanyId`).
 * @property area_company_id owning area on the **response** (`area_company_id`).
 * @property lastMessage last-activity date on the **create request** (`lastMessage`).
 * @property last_message last-activity date on the **response** (`last_message`).
 * @property categoryId owning category on the **create request** (`categoryId`).
 * @property category_id owning category on the **response** (`category_id`).
 * @property messageCount reply count on the **create request** (`messageCount`).
 * @property message_count reply count on the **response** (`message_count`).
 */
@Entity(tableName = "threads")
data class Thread(
    @PrimaryKey
    val thread_id: Long = 0L,
    val title: String? = null,
    val areaCompanyId: Long? = null,
    val area_company_id: Long? = null,
    val lastMessage: String? = null,
    val last_message: String? = null,
    val categoryId: Long? = null,
    val category_id: Long? = null,
    val messageCount: Int? = null,
    val message_count: Int? = null,
)
