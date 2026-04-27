package com.publiceye.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.publiceye.app.data.model.Issue
import com.publiceye.app.ui.theme.Amber40
import com.publiceye.app.ui.theme.Blue40
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ── Status badge ──────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(status: String) {
    val (bg, labelColor) = when (status) {
        Issue.STATUS_OPEN        -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        Issue.STATUS_IN_PROGRESS -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
        Issue.STATUS_RESOLVED    -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        else                     -> Color(0xFFF5F5F5) to Color(0xFF424242)
    }
    val label = when (status) {
        Issue.STATUS_OPEN        -> "Open"
        Issue.STATUS_IN_PROGRESS -> "In Progress"
        Issue.STATUS_RESOLVED    -> "Resolved"
        else                     -> status
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text          = label.uppercase(),
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = labelColor,
            letterSpacing = 0.5.sp,
        )
    }
}

// ── Issue card (used in feed list) ───────────────────────────────────────────

@Composable
fun IssueCard(
    issue: Issue,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier  = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Status + time
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                StatusBadge(issue.status)
                Text(
                    text  = issue.createdAt.toDate().timeAgo(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text       = issue.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
            )

            // Address
            if (issue.address.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text     = issue.address,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Upvote count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier           = Modifier.size(14.dp),
                    tint               = Amber40,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = "${issue.upvotes} upvote${if (issue.upvotes == 1) "" else "s"}",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = Amber40,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Bottom nav bar (shared by Map + Feed screens) ─────────────────────────────

enum class BottomNavTab { MAP, FEED, PROFILE }

private data class NavItem(val icon: ImageVector, val label: String, val tab: BottomNavTab)

@Composable
fun PublicEyeBottomNav(
    activeTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        NavItem(Icons.Default.Map,    "Map",     BottomNavTab.MAP),
        NavItem(Icons.Default.List,   "Feed",    BottomNavTab.FEED),
        NavItem(Icons.Default.Person, "Profile", BottomNavTab.PROFILE),
    )

    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            items.forEach { item ->
                val active = item.tab == activeTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(item.tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.label,
                        tint               = if (active) Blue40 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(22.dp),
                    )
                    Text(
                        text       = item.label,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = if (active) Blue40 else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    )
                    if (active) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(Blue40, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}

// ── Time formatting helper ────────────────────────────────────────────────────

private fun Date.timeAgo(): String {
    val diff = System.currentTimeMillis() - time
    return when {
        diff < TimeUnit.MINUTES.toMillis(1)  -> "just now"
        diff < TimeUnit.HOURS.toMillis(1)    -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < TimeUnit.HOURS.toMillis(24)   -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < TimeUnit.DAYS.toMillis(7)     -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(this)
    }
}
