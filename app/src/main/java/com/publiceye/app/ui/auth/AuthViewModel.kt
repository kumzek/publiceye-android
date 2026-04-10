package com.publiceye.app.ui.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.publiceye.app.data.repository.AuthRepository
import com.publiceye.app.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading : Boolean = false,
    val error     : String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val dataStore      : DataStore<Preferences>,
) : ViewModel() {

    companion object {
        private val KEY_NOTIF_RATIONALE_SEEN = booleanPreferencesKey("notification_rationale_seen")
    }

    // ── Auth state ───────────────────────────────────────────────────────────
    val isLoggedIn: StateFlow<Boolean> = authRepository.currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), authRepository.isLoggedIn)

    // ── Notification rationale state ─────────────────────────────────────────
    val hasSeenNotificationRationale: StateFlow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_NOTIF_RATIONALE_SEEN] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── UI state ─────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Actions ──────────────────────────────────────────────────────────────

    fun signInWithGoogle(activityContext: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signInWithGoogle(activityContext)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                is AuthResult.Error   -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                is AuthResult.Error   -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun signUp(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signUpWithEmail(name, email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                is AuthResult.Error   -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.deleteAccount()) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                is AuthResult.Error   -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun markNotificationRationaleSeen() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_NOTIF_RATIONALE_SEEN] = true
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
