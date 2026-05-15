package com.elysium.softwork.shared.presentation.views.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.softwork.R
import com.elysium.softwork.shared.domain.i18n.AppLocale
import com.elysium.softwork.shared.domain.i18n.LocaleHelper
import com.elysium.softwork.shared.presentation.components.InitialsAvatar
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.Danger
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * Profile / settings surface for the authenticated employee.
 *
 * Layout: header → hero (avatar + name + role) → work-info card → privacy card → language
 * card (live language toggle wired to [LocaleHelper]) → payment card → edit-profile button →
 * logout text button.
 *
 * @param onEditProfile handler for both header "Edit" and the bottom "Edit Profile" button.
 * @param onLogout handler that should clear the session and route back to the IAM graph.
 * @param onOpenAnonymousForumSettings handler for the privacy row.
 * @param onOpenPaymentMethods handler for the payment row.
 */
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onOpenAnonymousForumSettings: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val activeLanguage: String = configuration.locales[0].language
    val activeLocale: AppLocale = if (activeLanguage == "es") AppLocale.ES else AppLocale.EN

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Header(onEdit = onEditProfile)

        Spacer(Modifier.height(20.dp))

        Hero()

        Spacer(Modifier.height(28.dp))

        SectionLabel(text = stringResource(R.string.profile_section_work_info))
        Spacer(Modifier.height(8.dp))
        WorkInfoCard()

        Spacer(Modifier.height(20.dp))

        SectionLabel(text = stringResource(R.string.profile_section_privacy))
        Spacer(Modifier.height(8.dp))
        SimpleRowCard(
            title = stringResource(R.string.profile_anonymous_forum),
            onClick = onOpenAnonymousForumSettings,
        )

        Spacer(Modifier.height(20.dp))

        SectionLabel(text = stringResource(R.string.profile_section_language))
        Spacer(Modifier.height(8.dp))
        LanguageCard(active = activeLocale)

        Spacer(Modifier.height(20.dp))

        SectionLabel(text = stringResource(R.string.profile_section_payment))
        Spacer(Modifier.height(8.dp))
        SimpleRowCard(
            title = stringResource(R.string.profile_payment_methods),
            onClick = onOpenPaymentMethods,
        )

        Spacer(Modifier.height(28.dp))

        OutlineActionButton(
            text = stringResource(R.string.profile_edit_button),
            onClick = onEditProfile,
        )

        Spacer(Modifier.height(12.dp))

        LogoutButton(onLogout = onLogout)

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Header(onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = PrimaryNavy,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .clickable(onClick = onEdit)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_edit_action),
                color = PrimarySky,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.cd_edit),
                tint = PrimarySky,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun Hero() {
    val name: String = stringResource(R.string.profile_sample_name)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InitialsAvatar(fullName = name, size = 56.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryNavy,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.profile_sample_role),
            style = MaterialTheme.typography.bodySmall,
            color = AccentDark,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = AccentDark,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun WorkInfoCard() {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            InfoRow(
                label = stringResource(R.string.profile_company_label),
                value = stringResource(R.string.profile_company_value),
            )
            InfoRow(
                label = stringResource(R.string.profile_area_label),
                value = stringResource(R.string.profile_area_value),
            )
            InfoRow(
                label = stringResource(R.string.profile_position_label),
                value = stringResource(R.string.profile_position_value),
            )
            InfoRow(
                label = stringResource(R.string.profile_email_label_row),
                value = stringResource(R.string.profile_email_value),
                isLast = true,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AccentDark,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = PrimaryNavy,
        )
    }
    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AccentWhite),
        )
    }
}

@Composable
private fun SimpleRowCard(title: String, onClick: () -> Unit) {
    SoftWorkCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = PrimaryNavy,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = stringResource(R.string.cd_chevron),
                tint = AccentDark,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Language card with two-pill segmented selector. Tapping a pill calls [LocaleHelper.apply],
 * which delegates to `AppCompatDelegate.setApplicationLocales` — the platform / AppCompat
 * back-port handles persistence and activity recreation transparently. The composable
 * recomposes automatically once the new configuration is applied.
 */
@Composable
private fun LanguageCard(active: AppLocale) {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_section_language),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = PrimaryNavy,
                modifier = Modifier.weight(1f),
            )
            LanguagePill(
                label = stringResource(R.string.profile_language_es),
                selected = active == AppLocale.ES,
                onClick = { if (active != AppLocale.ES) LocaleHelper.apply(AppLocale.ES) },
            )
            Spacer(Modifier.size(8.dp))
            LanguagePill(
                label = stringResource(R.string.profile_language_en),
                selected = active == AppLocale.EN,
                onClick = { if (active != AppLocale.EN) LocaleHelper.apply(AppLocale.EN) },
            )
        }
    }
}

@Composable
private fun LanguagePill(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    val background: Color = if (selected) PrimarySky else Color.White
    val contentColor: Color = if (selected) Color.White else AccentDark
    val border = if (selected) null else BorderStroke(1.dp, AccentMint)

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = shape,
        color = background,
        border = border,
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun OutlineActionButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(width = 1.dp, color = PrimarySky),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = PrimarySky,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLogout)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.profile_logout),
            color = Danger,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
