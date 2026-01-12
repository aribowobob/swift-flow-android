package com.swiftflow.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.swiftflow.domain.model.GeocodingResult
import com.swiftflow.domain.repository.GeocodingRepository
import com.swiftflow.utils.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Implementation of GeocodingRepository using Android's built-in Geocoder
 */
class GeocodingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GeocodingRepository {

    private val geocoder = Geocoder(context, Locale("id", "ID"))

    override suspend fun reverseGeocode(lat: Double, lon: Double): Flow<Resource<GeocodingResult>> = flow {
        emit(Resource.Loading())

        val result = getAddressFromCoordinates(lat, lon)
        if (result != null) {
            emit(Resource.Success(result))
        } else {
            emit(Resource.Error("No address found for the given coordinates"))
        }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to reverse geocode"))
    }.flowOn(Dispatchers.IO)

    @Suppress("DEPRECATION")
    private suspend fun getAddressFromCoordinates(lat: Double, lon: Double): GeocodingResult? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use the new async API for Android 13+
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses.first()
                        continuation.resume(mapAddressToResult(address))
                    } else {
                        continuation.resume(null)
                    }
                }
            }
        } else {
            // Use the deprecated sync API for older Android versions
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                mapAddressToResult(addresses.first())
            } else {
                null
            }
        }
    }

    private fun mapAddressToResult(address: android.location.Address): GeocodingResult {
        // Build street from thoroughfare and subThoroughfare
        val street = buildString {
            address.thoroughfare?.let { append(it) }
            address.subThoroughfare?.let {
                if (isNotEmpty()) append(" ")
                append(it)
            }
        }.ifEmpty { null }

        // Get location name (use feature name or premises)
        val locationName = address.featureName ?: address.premises ?: street

        // Get district (subLocality)
        val district = address.subLocality ?: address.locality

        // Get city (locality or subAdminArea)
        val city = address.subAdminArea ?: address.locality

        // Get region (adminArea = province)
        val region = address.adminArea

        // Build formatted address
        val formattedAddress = buildString {
            for (i in 0..address.maxAddressLineIndex) {
                if (i > 0) append(", ")
                append(address.getAddressLine(i))
            }
        }

        return GeocodingResult(
            locationName = locationName,
            street = street,
            district = district,
            city = city,
            region = region,
            formattedAddress = formattedAddress.ifEmpty { null }
        )
    }
}
