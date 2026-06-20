package com.elysium.softwork.feedback.presentation.views.surveys

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.feedback.presentation.viewmodel.PendingSurveysViewModel
import com.elysium.softwork.feedback.domain.model.Survey
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.Danger
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.utils.discriminators.ButtonVariant

/**
 * "Surveys" screen — lists the HR surveys the worker still has to answer.
 *
 * The screen subscribes to [PendingSurveysViewModel.surveys] and renders one [SoftWorkCard]
 * per survey. The card shows title + description separated by a 0.5.dp divider, with a
 * full-width "Start" button at the bottom.
 *
 * @param onBack invoked when the user taps the back arrow in the header.
 * @param onStartSurvey invoked when the user taps "Start" on a survey card; the backend
 *   `survey_id` is forwarded so the host can route to the answer flow.
 */
@Composable
fun PendingSurveysScreen(
    onBack: () -> Unit,
    onStartSurvey: (Long?) -> Unit,
    viewModel: PendingSurveysViewModel = viewModel(factory = PendingSurveysViewModel.Factory),
) {
    val surveys: List<Survey> by viewModel.surveys.collectAsState()
    val errorMessage: String? by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite),
    ) {
        SurveysHeader(onBack = onBack)

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Danger,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = surveys, key = { it.survey_id ?: 0L }) { survey ->
                SurveyCard(
                    survey = survey,
                    onStart = { onStartSurvey(survey.survey_id) },
                )
            }
        }
    }
}

@Composable
private fun SurveysHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.cd_back),
            tint = PrimaryNavy,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onBack),
        )
        Text(
            text = stringResource(R.string.surveys_title),
            color = PrimaryNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
        )
    }
}

@Composable
private fun SurveyCard(
    survey: Survey,
    onStart: () -> Unit,
) {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = survey.title.orEmpty(),
                color = PrimaryNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = survey.description.orEmpty(),
                color = AccentDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = AccentWhite)
            Spacer(modifier = Modifier.height(16.dp))
            SoftWorkButton(
                text = stringResource(R.string.survey_start_button),
                onClick = onStart,
                variant = ButtonVariant.EMPLOYEE,
            )
        }
    }
}
