package com.swiftflow.domain.repository

import android.net.Uri
import com.swiftflow.domain.model.CreateDeliveryRequest
import com.swiftflow.domain.model.Delivery
import com.swiftflow.domain.model.DeliveryListItem
import com.swiftflow.domain.model.DeliveryPhoto
import com.swiftflow.domain.model.DeliveryWithDetails
import com.swiftflow.domain.model.UpdateDeliveryRequest
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    suspend fun getDeliveries(
        page: Int? = null,
        limit: Int? = null,
        district: String? = null,
        city: String? = null,
        region: String? = null,
        status: String? = null
    ): Flow<Resource<List<DeliveryListItem>>>

    suspend fun getDelivery(id: Int): Flow<Resource<DeliveryWithDetails>>

    suspend fun createDelivery(request: CreateDeliveryRequest): Flow<Resource<Delivery>>

    suspend fun updateDelivery(id: Int, request: UpdateDeliveryRequest): Flow<Resource<Delivery>>

    suspend fun uploadPhotoFile(deliveryId: Int, photoUri: Uri): Flow<Resource<DeliveryPhoto>>
}
