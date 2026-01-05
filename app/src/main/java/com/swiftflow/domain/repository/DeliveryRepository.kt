package com.swiftflow.domain.repository

import com.swiftflow.domain.model.CreateDeliveryRequest
import com.swiftflow.domain.model.Delivery
import com.swiftflow.domain.model.DeliveryListItem
import com.swiftflow.domain.model.DeliveryWithDetails
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
}
