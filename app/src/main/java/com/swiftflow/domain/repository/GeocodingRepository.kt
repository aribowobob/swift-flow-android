package com.swiftflow.domain.repository

import com.swiftflow.domain.model.GeocodingResult
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for geocoding operations
 */
interface GeocodingRepository {

    /**
     * Reverse geocode coordinates to get address information
     *
     * @param lat Latitude
     * @param lon Longitude
     * @return Flow of Resource containing GeocodingResult
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): Flow<Resource<GeocodingResult>>
}
