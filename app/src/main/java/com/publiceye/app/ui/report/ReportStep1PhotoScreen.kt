package com.publiceye.app.ui.report

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.publiceye.app.R
import com.publiceye.app.data.util.CameraCapture

/**
 * Step 1 of 3. User picks a photo via camera or gallery.
 *
 * Camera flow uses [ActivityResultContracts.TakePicture] with a FileProvider URI pre-created
 * in cache. Gallery uses [ActivityResultContracts.PickVisualMedia] (system photo picker — no
 * READ_MEDIA_IMAGES permission needed on Android 13+).
 */
@Composable
fun ReportStep1PhotoScreen(
    state     : ReportUiState,
    viewModel : ReportViewModel,
) {
    val context = LocalContext.current

    // Track the URI we hand to the camera so we can consume it on success
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            pendingCameraUri?.let { viewModel.onPhotoChosen(it) }
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = CameraCapture.createTempPhotoUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onPhotoChosen(uri)
    }

    fun launchCamera() {
        // Since we only want a photo (not recording), runtime permission is only required
        // on devices where the camera app doesn't do its own consent (edge case for pre-
        // installed camera apps that ACCESS_FINE_LOCATION is configured with). Android's
        // standard pattern: request CAMERA permission before launching TakePicture.
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun launchGallery() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text  = stringResource(R.string.report_photo_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = stringResource(R.string.report_photo_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        // Preview or placeholder
        Surface(
            modifier    = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(12.dp)),
            color       = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
        ) {
            if (state.photoUri != null) {
                AsyncImage(
                    model               = state.photoUri,
                    contentDescription  = null,
                    modifier            = Modifier.fillMaxSize(),
                    contentScale        = ContentScale.Crop,
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(56.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick  = { launchGallery() },
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.report_choose_from_gallery))
            }
            Button(
                onClick  = { launchCamera() },
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.photoUri == null) stringResource(R.string.report_take_photo)
                    else stringResource(R.string.report_retake_photo)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick  = viewModel::next,
            enabled  = state.photoUri != null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(R.string.report_next))
        }
    }
}
