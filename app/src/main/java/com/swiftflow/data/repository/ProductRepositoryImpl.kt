package com.swiftflow.data.repository

import com.swiftflow.data.remote.api.ProductApi
import com.swiftflow.domain.model.Product
import com.swiftflow.domain.repository.ProductRepository
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productApi: ProductApi
) : ProductRepository {

    override suspend fun getProducts(): Flow<Resource<List<Product>>> = flow {
        try {
            emit(Resource.Loading())
            val products = productApi.getProducts()
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch products"))
        }
    }
}
