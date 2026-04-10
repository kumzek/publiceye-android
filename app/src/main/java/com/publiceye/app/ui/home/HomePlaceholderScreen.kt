package com.publiceye.app.ui.home

import androidx.compose.foundation.layout.*
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
 * Will be replaced in Phase 2 once wireframes are approved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePlaceholderScreen(onSignOut: () -> Unit) {
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
        }
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
            }
        }
    }
}
