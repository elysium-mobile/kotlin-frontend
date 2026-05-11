package com.elysium.softwork.forum.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elysium.softwork.forum.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for cached forum posts. Reads expose a [Flow] so the UI auto-refreshes whenever
 * [PostStoreImpl][com.elysium.softwork.forum.data.store.PostStoreImpl] writes new rows after
 * a network refresh or a successful publishing.
 */
@Dao
interface PostDao {

    /** All posts, newest first. UI observes this stream directly. */
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<Post>>

    /** One-shot lookup used by the thread-detail screen. */
    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Post?

    /** Counts cached rows. The store uses this to decide whether to seed sample data. */
    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    /** Replaces existing rows on PK conflict, so a single call handles both insert and update. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(posts: List<Post>)

    /** Convenience for a single insert. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: Post)
}
