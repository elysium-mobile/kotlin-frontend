package com.elysium.softwork.shared.core

import android.content.Context
import com.elysium.softwork.worker.forum.data.local.ForumDatabase
import com.elysium.softwork.worker.forum.data.network.PostWebService
import com.elysium.softwork.worker.forum.data.store.PostStore
import com.elysium.softwork.worker.forum.data.store.PostStoreImpl
import com.elysium.softwork.iam.data.network.AuthWebService
import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.data.store.AuthStoreImpl
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import com.elysium.softwork.shared.data.network.ApiClient
import com.elysium.softwork.worker.forum.data.network.ForumReportWebService
import com.elysium.softwork.worker.forum.data.store.ForumReportStoreImpl
import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.feedback.data.store.SurveyStore
import com.elysium.softwork.feedback.data.store.SurveyStoreImpl
import com.elysium.softwork.notifications.data.store.NotificationStore
import com.elysium.softwork.notifications.data.store.NotificationStoreImpl

/**
 * Manual service locator. The locked stack does not include Hilt, so a single, explicit
 * locator owns the wiring of process-wide singletons: shared prefs, the Retrofit instance,
 * each context's WebService, and each context's Store implementation.
 *
 * Stores are exposed as their interface type ([AuthStore], [PostStore], …) so call sites
 * depend on the contract rather than the impl. New bounded contexts add their stores here
 * as they come online.
 */
class ServiceLocator(context: Context) {

    // region Shared infrastructure
    val sharedPrefsManager: SharedPrefsManager = SharedPrefsManager(context)
    // endregion

    // region IAM
    private val authWebService: AuthWebService =
        ApiClient.retrofit.create(AuthWebService::class.java)

    val authStore: AuthStore = AuthStoreImpl(authWebService, sharedPrefsManager)
    // endregion

    // region Forum
    private val forumDatabase: ForumDatabase = ForumDatabase.create(context)

    private val postWebService: PostWebService =
        ApiClient.retrofit.create(PostWebService::class.java)

    val postStore: PostStore = PostStoreImpl(
        dao = forumDatabase.postDao(),
        webService = postWebService,
    )

    private val forumReportWebService: ForumReportWebService =
        ApiClient.retrofit.create(ForumReportWebService::class.java)

    val forumReportStore: ForumReportStore = ForumReportStoreImpl(
        webService = forumReportWebService
    )
    // endregion

    // region Feedback
    val surveyStore: SurveyStore = SurveyStoreImpl(context.applicationContext)
    // endregion

    // region Notifications
    val notificationStore: NotificationStore =
        NotificationStoreImpl(context.applicationContext)
    // endregion
}
