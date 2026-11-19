package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object CustomWallpaperManager {

    private const val BG_FILENAME = "custom_wallpaper_bg.jpg"
    private const val SUBJECT_FILENAME = "custom_wallpaper_subject.png"

    fun hasCustomBackground(context: Context): Boolean {
        return File(context.filesDir, BG_FILENAME).exists()
    }

    fun hasCustomSubject(context: Context): Boolean {
        return File(context.filesDir, SUBJECT_FILENAME).exists()
    }

    fun saveCustomBackground(context: Context, uri: Uri): Boolean {
        return try {
            val bitmap = decodeSampledBitmapFromUri(context, uri, 1440, 2560) ?: return false
            val file = File(context.filesDir, BG_FILENAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun saveCustomSubject(context: Context, uri: Uri): Boolean {
        return try {
            val bitmap = decodeSampledBitmapFromUri(context, uri, 1440, 2560) ?: return false
            val file = File(context.filesDir, SUBJECT_FILENAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun loadCustomBackground(context: Context, targetW: Int, targetH: Int): Bitmap? {
        val file = File(context.filesDir, BG_FILENAME)
        if (!file.exists()) return null
        return try {
            decodeSampledBitmapFromFile(file.absolutePath, targetW, targetH)
        } catch (_: Exception) {
            null
        }
    }

    fun loadCustomSubject(context: Context, targetW: Int, targetH: Int): Bitmap? {
        val file = File(context.filesDir, SUBJECT_FILENAME)
        if (!file.exists()) return null
        return try {
            decodeSampledBitmapFromFile(file.absolutePath, targetW, targetH)
        } catch (_: Exception) {
            null
        }
    }

    fun clearCustomLayers(context: Context) {
        File(context.filesDir, BG_FILENAME).delete()
        File(context.filesDir, SUBJECT_FILENAME).delete()
    }

    private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            // Decode bitmap with inSampleSize set
            inputStream = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(inputStream, null, options) ?: return null
            inputStream?.close()

            // Handle Exif rotation
            val exifStream = context.contentResolver.openInputStream(uri)
            val orientation = exifStream?.let {
                try {
                    ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } finally {
                    it.close()
                }
            } ?: ExifInterface.ORIENTATION_NORMAL

            rotateBitmapIfNeeded(decoded, orientation)
        } catch (_: Exception) {
            null
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
        }
    }

    private fun decodeSampledBitmapFromFile(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }
}
