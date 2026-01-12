package com.swiftflow.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

/**
 * Utility object for extracting EXIF metadata from images
 */
object ExifUtils {

    /**
     * Extracts GPS coordinates from image EXIF data
     *
     * @param context The application context
     * @param uri The URI of the image file
     * @return Pair of (latitude, longitude) or null if not available
     */
    fun extractLocation(context: Context, uri: Uri): Pair<Double, Double>? {
        return try {
            // On Android Q+, we need to request the original file to access location EXIF data
            val actualUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.setRequireOriginal(uri)
            } else {
                uri
            }

            context.contentResolver.openInputStream(actualUri)?.use { inputStream ->
                extractLocationFromStream(inputStream)
            }
        } catch (e: Exception) {
            // Fallback: try without setRequireOriginal (might work if permission is granted)
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    extractLocationFromStream(inputStream)
                }
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Extracts GPS coordinates from an input stream
     */
    private fun extractLocationFromStream(inputStream: InputStream): Pair<Double, Double>? {
        return try {
            val exif = ExifInterface(inputStream)
            val latLong = exif.latLong
            if (latLong != null && latLong.size >= 2) {
                Pair(latLong[0], latLong[1])
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts the date/time when the photo was taken
     *
     * @param context The application context
     * @param uri The URI of the image file
     * @return DateTime string or null if not available
     */
    fun extractDateTime(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts all relevant EXIF data for a photo
     *
     * @param context The application context
     * @param uri The URI of the image file
     * @return ExifData object with extracted metadata
     */
    fun extractExifData(context: Context, uri: Uri): ExifData {
        var latitude: Double? = null
        var longitude: Double? = null
        var dateTime: String? = null

        // On Android Q+, we need to request the original file to access location EXIF data
        val actualUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                MediaStore.setRequireOriginal(uri)
            } catch (e: Exception) {
                uri
            }
        } else {
            uri
        }

        try {
            context.contentResolver.openInputStream(actualUri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)

                exif.latLong?.let {
                    if (it.size >= 2) {
                        latitude = it[0]
                        longitude = it[1]
                    }
                }

                dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            }
        } catch (e: Exception) {
            // Fallback: try without setRequireOriginal
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val exif = ExifInterface(inputStream)

                    exif.latLong?.let {
                        if (it.size >= 2) {
                            latitude = it[0]
                            longitude = it[1]
                        }
                    }

                    dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                        ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                }
            } catch (e2: Exception) {
                // Silently fail - EXIF data is optional
            }
        }

        return ExifData(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )
    }
}

/**
 * Data class to hold extracted EXIF metadata
 */
data class ExifData(
    val latitude: Double?,
    val longitude: Double?,
    val dateTime: String?
)
