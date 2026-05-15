package com.elysium.softwork.iam.presentation.views.success

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elysium.softwork.R
import com.elysium.softwork.shared.presentation.components.ButtonVariant
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.Success

/**
 * Generic success surface used after register or login. The caller supplies the title and
 * the action button label/handler; the screen owns the visual treatment.
 *
 * @param title localized title (e.g. "User Registered", "Session started").
 * @param actionLabel label for the primary button (e.g. "Sign in", "Home Menu").
 * @param onAction click handler for the primary button.
 * @param checkColor color of the giant check icon. Defaults to brand [Success].
 */
@Composable
fun AuthSuccessScreen(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    checkColor: Color = Success,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(0.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(color = checkColor, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(R.string.cd_success_check),
                    tint = Color.White,
                    modifier = Modifier.size(72.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryNavy,
                textAlign = TextAlign.Center,
            )
        }

        SoftWorkButton(
            text = actionLabel,
            onClick = onAction,
            modifier = Modifier.padding(bottom = 32.dp),
            variant = ButtonVariant.EMPLOYEE,
        )
    }
}
