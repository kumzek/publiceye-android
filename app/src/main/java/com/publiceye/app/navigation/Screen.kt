package com.publiceye.app.navigation

/**
 * All navigation destinations in PublicEye.
 * Add new screens here as phases progress — do not hardcode route strings elsewhere.
 */
sealed class Screen(val route: String) {

    // ── Phase 1: Auth & Onboarding ──────────────────────────────────────────
    data object Login                   : Screen("login")
    data object SignUp                  : Screen("signup")
    data object NotificationRationale   : Screen("notification_rationale")

    // ── Phase 2: Core (placeholders until wireframes are approved) ──────────
    data object Home                    : Screen("home")          // Map + Feed tabs
    data object ReportIssue             : Screen("report_issue")  // Report submission
    data object IssueDetail             : Screen("issue_detail/{issueId}") {
        fun createRoute(issueId: String) = "issue_detail/$issueId"
    }
    data object Profile                 : Screen("profile")
}
