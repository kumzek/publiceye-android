package com.publiceye.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.publiceye.app.R
import com.publiceye.app.ui.auth.AuthViewModel

/**
 * First-launch notification permission rationale screen.
 *
 * Flow:
 *  1. Show this screen with two benefit points and a Skip option.
 *  2. If "Enable Notifications" → trigger the system permission dialog.
 *  3. Whether the user grants or denies, mark rationale as seen and proceed.
 *  4. If "Skip" → mark seen and proceed immediately. Never ask again automatically.
 */
@Composable
fun NotificationRationaleScreen(
    viewModel  : AuthViewModel = hiltViewModel(),
    onComplete : () -> Unit,
) {
    // Permission launcher — called AFTER user taps "Enable Notifications"
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Regardless of grant/deny, we move on. User can revisit in Settings.
        viewModel.markNotificationRationaleSeen()
        onComplete()
    }

    fun requestPermissionOrProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Below Android 13 — no runtime permission needed
            viewModel.markNotificationRationaleSeen()
            onComplete()
        }
    }

    fun skip() {
        viewModel.markNotificationRationaleSeen()
        onComplete()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector         = Icons.Default.Notifications,
                    contentDescription  = null,
                    modifier            = Modifier.size(48.dp),
                    tint                = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text      = stringResource(R.string.notif_rationale_title),
                style     = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "We'll only notify you for things that matter to you.",
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(40.dp))

            // Benefit 1
            BenefitRow(
                icon  = Icons.Default.LocationOn,
                text  = stringResource(R.string.notif_rationale_benefit_1),
            )

            Spacer(Modifier.height(20.dp))

            // Benefit 2
            BenefitRow(
                icon  = Icons.Default.ThumbUp,
                text  = stringResource(R.string.notif_rationale_benefit_2),
            )

            Spacer(Modifier.height(56.dp))

            // Enable button
            Button(
                onClick  = { requestPermissionOrProceed() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(stringResource(R.string.notif_enable))
            }

            Spacer(Modifier.height(12.dp))

            // Skip — prominent, not hidden
            TextButton(
                onClick  = { skip() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text  = stringResource(R.string.notif_skip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(
    icon : androidx.compose.ui.graphics.vector.ImageVector,
    text : String,
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.secondary,
                modifier           = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
