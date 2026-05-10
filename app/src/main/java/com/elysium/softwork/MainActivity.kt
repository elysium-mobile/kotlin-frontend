package com.elysium.softwork

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elysium.softwork.iam.presentation.navigation.AuthNavHost
import com.elysium.softwork.shared.presentation.navigation.MainNavHost
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme

/**
 * Single Activity hosting the entire Compose UI tree.
 *
 * Inherits from [AppCompatActivity] so that [androidx.appcompat.app.AppCompatDelegate.setApplicationLocales]
 * automatically recreates the activity with the new configuration on API 29-32 (the AppCompat
 * back-port path). On API 33+ the platform `LocaleManager` handles recreation transparently;
 * the AppCompat dependency is harmless on those versions.
 *
 * The Activity owns a single boolean — "is the user authenticated" — and swaps between the
 * IAM nav graph and the authenticated [MainNavHost] based on it. No parent nav graph is
 * needed because the two flows do not share back-stack entries.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoftWorkTheme {
                val app = remember { application as SoftWorkApplication }
                var isAuthenticated: Boolean by rememberSaveable {
                    mutableStateOf(app.serviceLocator.authStore.activeToken() != null)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (isAuthenticated) {
                        MainNavHost(
                            userName = stringResource(R.string.home_user_name_placeholder),
                            onLogout = {
                                app.serviceLocator.authStore.clearSession()
                                isAuthenticated = false
                            },
                        )
                    } else {
                        AuthNavHost(
                            onAuthComplete = { isAuthenticated = true },
                        )
                    }
                }
            }
        }
    }
}
