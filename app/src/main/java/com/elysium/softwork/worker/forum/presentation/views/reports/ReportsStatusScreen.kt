package com.elysium.softwork.worker.forum.presentation.views.reports

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.iam.presentation.components.BackTopBar
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.Danger
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.worker.forum.domain.model.Report
import com.elysium.softwork.worker.forum.presentation.viewmodel.ReportsStatusViewModel

/**
 * Reports-status screen. Lists the reports the worker has submitted (`GET /api/v1/reports`).
 *
 * Adapted to the live backend `Report`: a report carries a reason, a description, and a date —
 * there is no server-side status field, so the former status pills are gone. Filing a new
 * report stays on the thread-detail screen; this screen is read-only.
 *
 * @param onBack pop handler for the header back arrow.
 */
@Composable
fun ReportsStatusScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsStatusViewModel = viewModel(factory = ReportsStatusViewModel.Factory),
) {
    val state: ReportsStatusViewModel.UiState by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        BackTopBar(onBack = onBack)
        Header()

        when (val current = state) {
            ReportsStatusViewModel.UiState.Loading -> LoadingBlock()
            is ReportsStatusViewModel.UiState.Error -> ErrorBlock(message = current.message)
            is ReportsStatusViewModel.UiState.Ready -> {
                if (current.reports.isEmpty()) EmptyState() else ReportList(reports = current.reports)
            }
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.reports_status_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = PrimaryNavy,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.reports_status_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AccentDark,
        )
    }
}

@Composable
private fun LoadingBlock() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimarySky)
    }
}

@Composable
private fun ErrorBlock(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = Danger,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_flag),
            contentDescription = null,
            tint = AccentMint,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.reports_status_empty_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryNavy,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.reports_status_empty_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = AccentDark,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReportList(reports: List<Report>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
    ) {
        items(items = reports, key = { it.report_id ?: it.hashCode().toLong() }) { report ->
            ReportCard(report = report)
        }
    }
}

@Composable
private fun ReportCard(report: Report) {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = report.reason.orEmpty(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryNavy,
            )

            if (!report.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentDark,
                    maxLines = 3,
                )
            }

            report.report_date?.let { date ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.reports_status_filed_on, date),
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentDark.copy(alpha = 0.6f),
                )
            }
        }
    }
}
