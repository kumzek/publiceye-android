package com.publiceye.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.publiceye.app.data.model.Issue
import com.publiceye.app.data.repository.IssueRepository
import com.publiceye.app.data.repository.UpvoteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IssueDetailViewModel @Inject constructor(
    private val issueRepository : IssueRepository,
    private val auth            : FirebaseAuth,
    savedStateHandle            : SavedStateHandle,
) : ViewModel() {

    // issueId is injected by Navigation via SavedStateHandle
    private val issueId: String = checkNotNull(savedStateHandle["issueId"])

    // ── Issue state ───────────────────────────────────────────────────────────
    private val _issue = MutableStateFlow<Issue?>(null)
    val issue: StateFlow<Issue?> = _issue.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── Upvote state ──────────────────────────────────────────────────────────
    private val _isUpvoting = MutableStateFlow(false)
    val isUpvoting: StateFlow<Boolean> = _isUpvoting.asStateFlow()

    private val _upvoteError = MutableStateFlow<String?>(null)
    val upvoteError: StateFlow<String?> = _upvoteError.asStateFlow()

    // ── Current user UID (for has-upvoted check) ──────────────────────────────
    val currentUid: String? get() = auth.currentUser?.uid

    // ── Init: subscribe to real-time issue updates ────────────────────────────
    init {
        viewModelScope.launch {
            issueRepository.getIssueFlow(issueId)
                .catch { _isLoading.value = false }
                .collect { fetched ->
                    _issue.value    = fetched
                    _isLoading.value = false
                }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun toggleUpvote() {
        if (_isUpvoting.value) return // debounce rapid taps
        viewModelScope.launch {
            _isUpvoting.value   = true
            _upvoteError.value  = null
            val result = issueRepository.toggleUpvote(issueId)
            if (result is UpvoteResult.Failed) {
                _upvoteError.value = result.message
            }
            _isUpvoting.value = false
        }
    }

    fun clearUpvoteError() { _upvoteError.value = null }
}
