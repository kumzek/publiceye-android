package com.publiceye.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.publiceye.app.data.model.Issue
import com.publiceye.app.ui.components.StatusBadge
import com.publiceye.app.ui.theme.Amber40
import com.publiceye.app.ui.theme.Blue40
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    onBack: () -> Unit,
    viewModel: IssueDetailViewModel = hiltViewModel(),
) {
    val issue        by viewModel.issue.collectAsStateWithLifecycle()
    val isLoading    by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUpvoting   by viewModel.isUpvoting.collectAsStateWithLifecycle()
    val upvoteError  by viewModel.upvoteError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show upvote errors in a snackbar
    LaunchedEffect(upvoteError) {
        upvoteError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUpvoteError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Issue Detail",
                        fontWeight = FontWeight.SemiBold,
                        color      = Blue40,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData    = data,
                    containerColor  = MaterialTheme.colorScheme.errorContainer,
                    contentColor    = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                isLoading    -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = Blue40,
                )
                issue == null -> IssueNotFound(onBack = onBack)
                else          -> IssueDetailContent(
                    issue      = issue!!,
                    currentUid = viewModel.currentUid,
                    isUpvoting = isUpvoting,
                    onUpvote   = viewModel::toggleUpvote,
                )
            }
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────

@Composable
private fun IssueDetailContent(
    issue: Issue,
    currentUid: String?,
    isUpvoting: Boolean,
    onUpvote: () -> Unit,
) {
    val hasUpvoted = currentUid != null && issue.upvotedBy.contains(currentUid)
    val dateStr    = remember(issue.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(issue.createdAt.toDate())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {

        // ── Photo ─────────────────────────────────────────────────────────────
        if (issue.photoURL.isNotBlank()) {
            AsyncImage(
                model             = issue.photoURL,
                contentDescription = "Issue photo",
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale       = ContentScale.Crop,
            )
        } else {
            // Placeholder when no photo (shouldn't happen in practice — report flow requires one)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "No photo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── Status + date row ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                StatusBadge(issue.status)
                Text(
                    text  = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Title ─────────────────────────────────────────────────────────
            Text(
                text       = issue.title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            // ── Address ───────────────────────────────────────────────────────
            if (issue.address.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text  = issue.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Description ───────────────────────────────────────────────────
            if (issue.description.isNotBlank()) {
                Text(
                    text       = "Description",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = issue.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(20.dp))
            }

            // ── Upvote button ─────────────────────────────────────────────────
            UpvoteButton(
                upvoteCount = issue.upvotes,
                hasUpvoted  = hasUpvoted,
                isUpvoting  = isUpvoting,
                onUpvote    = onUpvote,
            )
        }
    }
}

// ── Upvote button ─────────────────────────────────────────────────────────────

@Composable
private fun UpvoteButton(
    upvoteCount : Int,
    hasUpvoted  : Boolean,
    isUpvoting  : Boolean,
    onUpvote    : () -> Unit,
) {
    val label = "${upvoteCount} upvote${if (upvoteCount == 1) "" else "s"}"

    if (hasUpvoted) {
        // Filled / active state
        Button(
            onClick        = onUpvote,
            enabled        = !isUpvoting,
            colors         = ButtonDefaults.buttonColors(containerColor = Amber40),
            shape          = RoundedCornerShape(24.dp),
            modifier       = Modifier.fillMaxWidth(),
        ) {
            if (isUpvoting) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(18.dp),
                    color     = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector        = Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(text = label, fontWeight = FontWeight.SemiBold)
            }
        }
    } else {
        // Outlined / inactive state
        OutlinedButton(
            onClick  = onUpvote,
            enabled  = !isUpvoting,
            shape    = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isUpvoting) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(18.dp),
                    color       = Amber40,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector        = Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp),
                    tint               = Amber40,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Upvote · $label",
                    color = Amber40,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Issue not found ───────────────────────────────────────────────────────────

@Composable
private fun IssueNotFound(onBack: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text       = "Report not found",
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "It may have been removed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Go Back") }
    }
}
