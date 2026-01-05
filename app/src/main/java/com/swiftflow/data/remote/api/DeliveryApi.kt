package com.swiftflow.data.remote.api

import com.swiftflow.domain.model.CreateDeliveryRequest
import com.swiftflow.domain.model.Delivery
import com.swiftflow.domain.model.DeliveryListItem
import com.swiftflow.domain.model.DeliveryPhoto
import com.swiftflow.domain.model.DeliveryWithDetails
import retrofit2.http.*

interface DeliveryApi {
    @GET("deliveries")
    suspend fun getDeliveries(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("district") district: String? = null,
        @Query("city") city: String? = null,
        @Query("region") region: String? = null,
        @Query("status") status: String? = null
    ): List<DeliveryListItem>

    @GET("deliveries/{id}")
    suspend fun getDelivery(@Path("id") id: Int): DeliveryWithDetails

    @POST("deliveries")
    suspend fun createDelivery(@Body request: CreateDeliveryRequest): Delivery

    @POST("deliveries/{id}/photos")
    suspend fun uploadPhoto(
        @Path("id") deliveryId: Int,
        @Body url: String
    ): DeliveryPhoto

    @DELETE("photos/{id}")
    suspend fun deletePhoto(@Path("id") photoId: Int)
}
