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
 * - [refresh] is **mocked** for the UI-testing phase: it skips the Retrofit call entirely
 *   and seeds the Room cache with [SeedPosts] when empty so the Forum screen always has
 *   data to render. Re-enable the network branch (commented inline) when the `/posts`
 *   endpoint is live.
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
        // Mocked: bypass Retrofit entirely and ensure the cache holds the seed set. When the
        // backend is reachable, restore the original implementation:
        //     val response = webService.list()
        //     if (response.isSuccessful) response.body()?.let { dao.upsertAll(it) }
        if (dao.count() == 0) {
            dao.upsertAll(SeedPosts)
        }
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
            Post(
                id = "seed-4",
                authorName = "Cesar",
                isAnonymous = false,
                title = "Bienvenidos al foro interno",
                content = "Comparte aquí tus ideas y conflictos con el equipo.",
                category = "suggestions",
                timestamp = System.currentTimeMillis() - 1_000L * 60 * 5,
                repliesCount = 3,
            ),
        )
    }
}
