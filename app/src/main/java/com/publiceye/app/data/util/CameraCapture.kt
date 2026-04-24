package com.publiceye.app.data.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Creates a temp file in the app cache and returns a FileProvider content:// URI
 * that can be passed to [androidx.activity.result.contract.ActivityResultContracts.TakePicture].
 *
 * The target file lives under `cacheDir/camera_photos/` — matches `file_provider_paths.xml`.
 */
object CameraCapture {
    fun createTempPhotoUri(context: Context): Uri {
        val dir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
        val file = File(dir, "capture_${UUID.randomUUID()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}
