package com.swiftflow.data.remote.api

import com.swiftflow.domain.model.CreateProductRequest
import com.swiftflow.domain.model.Product
import com.swiftflow.domain.model.UpdateProductRequest
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @Headers("Cache-Control: no-cache", "Pragma: no-cache")
    @GET("products")
    suspend fun getProducts(): List<Product>

    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Product

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: UpdateProductRequest
    ): Product

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>
}
