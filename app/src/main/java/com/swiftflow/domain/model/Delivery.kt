package com.swiftflow.domain.model

enum class DeliveryStatus {
    READY,
    DONE,
    BROKEN,
    NEED_TO_CONFIRM
}

data class Delivery(
    val id: Int,
    val locationName: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val region: String?,
    val lat: String?,
    val lon: String?,
    val notes: String?,
    val status: DeliveryStatus,
    val createdAt: String,
    val createdBy: Int,
    val updatedAt: String,
    val updatedBy: Int
)

data class DeliveryProductInput(
    val productId: Int,
    val qty: Double
)

data class CreateDeliveryRequest(
    val locationName: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val region: String?,
    val lat: String?,
    val lon: String?,
    val notes: String?,
    val products: List<DeliveryProductInput>
)

data class DeliveryWithDetails(
    val delivery: Delivery,
    val products: List<DeliveryProductDetail>,
    val photos: List<DeliveryPhoto>
)

data class DeliveryProductDetail(
    val productId: Int,
    val productSku: String,
    val productName: String,
    val productUnit: String,
    val qty: Double
)

data class DeliveryPhoto(
    val id: Int,
    val deliveryId: Int,
    val url: String,
    val createdAt: String
)
