package com.publiceye.app.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.publiceye.app.data.model.Issue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Outcome of a submitReport call.
 *
 *  - [Success] — Firestore doc written. `issueId` is the new doc's ID.
 *  - [RateLimited] — user already hit [Issue.MAX_REPORTS_PER_DAY] in the rolling 24h window.
 *  - [NotLoggedIn] — no Firebase Auth user; callers should never see this (login-gated app).
 *  - [Failed] — network / storage / firestore error. `message` is user-facing-ish.
 */
sealed class SubmitResult {
    data class  Success     (val issueId: String)   : SubmitResult()
    data object RateLimited                         : SubmitResult()
    data object NotLoggedIn                         : SubmitResult()
    data class  Failed      (val message: String)  : SubmitResult()
}

@Singleton
class IssueRepository @Inject constructor(
    private val auth       : FirebaseAuth,
    private val firestore  : FirebaseFirestore,
    private val storage    : FirebaseStorage,
) {

    /**
     * Real-time stream of all issues, ordered newest first.
     * Capped at 500 documents — enough for MVP scale. Upgrade to geo-query in V1.1.
     * The Flow stays active as long as the collector is alive (ViewModel scope).
     */
    fun getIssuesFlow(): Flow<List<Issue>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_ISSUES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(500)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val issues = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Issue::class.java)
                } ?: emptyList()
                trySend(issues)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Submit a new issue report. Runs three steps:
     *   1. Rate-limit check (client-side — will be hardened by Firestore rules in V1.1).
     *   2. Upload the already-compressed photo to `photos/{uid}/{uuid}.jpg` in Storage.
     *   3. Create the Firestore `issues` doc with the download URL.
     *
     * The caller is responsible for compression before invoking this — pass a `file://` URI
     * from the app cache. We do not re-encode here.
     */
    suspend fun submitReport(
        photoUri     : Uri,
        title        : String,
        description  : String,
        latitude     : Double,
        longitude    : Double,
        address      : String,
    ): SubmitResult {
        val user = auth.currentUser ?: return SubmitResult.NotLoggedIn
        val uid  = user.uid

        // 1. Rate-limit check
        val recent = try {
            countReportsInLast24Hours(uid)
        } catch (e: Exception) {
            // If Firestore is unavailable we can't verify — fail closed.
            return SubmitResult.Failed(e.message ?: "Couldn't verify rate limit. Try again.")
        }
        if (recent >= Issue.MAX_REPORTS_PER_DAY) {
            return SubmitResult.RateLimited
        }

        // 2. Upload photo
        val photoURL = try {
            uploadPhoto(uid, photoUri)
        } catch (e: Exception) {
            return SubmitResult.Failed(e.message ?: "Photo upload failed.")
        }

        // 3. Write Firestore doc
        return try {
            val issue = Issue(
                title       = title.trim(),
                description = description.trim(),
                category    = Issue.CATEGORY_ROADS,
                status      = Issue.STATUS_OPEN,
                location    = GeoPoint(latitude, longitude),
                address     = address,
                photoURL    = photoURL,
                upvotes     = 0,
                upvotedBy   = emptyList(),
                createdBy   = uid,
                createdAt   = Timestamp.now(),
            )
            val ref = firestore.collection(COLLECTION_ISSUES).add(issue).await()

            // Bump user's report count (best-effort — don't fail the submit if this errors)
            runCatching {
                firestore.collection(COLLECTION_USERS)
                    .document(uid)
                    .update("reportCount", FieldValue.increment(1))
                    .await()
            }

            SubmitResult.Success(ref.id)
        } catch (e: Exception) {
            SubmitResult.Failed(e.message ?: "Could not save report.")
        }
    }

    /**
     * Count how many reports this user has created in the last 24 hours.
     * Used for the 10-per-day rate limit.
     */
    private suspend fun countReportsInLast24Hours(uid: String): Int {
        val cutoff = Timestamp(Date(System.currentTimeMillis() - DAY_MILLIS))
        val snapshot = firestore.collection(COLLECTION_ISSUES)
            .whereEqualTo("createdBy", uid)
            .whereGreaterThan("createdAt", cutoff)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(Issue.MAX_REPORTS_PER_DAY.toLong() + 1) // +1 so we know if they're at or over
            .get()
            .await()
        return snapshot.size()
    }

    private suspend fun uploadPhoto(uid: String, photoUri: Uri): String {
        val path = "photos/$uid/${UUID.randomUUID()}.jpg"
        val ref  = storage.reference.child(path)
        ref.putFile(photoUri).await()
        return ref.downloadUrl.await().toString()
    }

    companion object {
        private const val COLLECTION_ISSUES = "issues"
        private const val COLLECTION_USERS  = "users"
        private const val DAY_MILLIS        = 24L * 60 * 60 * 1000
    }
}
