package com.swiftflow.domain.repository

import com.swiftflow.domain.model.CreateProductRequest
import com.swiftflow.domain.model.Product
import com.swiftflow.domain.model.UpdateProductRequest
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): Flow<Resource<List<Product>>>
    suspend fun createProduct(request: CreateProductRequest): Flow<Resource<Product>>
    suspend fun updateProduct(id: Int, request: UpdateProductRequest): Flow<Resource<Product>>
    suspend fun deleteProduct(id: Int): Flow<Resource<Unit>>
}
