package com.swiftflow.domain.repository

import com.swiftflow.domain.model.Product
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): Flow<Resource<List<Product>>>
}
