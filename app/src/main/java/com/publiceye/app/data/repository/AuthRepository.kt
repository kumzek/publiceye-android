package com.publiceye.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.publiceye.app.BuildConfig
import com.publiceye.app.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: Flow<FirebaseUser?> = _currentUser.asStateFlow()

    val isLoggedIn: Boolean get() = auth.currentUser != null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    // ── Google Sign-In ───────────────────────────────────────────────────────
    suspend fun signInWithGoogle(activityContext: Context): AuthResult {
        return try {
            val credentialManager = CredentialManager.create(activityContext)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                authResult.user?.let { createUserDocumentIfNeeded(it) }
                AuthResult.Success
            } else {
                AuthResult.Error("Unsupported credential type")
            }
        } catch (e: GetCredentialException) {
            AuthResult.Error(e.message ?: "Google sign-in failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unexpected error occurred")
        }
    }

    // ── Email / Password Sign-In ─────────────────────────────────────────────
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-in failed")
        }
    }

    // ── Email / Password Sign-Up ─────────────────────────────────────────────
    suspend fun signUpWithEmail(name: String, email: String, password: String): AuthResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { user ->
                // Update display name
                val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdate).await()
                createUserDocumentIfNeeded(user, name)
            }
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-up failed")
        }
    }

    // ── Sign Out ─────────────────────────────────────────────────────────────
    fun signOut() {
        auth.signOut()
    }

    // ── Account Deletion (Play Store requirement) ────────────────────────────
    suspend fun deleteAccount(): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("Not logged in")
            // Delete Firestore user document
            firestore.collection("users").document(user.uid).delete().await()
            // Delete Firebase Auth account
            user.delete().await()
            AuthResult.Success
        } catch (e: Exception) {
            // Re-authentication may be required — surface this to the user
            AuthResult.Error(e.message ?: "Account deletion failed. Please sign out and sign back in, then try again.")
        }
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────
    private suspend fun createUserDocumentIfNeeded(firebaseUser: FirebaseUser, overrideName: String? = null) {
        val docRef = firestore.collection("users").document(firebaseUser.uid)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val user = User(
                uid         = firebaseUser.uid,
                displayName = overrideName ?: firebaseUser.displayName ?: "",
                email       = firebaseUser.email ?: "",
                photoUrl    = firebaseUser.photoUrl?.toString() ?: "",
            )
            docRef.set(user).await()
        }
    }
}
