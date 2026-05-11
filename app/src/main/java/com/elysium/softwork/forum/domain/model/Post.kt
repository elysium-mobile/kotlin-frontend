package com.elysium.softwork.forum.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Forum post — single bean acting as the Room entity, the Retrofit response payload, and
 * the Retrofit request body, per the IAM-context bean/pragmatic-shortcut pattern.
 *
 * The same fields therefore carry both the Room column annotations and the Gson
 * `@SerializedName`. Default values let constructors omit fields the server fills in
 * (e.g. on create, the client doesn't yet know [id] / [timestamp] / [repliesCount]).
 *
 * @property id server-assigned identifier; primary key in the local cache.
 * @property authorName display name of the author. Ignored by the UI when [isAnonymous].
 * @property isAnonymous when true the UI replaces the author block with the Anonymous badge.
 * @property title post title, shown in the feed and at the top of the thread detail.
 * @property content post body.
 * @property category one of [com.elysium.softwork.forum.application.ForumCategory] keys.
 * @property timestamp epoch millis when the server accepted the post.
 * @property repliesCount cached reply count surfaced in the feed footer.
 */
@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    @SerializedName("id") val id: String,
    @SerializedName("authorName") val authorName: String = "",
    @SerializedName("isAnonymous") val isAnonymous: Boolean = false,
    @SerializedName("title") val title: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("timestamp") val timestamp: Long = 0L,
    @SerializedName("repliesCount") val repliesCount: Int = 0,
)
