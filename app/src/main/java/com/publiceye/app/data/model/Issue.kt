package com.publiceye.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

/**
 * Represents a civic issue report.
 *
 * MVP scope: `category` is hardcoded to "roads" (Potholes & Roads). A multi-category picker
 * ships in V1.1 — do not add one here without product approval.
 *
 * Status lifecycle: `open` → `in_progress` → `resolved` (moderator-driven for now).
 */
data class Issue(
    @DocumentId
    val id          : String        = "",
    val title       : String        = "",
    val description : String        = "",
    val category    : String        = CATEGORY_ROADS,
    val status      : String        = STATUS_OPEN,
    val location    : GeoPoint?     = null,
    val address     : String        = "",
    val photoURL    : String        = "",
    val upvotes     : Int           = 0,
    val upvotedBy   : List<String>  = emptyList(),
    val createdBy   : String        = "",
    val createdAt   : Timestamp     = Timestamp.now(),
) {
    companion object {
        // Categories — MVP launches with roads only. V1.1 adds sanitation, utilities, safety, parks, other.
        const val CATEGORY_ROADS = "roads"

        // Statuses
        const val STATUS_OPEN        = "open"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_RESOLVED    = "resolved"

        // Rate limit — see CivicWatch MEMORY.md.
        const val MAX_REPORTS_PER_DAY = 10
    }
}
