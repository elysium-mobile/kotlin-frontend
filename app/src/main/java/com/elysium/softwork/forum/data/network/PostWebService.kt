package com.elysium.softwork.forum.data.network

import com.elysium.softwork.forum.domain.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit contract for forum endpoints. Per the bean/pragmatic-shortcut pattern, the same
 * [Post] data class flows through both directions of the wire — the server fills in `id`,
 * `timestamp`, and `repliesCount` on the response.
 */
interface PostWebService {

    /** Returns the full feed, server-sorted by timestamp descending. */
    @GET("posts")
    suspend fun list(): Response<List<Post>>

    /** Submits a new post. The server response carries the assigned id + timestamp. */
    @POST("posts")
    suspend fun create(@Body post: Post): Response<Post>
}
