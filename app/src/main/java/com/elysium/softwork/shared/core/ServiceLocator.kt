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
import com.elysium.softwork.feedback.data.store.FeedbackStore
import com.elysium.softwork.feedback.data.store.FeedbackStoreImpl
import com.elysium.softwork.feedback.data.store.SurveyStore
import com.elysium.softwork.feedback.data.store.SurveyStoreImpl
import com.elysium.softwork.notifications.data.store.NotificationStore
import com.elysium.softwork.notifications.data.store.NotificationStoreImpl
import com.elysium.softwork.payment.membership.data.store.MembershipStore
import com.elysium.softwork.payment.membership.data.store.MembershipStoreImpl

/**
 * Manual service locator that owns process-wide singletons (shared preferences, the
 * Retrofit instance, per-context WebServices and Stores).
 *
 * Every singleton is exposed through `by lazy` so its construction cost — Retrofit proxy
 * generation, the Room database open call, SharedPreferences disk read — is deferred to
 * first access rather than paid on `Application.onCreate()`. Cold-start critical paths
 * (authentication, the payment gate) therefore avoid touching the forum database, the
 * forum WebServices, and any unrelated stores until the user actually navigates to them.
 *
 * Stores are exposed as their interface type so call sites depend on the contract rather
 * than the implementation, and the captured [Context] is normalized to the application
 * context up front to guarantee the locator can never retain an Activity reference.
 */
class ServiceLocator(context: Context) {

    private val appContext: Context = context.applicationContext

    val sharedPrefsManager: SharedPrefsManager by lazy { SharedPrefsManager(appContext) }

    private val authWebService: AuthWebService by lazy {
        ApiClient.retrofit.create(AuthWebService::class.java)
    }

    val authStore: AuthStore by lazy { AuthStoreImpl(authWebService, sharedPrefsManager) }

    private val forumDatabase: ForumDatabase by lazy { ForumDatabase.create(appContext) }

    private val postWebService: PostWebService by lazy {
        ApiClient.retrofit.create(PostWebService::class.java)
    }

    val postStore: PostStore by lazy {
        PostStoreImpl(dao = forumDatabase.postDao(), webService = postWebService)
    }

    private val forumReportWebService: ForumReportWebService by lazy {
        ApiClient.retrofit.create(ForumReportWebService::class.java)
    }

    val forumReportStore: ForumReportStore by lazy {
        ForumReportStoreImpl(webService = forumReportWebService)
    }

    val surveyStore: SurveyStore by lazy { SurveyStoreImpl(appContext) }

    val feedbackStore: FeedbackStore by lazy { FeedbackStoreImpl() }

    val notificationStore: NotificationStore by lazy { NotificationStoreImpl(appContext) }

    val membershipStore: MembershipStore by lazy { MembershipStoreImpl(sharedPrefsManager) }
}
