package com.swiftflow.domain.model

import com.google.gson.annotations.SerializedName

enum class DeliveryStatus {
    @SerializedName("Ready")
    READY,

    @SerializedName("Done")
    DONE,

    @SerializedName("Broken")
    BROKEN,

    @SerializedName("NeedToConfirm")
    NEED_TO_CONFIRM
}

data class Delivery(
    val id: Int,
    @SerializedName("location_name")
    val locationName: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val region: String?,
    val lat: String?,
    val lon: String?,
    val notes: String?,
    val status: DeliveryStatus,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_by")
    val updatedBy: Int
)

data class DeliveryProductInput(
    @SerializedName("product_id")
    val productId: Int,
    val qty: Double
)

data class CreateDeliveryRequest(
    @SerializedName("location_name")
    val locationName: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val region: String?,
    val lat: String?,
    val lon: String?,
    val notes: String?,
    val status: DeliveryStatus? = null,
    val products: List<DeliveryProductInput>
)

data class UpdateDeliveryRequest(
    @SerializedName("location_name")
    val locationName: String? = null,
    val street: String? = null,
    val district: String? = null,
    val city: String? = null,
    val region: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val notes: String? = null,
    val status: DeliveryStatus? = null,
    val products: List<DeliveryProductInput>? = null
)

// For list view - flattened delivery with products
data class DeliveryListItem(
    val id: Int,
    @SerializedName("location_name")
    val locationName: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val region: String?,
    val lat: String?,
    val lon: String?,
    val notes: String?,
    val status: DeliveryStatus,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_by")
    val updatedBy: Int,
    val products: List<DeliveryProductDetail>,
    val photos: List<DeliveryPhoto>
)

// For detail view
data class DeliveryWithDetails(
    val delivery: Delivery,
    val products: List<DeliveryProductDetail>,
    val photos: List<DeliveryPhoto>
)

data class DeliveryProductDetail(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("product_sku")
    val productSku: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_unit")
    val productUnit: String,
    val qty: Double
)

data class DeliveryPhoto(
    val id: Int,
    @SerializedName("delivery_id")
    val deliveryId: Int,
    val url: String,
    @SerializedName("created_at")
    val createdAt: String
)
