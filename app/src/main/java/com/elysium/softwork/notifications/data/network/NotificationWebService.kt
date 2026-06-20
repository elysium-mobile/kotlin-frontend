package com.elysium.softwork.notifications.data.network

import com.elysium.softwork.notifications.domain.model.Notification
import com.elysium.softwork.notifications.domain.model.NotificationDetail
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit contract for the live Notification endpoints of the FlowWork Spring Boot API.
 *
 * The same annotation-free domain beans ([Notification], [NotificationDetail]) carry both
 * request bodies and response payloads (the bean shortcut) — no DTOs. All paths are
 * **relative**; the host + `/` base lives in `BuildConfig.BACKEND_BASE_URL` (resolved by
 * `ApiClient`), and `AuthInterceptor` attaches the bearer token automatically.
 */
interface NotificationWebService {

    // region Notifications
    /** Lists every notification record (category + ownership metadata). */
    @GET("api/v1/notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    /** Fetches a single notification by its `notification_id`. */
    @GET("api/v1/notifications/{id}")
    suspend fun getNotification(@Path("id") id: Long): Response<Notification>

    /** Creates a new notification record. */
    @POST("api/v1/notifications")
    suspend fun createNotification(@Body notification: Notification): Response<Notification>
    // endregion

    // region Notification Details
    /** Lists every notification detail (title + content), joined client-side by parent id. */
    @GET("api/v1/notification-details")
    suspend fun getNotificationDetails(): Response<List<NotificationDetail>>

    /** Fetches a single detail by its `notification_detail_id`. */
    @GET("api/v1/notification-details/{id}")
    suspend fun getNotificationDetail(@Path("id") id: Long): Response<NotificationDetail>

    /** Creates a new notification detail. */
    @POST("api/v1/notification-details")
    suspend fun createNotificationDetail(@Body detail: NotificationDetail): Response<NotificationDetail>
    // endregion
}
