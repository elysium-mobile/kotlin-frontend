package com.elysium.softwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme

/**
 * Single Activity hosting the entire Compose UI tree. The app applies edge-to-edge insets and
 * mounts everything under [SoftWorkTheme]. Navigation is composed on top in later phases.
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
                    // Phase 1 placeholder — feature navigation graph plugs in here.
                }
            }
        }
    }
}
