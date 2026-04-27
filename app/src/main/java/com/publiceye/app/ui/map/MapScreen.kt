package com.publiceye.app.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.publiceye.app.R
import com.publiceye.app.data.model.Issue
import com.publiceye.app.ui.components.BottomNavTab
import com.publiceye.app.ui.components.PublicEyeBottomNav
import com.publiceye.app.ui.components.StatusBadge
import com.publiceye.app.ui.theme.Amber40
import com.publiceye.app.ui.theme.Blue40

private val Bengaluru = LatLng(12.9716, 77.5946)

// ── Map Screen ────────────────────────────────────────────────────────────────

@Composable
fun MapScreen(
    onNavigateToReport  : () -> Unit,
    onNavigateToFeed    : () -> Unit,
    onNavigateToProfile : () -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val filteredIssues by viewModel.filteredIssues.collectAsStateWithLifecycle()
    val selectedIssue  by viewModel.selectedIssue.collectAsStateWithLifecycle()
    val isLoading      by viewModel.isLoading.collectAsStateWithLifecycle()
    val activeCount    by viewModel.activeCount.collectAsStateWithLifecycle()
    val totalCount     by viewModel.totalCount.collectAsStateWithLifecycle()
    val statusFilter   by viewModel.statusFilter.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Bengaluru, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Google Map ────────────────────────────────────────────────────────
        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties          = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style),
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled     = false,
                myLocationButtonEnabled = false,
                compassEnabled          = false,
                mapToolbarEnabled       = false,
            ),
            onMapClick = { viewModel.clearSelection() },
        ) {
            filteredIssues.forEach { issue ->
                issue.location?.let { geoPoint ->
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(geoPoint.latitude, geoPoint.longitude),
                        ),
                        icon  = BitmapDescriptorFactory.defaultMarker(issue.markerHue()),
                        title = issue.title,
                        onClick = {
                            viewModel.selectIssue(issue)
                            true
                        },
                    )
                }
            }
        }

        // ── Top overlay: app bar + filter bar ────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        ) {
            MapTopBar(activeCount = activeCount, totalCount = totalCount)
            MapFilterBar(
                statusFilter   = statusFilter,
                onStatusFilter = viewModel::setStatusFilter,
            )
        }

        // ── Loading spinner ───────────────────────────────────────────────────
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = Blue40,
            )
        }

        // ── Report FAB (hidden when issue card is shown) ──────────────────────
        AnimatedVisibility(
            visible  = selectedIssue == null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 68.dp),
            enter = fadeIn(),
            exit  = fadeOut(),
        ) {
            ExtendedFloatingActionButton(
                onClick        = onNavigateToReport,
                containerColor = Amber40,
                contentColor   = Color.White,
                icon           = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                text           = { Text("Report Issue", fontWeight = FontWeight.SemiBold) },
            )
        }

        // ── Bottom navigation bar ─────────────────────────────────────────────
        PublicEyeBottomNav(
            activeTab     = BottomNavTab.MAP,
            onTabSelected = { tab ->
                when (tab) {
                    BottomNavTab.MAP     -> { /* already here */ }
                    BottomNavTab.FEED    -> onNavigateToFeed()
                    BottomNavTab.PROFILE -> onNavigateToProfile()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // ── Selected issue preview card ───────────────────────────────────────
        AnimatedVisibility(
            visible  = selectedIssue != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 60.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            selectedIssue?.let { issue ->
                IssuePreviewCard(
                    issue     = issue,
                    onDismiss = viewModel::clearSelection,
                    onReport  = onNavigateToReport,
                )
            }
        }
    }
}

// ── Marker colour helper ──────────────────────────────────────────────────────

private fun Issue.markerHue(): Float = when (status) {
    Issue.STATUS_RESOLVED    -> BitmapDescriptorFactory.HUE_GREEN
    Issue.STATUS_IN_PROGRESS -> BitmapDescriptorFactory.HUE_AZURE
    else                     -> 30f  // amber/orange for open issues
}

// ── App top bar ───────────────────────────────────────────────────────────────

@Composable
private fun MapTopBar(activeCount: Int, totalCount: Int) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation  = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text       = "PublicEye",
                style      = MaterialTheme.typography.titleMedium,
                color      = Blue40,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text       = "$activeCount",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = Amber40,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text  = "active ·",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text       = "$totalCount",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text  = "reports",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Filter bar ────────────────────────────────────────────────────────────────

@Composable
private fun MapFilterBar(
    statusFilter: String?,
    onStatusFilter: (String?) -> Unit,
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box {
                FilterChip(
                    selected = statusFilter != null,
                    onClick  = { showStatusMenu = true },
                    label    = {
                        Text(
                            text     = statusFilter?.replaceFirstChar { it.uppercase() }
                                ?.replace("_", " ") ?: "All Status",
                            fontSize = 11.sp,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Blue40.copy(alpha = 0.12f),
                        selectedLabelColor     = Blue40,
                    ),
                )
                DropdownMenu(
                    expanded         = showStatusMenu,
                    onDismissRequest = { showStatusMenu = false },
                ) {
                    listOf(
                        null,
                        Issue.STATUS_OPEN,
                        Issue.STATUS_IN_PROGRESS,
                        Issue.STATUS_RESOLVED,
                    ).forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    status?.replaceFirstChar { it.uppercase() }
                                        ?.replace("_", " ") ?: "All"
                                )
                            },
                            onClick = {
                                onStatusFilter(status)
                                showStatusMenu = false
                            },
                        )
                    }
                }
            }

            FilterChip(
                selected = true,
                onClick  = {},
                label    = { Text("Roads", fontSize = 11.sp) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Amber40.copy(alpha = 0.10f),
                    selectedLabelColor     = Amber40,
                ),
            )
        }
    }
}

// ── Issue preview card (shown on marker tap) ──────────────────────────────────

@Composable
private fun IssuePreviewCard(
    issue: Issue,
    onDismiss: () -> Unit,
    onReport: () -> Unit,
) {
    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Status badge + dismiss button
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                StatusBadge(issue.status)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier           = Modifier.size(18.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text       = issue.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
            )

            if (issue.address.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = issue.address,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier           = Modifier.size(15.dp),
                    tint               = Amber40,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = "${issue.upvotes} upvotes",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = Amber40,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick        = onReport,
                    colors         = ButtonDefaults.buttonColors(containerColor = Amber40),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape          = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier           = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Report Issue", fontSize = 12.sp)
                }
            }
        }
    }
}
