package com.publiceye.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.publiceye.app.ui.auth.AuthViewModel
import com.publiceye.app.ui.auth.LoginScreen
import com.publiceye.app.ui.auth.SignUpScreen
import com.publiceye.app.ui.home.HomePlaceholderScreen
import com.publiceye.app.ui.onboarding.NotificationRationaleScreen
import com.publiceye.app.ui.report.ReportFlowScreen
import com.publiceye.app.ui.report.ReportSuccessScreen

@Composable
fun PublicEyeNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    hasSeenNotificationRationale: Boolean,
) {
    // Determine start destination based on auth and onboarding state
    val startDestination = when {
        !isLoggedIn                   -> Screen.Login.route
        !hasSeenNotificationRationale -> Screen.NotificationRationale.route
        else                          -> Screen.Home.route
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {

        // ── Login ────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.NotificationRationale.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        // ── Sign Up ──────────────────────────────────────────────────────────
        composable(Screen.SignUp.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            SignUpScreen(
                viewModel = viewModel,
                onSignUpSuccess = {
                    navController.navigate(Screen.NotificationRationale.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── Notification Rationale ───────────────────────────────────────────
        composable(Screen.NotificationRationale.route) {
            NotificationRationaleScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.NotificationRationale.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home (Phase 2 placeholder, but now with Report FAB) ──────────────
        composable(Screen.Home.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            HomePlaceholderScreen(
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onReport = {
                    navController.navigate(Screen.ReportIssue.route)
                },
            )
        }

        // ── Report Flow (Photo → Details → Review) ───────────────────────────
        composable(Screen.ReportIssue.route) {
            ReportFlowScreen(
                onExit = {
                    navController.popBackStack()
                },
                onSubmitted = { issueId ->
                    navController.navigate(Screen.ReportSuccess.createRoute(issueId)) {
                        // Clear the report flow off the back stack so Back from Success
                        // returns the user to Home, not back into a submitted form.
                        popUpTo(Screen.ReportIssue.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Report Success ───────────────────────────────────────────────────
        composable(Screen.ReportSuccess.route) {
            ReportSuccessScreen(
                onDone = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
            )
        }
    }
}
