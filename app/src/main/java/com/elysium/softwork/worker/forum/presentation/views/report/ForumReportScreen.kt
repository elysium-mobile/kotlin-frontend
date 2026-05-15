package com.elysium.softwork.worker.forum.presentation.views.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkTextField
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.Danger
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal
import com.elysium.softwork.worker.forum.application.viewmodel.ForumReportViewModel
import com.elysium.softwork.worker.forum.presentation.components.Chip

/**
 * Screen for reporting a forum post.
 * Displays a form with irregularity type, area, description, and date.
 *
 * @param postId ID of the post being reported.
 * @param onBack callback to navigate back.
 */
@Composable
fun ForumReportScreen(
    postId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumReportViewModel = viewModel(factory = ForumReportViewModel.provideFactory(postId)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackTopBar(onBack = onBack)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            ReportBanner()

            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.report_type_label))
            Spacer(Modifier.height(8.dp))
            ReportTypeChips(
                selectedType = state.type,
                onTypeSelected = viewModel::onTypeSelected,
                types = viewModel.reportTypes
            )

            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.report_area_label))
            Spacer(Modifier.height(8.dp))
            AreaDropdown(
                selectedArea = state.area,
                onAreaSelected = viewModel::onAreaSelected,
                areas = viewModel.areas
            )

            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.report_description_label))
            Spacer(Modifier.height(8.dp))
            SoftWorkTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = stringResource(R.string.report_description_placeholder),
                modifier = Modifier.height(120.dp),
                singleLine = false,
                minLines = 5
            )

            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.report_date_label))
            Spacer(Modifier.height(8.dp))
            SoftWorkTextField(
                value = state.date,
                onValueChange = viewModel::onDateChange,
                placeholder = stringResource(R.string.report_date_placeholder),
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_home), // Placeholder icon
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
            AnonymityCard(isAnonymous = state.isAnonymous)

            Spacer(Modifier.height(24.dp))
            SoftWorkButton(
                text = stringResource(R.string.report_submit_button),
                onClick = viewModel::submitReport,
                enabled = state.isValid && !state.isSubmitting
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.report_tracking_code_notice),
                style = MaterialTheme.typography.bodySmall,
                color = AccentDark.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.error!!,
                    color = Danger,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Danger.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Danger.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_flag),
            contentDescription = null,
            tint = Danger,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(R.string.report_banner_text),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            ),
            color = Danger
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = PrimaryNavy
    )
}

@Composable
private fun ReportTypeChips(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    types: List<String>
) {
    val typeLabels = mapOf(
        "Acoso" to stringResource(R.string.report_type_harassment),
        "Discriminación" to stringResource(R.string.report_type_discrimination),
        "Seguridad" to stringResource(R.string.report_type_security),
        "Ética" to stringResource(R.string.report_type_ethics),
        "Otro" to stringResource(R.string.report_type_other)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.take(4).forEach { type ->
                Chip(
                    label = typeLabels[type] ?: type,
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.drop(4).forEach { type ->
                Chip(
                    label = typeLabels[type] ?: type,
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) }
                )
            }
        }
    }
}

@Composable
private fun AreaDropdown(
    selectedArea: String,
    onAreaSelected: (String) -> Unit,
    areas: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        SoftWorkTextField(
            value = selectedArea,
            onValueChange = {},
            placeholder = stringResource(R.string.report_area_placeholder),
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { expanded = !expanded }
                )
            },
            modifier = Modifier.clickable { expanded = !expanded }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White)
        ) {
            areas.forEach { area ->
                DropdownMenuItem(
                    text = { Text(area) },
                    onClick = {
                        onAreaSelected(area)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AnonymityCard(isAnonymous: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, AccentWhite, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.report_anonymous_label),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryNavy
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.report_anonymous_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentDark.copy(alpha = 0.5f)
                )
            }
            Switch(
                checked = isAnonymous,
                onCheckedChange = null,
                enabled = false,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryTeal,
                    disabledCheckedTrackColor = PrimaryTeal,
                    disabledCheckedThumbColor = Color.White
                )
            )
        }
    }
}
