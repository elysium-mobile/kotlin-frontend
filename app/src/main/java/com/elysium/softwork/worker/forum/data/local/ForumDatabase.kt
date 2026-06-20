package com.elysium.softwork.worker.forum.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elysium.softwork.worker.forum.domain.model.Message
import com.elysium.softwork.worker.forum.domain.model.Thread

/**
 * Room database for the Forum bounded context. Owns the offline-first [Thread] and [Message]
 * caches that `ForumStoreImpl` writes after each network refresh and that the UI observes
 * through [ThreadDao.observeThreads] / [MessageDao.observeForThread].
 *
 * Schema version 2 (bumped from the former single `posts` table to the hierarchical
 * `threads` + `messages` tables) ships with destructive migrations — the cache is
 * regenerated from the backend on the next refresh, so dropping it on a schema change is
 * acceptable. Switch to explicit migrations before any production release.
 */
@Database(entities = [Thread::class, Message::class], version = 2, exportSchema = false)
abstract class ForumDatabase : RoomDatabase() {

    abstract fun threadDao(): ThreadDao

    abstract fun messageDao(): MessageDao

    companion object {
        private const val DB_NAME: String = "forum.db"

        /** Builds a process-wide [ForumDatabase] singleton from the application context. */
        fun create(context: Context): ForumDatabase = Room.databaseBuilder(
            context.applicationContext,
            ForumDatabase::class.java,
            DB_NAME,
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }
}
