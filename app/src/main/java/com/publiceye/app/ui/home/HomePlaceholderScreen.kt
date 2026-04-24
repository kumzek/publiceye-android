package com.publiceye.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.publiceye.app.R

/**
 * Temporary placeholder for the Home screen (Map + Feed tabs).
 *
 * In Phase 2a we added the Report FAB — the "+" button that starts the 3-step report flow.
 * The map UI itself is still pending in Phase 2a (after the report flow lands).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePlaceholderScreen(
    onSignOut : () -> Unit,
    onReport  : () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PublicEye") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text(stringResource(R.string.sign_out))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick      = onReport,
                icon         = { Icon(Icons.Default.Add, contentDescription = null) },
                text         = { Text(stringResource(R.string.home_fab_report)) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor   = MaterialTheme.colorScheme.onSecondary,
            )
        },
    ) { innerPadding ->
        Box(
            modifier          = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            contentAlignment  = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text      = "🎉",
                    style     = MaterialTheme.typography.displayMedium,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text      = "Auth is working!",
                    style     = MaterialTheme.typography.headlineSmall,
                    color     = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = stringResource(R.string.home_coming_soon),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text      = "Tap Report to submit a civic issue.",
                    style     = MaterialTheme.typography.labelLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
