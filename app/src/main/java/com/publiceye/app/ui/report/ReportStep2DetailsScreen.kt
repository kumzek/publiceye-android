package com.publiceye.app.ui.report

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.publiceye.app.R

/**
 * Step 2 of 3. User enters a title + description and confirms the auto-detected location.
 *
 * On first entry we request ACCESS_FINE_LOCATION (if not granted) and immediately fetch the
 * current location. User can tap Refresh to try again (e.g. after stepping outside).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportStep2DetailsScreen(
    state     : ReportUiState,
    viewModel : ReportViewModel,
) {
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.refreshLocation()
    }

    // On first entry: request permission (it's a no-op if already granted). The system
    // callback fires synchronously when already granted, so `refreshLocation()` is then
    // invoked via the launcher result.
    LaunchedEffect(Unit) {
        if (state.locationStatus is LocationStatus.Idle) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {

        Text(
            text  = stringResource(R.string.report_details_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(20.dp))

        // ── Location card ────────────────────────────────────────────────────
        Surface(
            shape  = RoundedCornerShape(12.dp),
            tonalElevation = 0.dp,
            color  = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier          = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text  = stringResource(R.string.report_location_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    when (val s = state.locationStatus) {
                        LocationStatus.Idle -> {
                            Text(stringResource(R.string.report_location_fetching),
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        LocationStatus.Fetching -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier   = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.report_location_fetching),
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        is LocationStatus.Ready -> {
                            Text(s.address, style = MaterialTheme.typography.bodyMedium)
                        }
                        is LocationStatus.Error -> {
                            Text(
                                text  = s.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                TextButton(onClick = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.report_location_refresh))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Title ────────────────────────────────────────────────────────────
        OutlinedTextField(
            value          = state.title,
            onValueChange  = viewModel::onTitleChange,
            label          = { Text(stringResource(R.string.report_title_label)) },
            placeholder    = { Text(stringResource(R.string.report_title_hint)) },
            singleLine     = true,
            modifier       = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // ── Description ──────────────────────────────────────────────────────
        OutlinedTextField(
            value          = state.description,
            onValueChange  = viewModel::onDescriptionChange,
            label          = { Text(stringResource(R.string.report_description_label)) },
            placeholder    = { Text(stringResource(R.string.report_description_hint)) },
            minLines       = 3,
            maxLines       = 6,
            modifier       = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // ── Category (display-only for MVP) ──────────────────────────────────
        Surface(
            shape  = RoundedCornerShape(8.dp),
            color  = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text  = stringResource(R.string.report_category_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text  = stringResource(R.string.report_category_roads),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label   = { Text("MVP") },
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = viewModel::next,
            enabled  = state.title.isNotBlank() && state.locationStatus is LocationStatus.Ready,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(R.string.report_next))
        }

        Spacer(Modifier.height(24.dp))
    }
}
