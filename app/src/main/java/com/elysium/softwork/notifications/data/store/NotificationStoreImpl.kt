package com.elysium.softwork.notifications.data.store

import android.content.Context
import com.elysium.softwork.R
import com.elysium.softwork.notifications.domain.model.Notification
import com.elysium.softwork.shared.utils.values.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mocked [NotificationStore]. Returns a static four-entry list covering all four
 * [NotificationType] discriminators so the color-coded card layout is fully exercised in
 * the UI walkthrough.
 *
 * Replace with a Retrofit-backed implementation when `/notifications` is live; the
 * [getNotifications] contract is already a [Flow] so the UI does not need to change.
 *
 * @param context application context used to resolve the seed strings.
 */
class NotificationStoreImpl(private val context: Context) : NotificationStore {

    override fun getNotifications(): Flow<List<Notification>> = flowOf(
        listOf(
            Notification(
                id = "notif-survey-1",
                type = NotificationType.SURVEY,
                title = context.getString(R.string.notif_survey_title),
                description = context.getString(R.string.notif_survey_desc),
                isRead = false,
            ),
            Notification(
                id = "notif-payment-1",
                type = NotificationType.PAYMENT,
                title = context.getString(R.string.notif_payment_title),
                description = context.getString(R.string.notif_payment_desc),
                isRead = false,
            ),
            Notification(
                id = "notif-forum-1",
                type = NotificationType.FORUM,
                title = context.getString(R.string.notif_forum_title),
                description = context.getString(R.string.notif_forum_desc),
                isRead = false,
            ),
            Notification(
                id = "notif-message-1",
                type = NotificationType.MESSAGE,
                title = context.getString(R.string.notif_message_title),
                description = context.getString(R.string.notif_message_desc),
                isRead = false,
            ),
        ),
    )
}
