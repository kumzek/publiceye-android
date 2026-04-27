package com.publiceye.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.publiceye.app.data.model.Issue
import com.publiceye.app.ui.components.BottomNavTab
import com.publiceye.app.ui.components.IssueCard
import com.publiceye.app.ui.components.PublicEyeBottomNav
import com.publiceye.app.ui.theme.Amber40
import com.publiceye.app.ui.theme.Blue40

@Composable
fun FeedScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val filteredIssues by viewModel.filteredIssues.collectAsStateWithLifecycle()
    val isLoading      by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusFilter   by viewModel.statusFilter.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar ───────────────────────────────────────────────────────────
        FeedTopBar()

        // ── Filter bar ────────────────────────────────────────────────────────
        FeedFilterBar(
            statusFilter   = statusFilter,
            onStatusFilter = viewModel::setStatusFilter,
        )

        // ── Content ───────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = Blue40,
                    )
                }
                filteredIssues.isEmpty() -> {
                    EmptyFeedMessage(
                        modifier     = Modifier.align(Alignment.Center),
                        hasFilter    = statusFilter != null,
                    )
                }
                else -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = filteredIssues,
                            key   = { it.id },
                        ) { issue ->
                            IssueCard(
                                issue   = issue,
                                onClick = { onNavigateToDetail(issue.id) },
                            )
                        }
                    }
                }
            }
        }

        // ── Bottom nav ────────────────────────────────────────────────────────
        PublicEyeBottomNav(
            activeTab     = BottomNavTab.FEED,
            onTabSelected = { tab ->
                when (tab) {
                    BottomNavTab.MAP     -> onNavigateToMap()
                    BottomNavTab.FEED    -> { /* already here */ }
                    BottomNavTab.PROFILE -> onNavigateToProfile()
                }
            },
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun FeedTopBar() {
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
            Text(
                text  = "All Reports",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Filter bar ────────────────────────────────────────────────────────────────

@Composable
private fun FeedFilterBar(
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
                            text     = statusFilter
                                ?.replaceFirstChar { it.uppercase() }
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

            // Category — hardcoded Roads for MVP
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

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyFeedMessage(
    modifier: Modifier = Modifier,
    hasFilter: Boolean,
) {
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text       = if (hasFilter) "No reports match this filter" else "No reports yet",
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!hasFilter) {
            Text(
                text  = "Be the first to report an issue in your area.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
