package com.elysium.softwork.worker.forum.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Forum post entity backing the offline-first feed.
 *
 * Serializer-agnostic: property names match the backend wire keys exactly, so the data
 * layer's JSON serializer resolves them by reflection without mapping annotations.
 *
 * **Deliberate exception — Room annotations stay.** This entity doubles as the local cache
 * row (`posts` table). Splitting it into a pure domain class plus a separate cache entity
 * would require a mapper executing on every feed emission, doubling object allocations on
 * the hot scroll path of the lowest-end target devices. The Room coupling is therefore
 * accepted and contained: `@Entity`/`@PrimaryKey` are structural metadata with no runtime
 * behavior of their own, and no other domain entity carries them.
 *
 * Default values let constructors omit fields the server fills in (e.g. on create, the
 * client doesn't yet know [id] / [timestamp] / [repliesCount]).
 *
 * @property id server-assigned identifier; primary key in the local cache.
 * @property authorName display name of the author. Ignored by the UI when [isAnonymous].
 * @property isAnonymous when true the UI replaces the author block with the Anonymous badge.
 * @property title post title, shown in the feed and at the top of the thread detail.
 * @property content post body.
 * @property category one of `com.elysium.softwork.shared.utils.values.ForumCategory` keys.
 * @property timestamp epoch millis when the server accepted the post.
 * @property repliesCount cached reply count surfaced in the feed footer.
 */
@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val id: String,
    val authorName: String = "",
    val isAnonymous: Boolean = false,
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val timestamp: Long = 0L,
    val repliesCount: Int = 0,
)
