package com.elysium.softwork.shared.presentation.views.identity

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.shared.application.AnonymityViewModel
import com.elysium.softwork.shared.domain.identity.AnonymityPreferences
import com.elysium.softwork.shared.utils.discriminators.ButtonVariant
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal
import com.elysium.softwork.shared.presentation.theme.Success

/**
 * Identity-protection settings screen. The user toggles a global master switch plus three
 * granular per-context flags (forum, surveys, reports). Edits live in the
 * [AnonymityViewModel] in-memory buffer and flush to [com.elysium.softwork.shared.data.local.SharedPrefsManager]
 * only when the user taps "Save preferences", so backing out of the screen discards
 * unintended changes.
 *
 * @param onBack pop handler for the header arrow.
 * @param onSaved invoked after [AnonymityViewModel.save] completes — the host typically
 *   pops back to the calling screen.
 */
@Composable
fun ProtectedIdentityScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnonymityViewModel = viewModel(factory = AnonymityViewModel.Factory),
) {
    val state: AnonymityPreferences by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Header(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Hero()

            Spacer(Modifier.height(24.dp))
            GlobalToggleCard(
                checked = state.global,
                onCheckedChange = viewModel::setGlobal,
            )

            Spacer(Modifier.height(16.dp))
            GranularTogglesCard(
                state = state,
                onForumChange = viewModel::setForum,
                onSurveysChange = viewModel::setSurveys,
                onReportsChange = viewModel::setReports,
            )

            Spacer(Modifier.height(20.dp))
            HrInfoBanner()

            Spacer(Modifier.height(24.dp))
            SoftWorkButton(
                text = stringResource(R.string.protected_identity_save),
                onClick = {
                    viewModel.save()
                    onSaved()
                },
                variant = ButtonVariant.EMPLOYEE,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.cd_back),
            tint = AccentDark,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onBack),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.protected_identity_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = PrimaryNavy,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun Hero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(color = AccentDark.copy(alpha = 0.65f), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "?",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.protected_identity_anonymous_user),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryNavy,
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.protected_identity_description),
            style = MaterialTheme.typography.bodySmall,
            color = AccentDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun GlobalToggleCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.protected_identity_global_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryNavy,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.protected_identity_global_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentDark,
                )
            }
            BrandSwitch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun GranularTogglesCard(
    state: AnonymityPreferences,
    onForumChange: (Boolean) -> Unit,
    onSurveysChange: (Boolean) -> Unit,
    onReportsChange: (Boolean) -> Unit,
) {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            ToggleRow(
                label = stringResource(R.string.protected_identity_hide_forum),
                checked = state.forum,
                onCheckedChange = onForumChange,
            )
            ThinDivider()
            ToggleRow(
                label = stringResource(R.string.protected_identity_hide_surveys),
                checked = state.surveys,
                onCheckedChange = onSurveysChange,
            )
            ThinDivider()
            ToggleRow(
                label = stringResource(R.string.protected_identity_hide_reports),
                checked = state.reports,
                onCheckedChange = onReportsChange,
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = PrimaryNavy,
            modifier = Modifier.weight(1f),
        )
        BrandSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(AccentWhite),
    )
}

/** [Switch] preset to brand spec — track turns [PrimaryTeal] when on. */
@Composable
private fun BrandSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = PrimaryTeal,
            checkedBorderColor = PrimaryTeal,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = AccentWhite,
            uncheckedBorderColor = AccentMint,
        ),
    )
}

@Composable
private fun HrInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = AccentMint.copy(alpha = 0.20f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = stringResource(R.string.cd_lock),
            tint = Success,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.protected_identity_hr_notice),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
            color = PrimaryNavy,
        )
    }
}
