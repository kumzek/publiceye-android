package com.publiceye.app.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.publiceye.app.R

/**
 * Step 3 of 3. Read-only summary of what's about to be submitted + the primary CTA.
 * Tapping Submit fires [ReportViewModel.submit]. Loading spinner overlays the CTA while
 * the ViewModel's `submitting` flag is true.
 */
@Composable
fun ReportStep3ReviewScreen(
    state     : ReportUiState,
    viewModel : ReportViewModel,
) {
    val ready = state.locationStatus as? LocationStatus.Ready

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {

        Text(
            text  = stringResource(R.string.report_review_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(20.dp))

        // Photo preview
        if (state.photoUri != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                AsyncImage(
                    model              = state.photoUri,
                    contentDescription = null,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Title
        ReviewFieldRow(
            label = stringResource(R.string.report_title_label),
            value = state.title.ifBlank { "—" },
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        // Description
        ReviewFieldRow(
            label = stringResource(R.string.report_description_label),
            value = state.description.ifBlank { "—" },
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        // Category
        ReviewFieldRow(
            label = stringResource(R.string.report_category_label),
            value = stringResource(R.string.report_category_roads),
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        // Location
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.report_location_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = ready?.address ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                )
                ready?.let {
                    Text(
                        text  = "%.5f, %.5f".format(it.lat, it.lng),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = viewModel::submit,
            enabled  = !state.submitting && ready != null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.submitting) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.report_submitting))
            } else {
                Text(stringResource(R.string.report_submit))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReviewFieldRow(label: String, value: String) {
    Column {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
