package com.elysium.softwork.forum.data.store

import com.elysium.softwork.forum.data.local.PostDao
import com.elysium.softwork.forum.data.network.PostWebService
import com.elysium.softwork.forum.domain.model.Post
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Offline-first [PostStore].
 *
 * - [observe] returns the Room [Flow] directly so the UI always renders the cached snapshot.
 * - [refresh] tries the network and upserts on success. **Fallback**: the first time the
 *   table is empty after a failed refresh, a small set of bundled sample posts is seeded so
 *   the demo flow keeps working without a backend. Remove the seed when the API is live.
 * - [publish] tries the network; on success the server-issued [Post] is upserted. On failure
 *   the post is still inserted locally with a generated UUID so the user sees their content
 *   immediately — the next [refresh] will reconcile with the server.
 */
class PostStoreImpl(
    private val dao: PostDao,
    private val webService: PostWebService,
) : PostStore {

    override fun observe(): Flow<List<Post>> = dao.getAllPosts()

    override suspend fun refresh(): Result<Unit> = runCatching {
        try {
            val response = webService.list()
            if (response.isSuccessful) {
                response.body()?.let { dao.upsertAll(it) }
                return@runCatching
            }
        } catch (_: Throwable) {
            // Fall through to the seed branch below.
        }
        // Network unreachable or non-2xx: seed once so the UI has something to render.
        if (dao.count() == 0) {
            dao.upsertAll(SeedPosts)
        }
        error("Network refresh failed; cache unchanged or seeded.")
    }

    override suspend fun getById(id: String): Post? = dao.getById(id)

    override suspend fun publish(
        title: String,
        content: String,
        category: String,
        authorName: String,
        isAnonymous: Boolean,
    ): Result<Post> = runCatching {
        val draft = Post(
            id = UUID.randomUUID().toString(),
            authorName = authorName,
            isAnonymous = isAnonymous,
            title = title,
            content = content,
            category = category,
            timestamp = System.currentTimeMillis(),
            repliesCount = 0,
        )
        val persisted: Post = try {
            val response = webService.create(draft)
            if (response.isSuccessful) response.body() ?: draft else draft
        } catch (_: Throwable) {
            draft
        }
        dao.upsert(persisted)
        persisted
    }

    companion object {
        /**
         * Bundled sample posts inserted only when the cache is empty after a failed network
         * refresh. Removed once the backend is live (see KDoc on the class).
         */
        private val SeedPosts: List<Post> = listOf(
            Post(
                id = "seed-1",
                authorName = "Ana García",
                isAnonymous = false,
                title = "Propuesta para mejorar las salas de reuniones",
                content = "Las salas pequeñas se llenan rápido. ¿Podríamos reservar mejor?",
                category = "suggestions",
                timestamp = System.currentTimeMillis() - 1_000L * 60 * 30,
                repliesCount = 14,
            ),
            Post(
                id = "seed-2",
                authorName = "",
                isAnonymous = true,
                title = "¿Cómo solicito días de teletrabajo extra?",
                content = "No encuentro el formulario en el portal interno.",
                category = "questions",
                timestamp = System.currentTimeMillis() - 1_000L * 60 * 60 * 2,
                repliesCount = 6,
            ),
            Post(
                id = "seed-3",
                authorName = "Luis Méndez",
                isAnonymous = false,
                title = "After-office del viernes",
                content = "Confirmen asistencia para reservar mesas en el lugar habitual.",
                category = "events",
                timestamp = System.currentTimeMillis() - 1_000L * 60 * 60 * 24,
                repliesCount = 22,
            ),
        )
    }
}
