package com.elysium.softwork.notifications.data.store

import com.elysium.softwork.notifications.data.network.NotificationWebService
import com.elysium.softwork.notifications.domain.model.Notification
import com.elysium.softwork.notifications.domain.model.NotificationDetail
import com.elysium.softwork.shared.data.network.BadRequestException
import com.elysium.softwork.shared.data.network.BadRequestResponse
import com.google.gson.Gson
import retrofit2.Response

/**
 * Concrete [NotificationStore] backed by the live FlowWork Spring Boot API.
 *
 * No mock harness: both reads drive [NotificationWebService] directly. Each call wraps its
 * result in [Result], parsing a `400` into a [BadRequestException] so the presentation layer
 * can read the `field_errors`. The two reads are joined upstream (in the use case) into the
 * render-ready feed.
 *
 * @param webService Retrofit contract for the notification endpoints.
 * @param gson deserializer for the structured `400` validation payload.
 */
class NotificationStoreImpl(
    private val webService: NotificationWebService,
    private val gson: Gson,
) : NotificationStore {

    override suspend fun getNotifications(): Result<List<Notification>> =
        runCatching { unwrapList(webService.getNotifications()) }

    override suspend fun getNotificationDetails(): Result<List<NotificationDetail>> =
        runCatching { unwrapList(webService.getNotificationDetails()) }

    /** Unwraps a list [response], tolerating an empty body as an empty list. */
    private fun <T> unwrapList(response: Response<List<T>>): List<T> {
        if (response.isSuccessful) {
            return response.body().orEmpty()
        }
        val rawError: String? = runCatching { response.errorBody()?.string() }.getOrNull()
        if (response.code() == HTTP_BAD_REQUEST) {
            val parsed: BadRequestResponse = rawError
                ?.let { runCatching { gson.fromJson(it, BadRequestResponse::class.java) }.getOrNull() }
                ?: BadRequestResponse(message = rawError)
            throw BadRequestException(parsed)
        }
        error("HTTP ${response.code()} ${response.message().ifBlank { rawError ?: "request failed" }}")
    }

    private companion object {
        const val HTTP_BAD_REQUEST: Int = 400
    }
}
