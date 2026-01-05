package com.swiftflow.data.repository

import com.swiftflow.data.remote.api.DeliveryApi
import com.swiftflow.domain.model.CreateDeliveryRequest
import com.swiftflow.domain.model.Delivery
import com.swiftflow.domain.model.DeliveryListItem
import com.swiftflow.domain.model.DeliveryWithDetails
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeliveryRepositoryImpl @Inject constructor(
    private val deliveryApi: DeliveryApi
) : DeliveryRepository {

    override suspend fun getDeliveries(
        page: Int?,
        limit: Int?,
        district: String?,
        city: String?,
        region: String?,
        status: String?
    ): Flow<Resource<List<DeliveryListItem>>> = flow {
        try {
            emit(Resource.Loading())
            val deliveries = deliveryApi.getDeliveries(page, limit, district, city, region, status)
            emit(Resource.Success(deliveries))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch deliveries"))
        }
    }

    override suspend fun getDelivery(id: Int): Flow<Resource<DeliveryWithDetails>> = flow {
        try {
            emit(Resource.Loading())
            val delivery = deliveryApi.getDelivery(id)
            emit(Resource.Success(delivery))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch delivery details"))
        }
    }

    override suspend fun createDelivery(request: CreateDeliveryRequest): Flow<Resource<Delivery>> = flow {
        try {
            emit(Resource.Loading())
            val delivery = deliveryApi.createDelivery(request)
            emit(Resource.Success(delivery))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create delivery"))
        }
    }
}
