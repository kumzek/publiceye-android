package com.publiceye.app.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.publiceye.app.R

/**
 * Success screen shown after a report is submitted.
 *
 * Per the wireframes: show a secondary "Turn on notifications" card if we don't already have
 * POST_NOTIFICATIONS permission (Android 13+). This is the post-submit ask — it catches users
 * who skipped the first-launch rationale and is a natural moment to ask ("you just made a report
 * — want to hear back?"). Legacy Androids (< 13) don't need the permission and we hide the card.
 */
@Composable
fun ReportSuccessScreen(
    onDone: () -> Unit,
) {
    val context = LocalContext.current

    // Whether we should show the notifications card — re-checked if user taps inside
    var shouldShowNotifCard by rememberSaveable { mutableStateOf(computeShouldShow(context)) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Regardless of grant/deny, hide the card — user has responded.
        shouldShowNotifCard = false
    }

    fun askForNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            shouldShowNotifCard = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // Success tick
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector         = Icons.Default.Check,
                contentDescription  = null,
                modifier            = Modifier.size(52.dp),
                tint                = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text      = stringResource(R.string.report_success_title),
            style     = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = stringResource(R.string.report_success_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (shouldShowNotifCard) {
            Spacer(Modifier.height(32.dp))
            NotificationPermissionCard(onEnable = { askForNotifPermission() })
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick  = onDone,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(R.string.report_success_done))
        }
    }
}

@Composable
private fun NotificationPermissionCard(onEnable: () -> Unit) {
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector         = Icons.Default.Notifications,
                    contentDescription  = null,
                    tint                = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text  = stringResource(R.string.report_enable_notifs_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text  = stringResource(R.string.report_enable_notifs_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick  = onEnable,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.report_enable_notifs_cta))
            }
        }
    }
}

private fun computeShouldShow(context: android.content.Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) != PackageManager.PERMISSION_GRANTED
}
