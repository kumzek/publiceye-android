package com.publiceye.app.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Compresses an image URI to a JPEG in the app cache, suitable for Firebase Storage upload.
 *
 * Why we compress client-side:
 *   - Modern phones produce 5–12 MB photos. Uploading full res is wasteful (bandwidth + Storage
 *     cost on Blaze) and unnecessary — the UI displays pins, not pixel-peepable detail.
 *   - Target: ≤ 2048px long side, JPEG quality 85 → typically ~500 KB.
 *
 * Why we strip EXIF:
 *   - EXIF from a gallery photo may contain GPS coordinates from wherever the photo was taken —
 *     potentially someone's home. The report's location comes from the separate GPS fix, which
 *     the user explicitly consents to. We strip EXIF location to avoid leaking other location.
 *   - Rotation (EXIF orientation) is baked into the bitmap BEFORE re-encoding, so the output is
 *     correctly oriented without an orientation tag.
 */
object ImageCompressor {

    private const val MAX_DIMENSION = 2048
    private const val JPEG_QUALITY  = 85

    suspend fun compressToCache(context: Context, sourceUri: Uri): Uri =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver

            // 1. Decode bounds to compute sample size
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)

            // 2. Decode full bitmap at sampled size
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val raw = resolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: error("Could not decode image")

            // 3. Apply EXIF orientation
            val orientation = resolver.openInputStream(sourceUri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
            val rotated = applyOrientation(raw, orientation)

            // 4. Write out compressed JPEG to cache dir
            val outDir = File(context.cacheDir, "compressed_uploads").apply { mkdirs() }
            val outFile = File(outDir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(outFile).use { fos ->
                rotated.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)
            }

            // Release original if we allocated a new rotated copy
            if (rotated !== raw) raw.recycle()
            rotated.recycle()

            Uri.fromFile(outFile)
        }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        val longSide = maxOf(width, height)
        while (longSide / sample > maxDimension) sample *= 2
        return sample
    }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        if (orientation == ExifInterface.ORIENTATION_NORMAL ||
            orientation == ExifInterface.ORIENTATION_UNDEFINED) return bitmap

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90     -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180    -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270    -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
