package com.elysium.softwork.forum.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elysium.softwork.forum.domain.model.Post

/**
 * Room database for the Forum bounded context. Owns the cached [Post] table that the
 * [com.elysium.softwork.forum.data.store.PostStoreImpl] writes after each network refresh
 * and that the UI observes through [PostDao.getAllPosts].
 *
 * Phase 4 ships version 1 with destructive migrations enabled — the schema is small and
 * pre-launch, so dropping the table on schema change is acceptable. Switch to explicit
 * migrations once the app reaches beta.
 */
@Database(entities = [Post::class], version = 1, exportSchema = false)
abstract class ForumDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

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
