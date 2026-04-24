package com.publiceye.app.ui.report

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.publiceye.app.data.repository.IssueRepository
import com.publiceye.app.data.repository.LocationRepository
import com.publiceye.app.data.repository.SubmitResult
import com.publiceye.app.data.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Which step of the 3-step report flow the user is on.
 *  - [Photo]  : pick or take a photo
 *  - [Details]: set title, description, confirm location
 *  - [Review] : confirm everything, submit
 */
enum class ReportStep { Photo, Details, Review }

/** Status of the current location fix for this report. */
sealed class LocationStatus {
    data object Idle                                              : LocationStatus()
    data object Fetching                                          : LocationStatus()
    data class  Ready(val lat: Double, val lng: Double, val address: String) : LocationStatus()
    data class  Error(val message: String)                        : LocationStatus()
}

/** Overall UI state for the entire flow — Single source of truth for Compose. */
data class ReportUiState(
    val step           : ReportStep       = ReportStep.Photo,
    val photoUri       : Uri?             = null,
    val title          : String           = "",
    val description    : String           = "",
    val locationStatus : LocationStatus   = LocationStatus.Idle,
    val submitting     : Boolean          = false,
    val submitError    : String?          = null,
    val rateLimited    : Boolean          = false,
    val submittedId    : String?          = null,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val issueRepo    : IssueRepository,
    private val locationRepo : LocationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _state.asStateFlow()

    // ── Step navigation ──────────────────────────────────────────────────────
    fun goToStep(step: ReportStep) {
        _state.update { it.copy(step = step) }
    }

    fun next() {
        val s = _state.value
        when (s.step) {
            ReportStep.Photo -> {
                if (s.photoUri != null) goToStep(ReportStep.Details)
            }
            ReportStep.Details -> {
                if (s.title.isNotBlank() && s.locationStatus is LocationStatus.Ready) {
                    goToStep(ReportStep.Review)
                }
            }
            ReportStep.Review -> { /* no-op; submit handles it */ }
        }
    }

    fun back() {
        _state.update {
            when (it.step) {
                ReportStep.Photo   -> it // caller handles exit/discard
                ReportStep.Details -> it.copy(step = ReportStep.Photo)
                ReportStep.Review  -> it.copy(step = ReportStep.Details)
            }
        }
    }

    // ── Step 1: photo ────────────────────────────────────────────────────────
    /**
     * Called when the user picks a photo from camera OR gallery. We compress/re-encode
     * immediately so the ViewModel always holds a small, ready-to-upload URI.
     */
    fun onPhotoChosen(rawUri: Uri) {
        viewModelScope.launch {
            runCatching { ImageCompressor.compressToCache(context, rawUri) }
                .onSuccess { compressed -> _state.update { it.copy(photoUri = compressed) } }
                .onFailure { err ->
                    _state.update { ui ->
                        ui.copy(submitError = err.message ?: "Could not read photo.")
                    }
                }
        }
    }

    // ── Step 2: details + location ───────────────────────────────────────────
    fun onTitleChange(value: String)       { _state.update { it.copy(title = value) } }
    fun onDescriptionChange(value: String) { _state.update { it.copy(description = value) } }

    /**
     * Refresh the user's current location. Called on entering Step 2 and when user taps refresh.
     * Caller must have already confirmed location permission is granted.
     */
    fun refreshLocation() {
        viewModelScope.launch {
            _state.update { it.copy(locationStatus = LocationStatus.Fetching) }
            val loc = locationRepo.getCurrentLocation()
            if (loc == null) {
                _state.update {
                    it.copy(
                        locationStatus = LocationStatus.Error(
                            "Couldn't get your location. Make sure location is on."
                        ),
                    )
                }
                return@launch
            }
            val address = locationRepo.reverseGeocode(loc.latitude, loc.longitude)
            _state.update {
                it.copy(locationStatus = LocationStatus.Ready(loc.latitude, loc.longitude, address))
            }
        }
    }

    // ── Step 3: submit ───────────────────────────────────────────────────────
    fun submit() {
        val s = _state.value
        val photo = s.photoUri ?: return
        val loc   = s.locationStatus as? LocationStatus.Ready ?: return
        if (s.title.isBlank() || s.submitting) return

        viewModelScope.launch {
            _state.update { it.copy(submitting = true, submitError = null, rateLimited = false) }
            when (val result = issueRepo.submitReport(
                photoUri    = photo,
                title       = s.title,
                description = s.description,
                latitude    = loc.lat,
                longitude   = loc.lng,
                address     = loc.address,
            )) {
                is SubmitResult.Success -> _state.update {
                    it.copy(submitting = false, submittedId = result.issueId)
                }
                SubmitResult.RateLimited -> _state.update {
                    it.copy(submitting = false, rateLimited = true)
                }
                SubmitResult.NotLoggedIn -> _state.update {
                    it.copy(submitting = false, submitError = "Please sign in again.")
                }
                is SubmitResult.Failed -> _state.update {
                    it.copy(submitting = false, submitError = result.message)
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(submitError = null) }
    }
}
