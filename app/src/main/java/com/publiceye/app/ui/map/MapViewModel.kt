package com.publiceye.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.publiceye.app.data.model.Issue
import com.publiceye.app.data.repository.IssueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val issueRepository: IssueRepository,
) : ViewModel() {

    // ── Loading state ─────────────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── Raw issues from Firestore ─────────────────────────────────────────────
    private val _allIssues = MutableStateFlow<List<Issue>>(emptyList())

    // ── Selected issue (tapped marker) ────────────────────────────────────────
    private val _selectedIssue = MutableStateFlow<Issue?>(null)
    val selectedIssue: StateFlow<Issue?> = _selectedIssue.asStateFlow()

    // ── Status filter (null = show all) ──────────────────────────────────────
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // ── Derived: filtered issue list shown on map ─────────────────────────────
    val filteredIssues: StateFlow<List<Issue>> = combine(
        _allIssues, _statusFilter,
    ) { issues, filter ->
        if (filter == null) issues else issues.filter { it.status == filter }
    }.stateIn(
        scope            = viewModelScope,
        started          = SharingStarted.WhileSubscribed(5_000),
        initialValue     = emptyList(),
    )

    // ── Derived: stats for the top bar ────────────────────────────────────────
    val activeCount: StateFlow<Int> = _allIssues
        .map { list -> list.count { it.status != Issue.STATUS_RESOLVED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalCount: StateFlow<Int> = _allIssues
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Init: start listening to Firestore ────────────────────────────────────
    init {
        viewModelScope.launch {
            issueRepository.getIssuesFlow()
                .catch { _isLoading.value = false }
                .collect { issues ->
                    _allIssues.value  = issues
                    _isLoading.value  = false
                }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    fun selectIssue(issue: Issue)       { _selectedIssue.value = issue }
    fun clearSelection()                { _selectedIssue.value = null }
    fun setStatusFilter(filter: String?) { _statusFilter.value = filter }
}
