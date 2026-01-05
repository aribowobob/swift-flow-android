package com.swiftflow.domain.model

data class Product(
    val id: Int,
    val sku: String,
    val name: String,
    val unit: String
)

data class CreateProductRequest(
    val sku: String,
    val name: String,
    val unit: String
)

data class UpdateProductRequest(
    val sku: String? = null,
    val name: String? = null,
    val unit: String? = null
)
