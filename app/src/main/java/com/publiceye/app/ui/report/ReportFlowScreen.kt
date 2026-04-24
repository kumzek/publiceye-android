package com.publiceye.app.ui.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.publiceye.app.R

/**
 * Top-level container for the 3-step report flow. Hosts a single [ReportViewModel] so
 * state survives across steps. Listens for [ReportUiState.submittedId] and calls
 * [onSubmitted] when the report has been saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFlowScreen(
    viewModel   : ReportViewModel = hiltViewModel(),
    onExit      : () -> Unit,
    onSubmitted : (issueId: String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Navigate away once the ViewModel reports a successful submit. Wrapped in LaunchedEffect
    // so it fires once per ID transition, not on every recomposition.
    LaunchedEffect(state.submittedId) {
        state.submittedId?.let { onSubmitted(it) }
    }

    // Discard-confirmation dialog state
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    fun requestExit() {
        val hasWork = state.photoUri != null || state.title.isNotBlank() || state.description.isNotBlank()
        if (hasWork) showDiscardDialog = true else onExit()
    }
    BackHandler(enabled = true) {
        when (state.step) {
            ReportStep.Photo   -> requestExit()
            ReportStep.Details -> viewModel.back()
            ReportStep.Review  -> viewModel.back()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.report_step_of,
                            state.step.ordinal + 1,
                            ReportStep.entries.size,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (state.step) {
                            ReportStep.Photo   -> requestExit()
                            ReportStep.Details -> viewModel.back()
                            ReportStep.Review  -> viewModel.back()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (state.step) {
                ReportStep.Photo   -> ReportStep1PhotoScreen(
                    state      = state,
                    viewModel  = viewModel,
                )
                ReportStep.Details -> ReportStep2DetailsScreen(
                    state      = state,
                    viewModel  = viewModel,
                )
                ReportStep.Review  -> ReportStep3ReviewScreen(
                    state      = state,
                    viewModel  = viewModel,
                )
            }
        }
    }

    // ── Discard dialog ───────────────────────────────────────────────────────
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title       = { Text(stringResource(R.string.report_discard)) },
            text        = { Text(stringResource(R.string.report_discard_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onExit()
                }) { Text(stringResource(R.string.report_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.report_keep_editing))
                }
            },
        )
    }

    // ── Rate-limit dialog ────────────────────────────────────────────────────
    if (state.rateLimited) {
        AlertDialog(
            onDismissRequest = { /* nothing */ },
            title       = { Text(stringResource(R.string.report_issue)) },
            text        = { Text(stringResource(R.string.report_rate_limited)) },
            confirmButton = {
                TextButton(onClick = onExit) { Text(stringResource(R.string.report_success_done)) }
            },
        )
    }

    // ── Submit error snackbar (simple inline for now) ────────────────────────
    state.submitError?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title       = { Text(stringResource(R.string.error_generic)) },
            text        = { Text(msg) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            },
        )
    }

}
