package com.elysium.softwork.worker.forum.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elysium.softwork.worker.forum.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for cached forum [Message]s. The thread-detail screen observes the messages for a
 * single thread so replies render offline-first and update when `ForumStoreImpl` upserts new
 * rows after a refresh or a successful post.
 */
@Dao
interface MessageDao {

    /** Cached messages for [threadId], oldest first. */
    @Query("SELECT * FROM messages WHERE thread_id = :threadId ORDER BY message_id ASC")
    fun observeForThread(threadId: Long): Flow<List<Message>>

    /** Replaces existing rows on PK conflict, so a single call handles insert and update. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<Message>)

    /** Convenience for a single upsert. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: Message)
}
