package com.swiftflow.data.remote.api

import com.swiftflow.domain.model.Product
import retrofit2.http.GET

interface ProductApi {
    @GET("products")
    suspend fun getProducts(): List<Product>
}
