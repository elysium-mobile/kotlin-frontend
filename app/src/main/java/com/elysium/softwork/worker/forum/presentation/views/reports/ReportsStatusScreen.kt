package com.elysium.softwork.worker.forum.presentation.views.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.elysium.softwork.shared.presentation.theme.Success
import com.elysium.softwork.shared.presentation.theme.Warning
import com.elysium.softwork.worker.forum.presentation.viewmodel.ReportsStatusViewModel
import com.elysium.softwork.worker.forum.domain.model.ForumReport
import com.elysium.softwork.shared.utils.values.ReportStatus
import java.text.DateFormat
import java.util.Date

/**
 * Reports-status screen. Lists every report the authenticated user has submitted with its
 * current [ReportStatus] so they can track follow-up without leaving the app.
 *
 * Filing a new report is intentionally NOT exposed here — reports are tied to a specific
 * forum post, so the entry point lives on the thread-detail screen. This screen is purely
 * read-only.
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
                if (current.reports.isEmpty()) {
                    EmptyState()
                } else {
                    ReportList(reports = current.reports)
                }
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
private fun ReportList(reports: List<ForumReport>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
    ) {
        items(items = reports, key = { it.id ?: it.hashCode().toString() }) { report ->
            ReportCard(report = report)
        }
    }
}

@Composable
private fun ReportCard(report: ForumReport) {
    val status: ReportStatus = ReportStatus.fromKey(report.status)
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = report.type.orEmpty(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryNavy,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(status = status)
            }

            if (!report.area.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = report.area,
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentDark.copy(alpha = 0.7f),
                )
            }

            if (!report.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentDark,
                    maxLines = 3,
                )
            }

            Spacer(Modifier.height(12.dp))
            FooterRow(report = report)
        }
    }
}

@Composable
private fun FooterRow(report: ForumReport) {
    val locale = LocalConfiguration.current.locales[0]
    val submittedLabel: String? = report.createdAt?.let { millis ->
        val formatted = DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(millis))
        stringResource(R.string.reports_status_filed_on, formatted)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (report.isAnonymous) {
            AnonymousChip()
            Spacer(Modifier.size(8.dp))
        }
        if (submittedLabel != null) {
            Text(
                text = submittedLabel,
                style = MaterialTheme.typography.labelSmall,
                color = AccentDark.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun AnonymousChip() {
    Text(
        text = stringResource(R.string.reports_status_anonymous_chip),
        color = PrimaryNavy,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier
            .background(color = AccentMint, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun StatusPill(status: ReportStatus) {
    val (labelRes, color) = when (status) {
        ReportStatus.PENDING -> R.string.reports_status_pending to Warning
        ReportStatus.UNDER_REVIEW -> R.string.reports_status_under_review to PrimarySky
        ReportStatus.RESOLVED -> R.string.reports_status_resolved to Success
        ReportStatus.DISMISSED -> R.string.reports_status_dismissed to Danger
    }
    Text(
        text = stringResource(labelRes),
        color = color,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        fontSize = 11.sp,
        modifier = Modifier
            .background(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
