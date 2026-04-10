package com.publiceye.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.publiceye.app.navigation.PublicEyeNavGraph
import com.publiceye.app.ui.auth.AuthViewModel
import com.publiceye.app.ui.theme.PublicEyeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PublicEyeTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()

                val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
                val hasSeenNotificationRationale by authViewModel
                    .hasSeenNotificationRationale.collectAsStateWithLifecycle()

                PublicEyeNavGraph(
                    navController                = navController,
                    isLoggedIn                   = isLoggedIn,
                    hasSeenNotificationRationale = hasSeenNotificationRationale,
                )
            }
        }
    }
}
