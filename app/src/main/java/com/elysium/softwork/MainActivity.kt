package com.elysium.softwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.elysium.softwork.iam.presentation.navigation.AuthNavHost
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme

/**
 * Single Activity hosting the entire Compose UI tree. The app applies edge-to-edge insets,
 * mounts everything under [SoftWorkTheme], and starts at the IAM nav graph. When the user
 * completes authentication, [AuthNavHost] reports back via `onAuthComplete` — Phase 3 will
 * forward to the main app shell at that point.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoftWorkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AuthNavHost(
                        onAuthComplete = {
                            // Phase 3: navigate to the main app graph (forum/feedback). For
                            // Phase 2 we leave the success screen as the terminal surface.
                        },
                    )
                }
            }
        }
    }
}
