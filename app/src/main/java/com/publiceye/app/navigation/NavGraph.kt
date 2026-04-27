package com.publiceye.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.publiceye.app.ui.auth.AuthViewModel
import com.publiceye.app.ui.auth.LoginScreen
import com.publiceye.app.ui.auth.SignUpScreen
import com.publiceye.app.ui.detail.IssueDetailScreen
import com.publiceye.app.ui.feed.FeedScreen
import com.publiceye.app.ui.map.MapScreen
import com.publiceye.app.ui.onboarding.NotificationRationaleScreen
import com.publiceye.app.ui.report.ReportFlowScreen
import com.publiceye.app.ui.report.ReportSuccessScreen

@Composable
fun PublicEyeNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    hasSeenNotificationRationale: Boolean,
) {
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

        // ── Home — Map screen ─────────────────────────────────────────────────
        composable(Screen.Home.route) {
            MapScreen(
                onNavigateToReport = {
                    navController.navigate(Screen.ReportIssue.route)
                },
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                },
            )
        }

        // ── Feed ──────────────────────────────────────────────────────────────
        composable(Screen.Feed.route) {
            FeedScreen(
                onNavigateToMap = {
                    // Pop back to Map (Home) rather than re-creating it
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onNavigateToDetail = { issueId ->
                    navController.navigate(Screen.IssueDetail.createRoute(issueId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                },
            )
        }

        // ── Issue Detail ──────────────────────────────────────────────────────
        composable(
            route     = Screen.IssueDetail.route,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType }),
        ) {
            IssueDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Report Flow (Photo → Details → Review) ────────────────────────────
        composable(Screen.ReportIssue.route) {
            ReportFlowScreen(
                onExit = {
                    navController.popBackStack()
                },
                onSubmitted = { issueId ->
                    navController.navigate(Screen.ReportSuccess.createRoute(issueId)) {
                        popUpTo(Screen.ReportIssue.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Report Success ────────────────────────────────────────────────────
        composable(Screen.ReportSuccess.route) {
            ReportSuccessScreen(
                onDone = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
            )
        }

        // ── Profile (Phase 3+ stub) ───────────────────────────────────────────
        composable(Screen.Profile.route) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "Profile — coming soon",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
