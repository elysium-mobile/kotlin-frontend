package com.elysium.softwork.feedback.data.store

import android.content.Context
import com.elysium.softwork.R
import com.elysium.softwork.feedback.domain.model.Survey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mocked [SurveyStore]. Returns a static list of two surveys ("Clima laboral",
 * "Productividad") resolved through Android string resources so the catalogue stays
 * localized while the backend is offline.
 *
 * Replace with a Retrofit-backed implementation when the `/surveys` endpoint is live; the
 * [getPendingSurveys] contract is already a [Flow] so the UI does not need to change.
 *
 * @param context application context used to resolve the seed strings. The locator owns the
 *   single instance so this never leaks an activity context.
 */
class SurveyStoreImpl(private val context: Context) : SurveyStore {

    override fun getPendingSurveys(): Flow<List<Survey>> = flowOf(
        listOf(
            Survey(
                id = "climate",
                title = context.getString(R.string.survey_climate_title),
                description = context.getString(R.string.survey_climate_desc),
            ),
            Survey(
                id = "productivity",
                title = context.getString(R.string.survey_productivity_title),
                description = context.getString(R.string.survey_productivity_desc),
            ),
        ),
    )
}
