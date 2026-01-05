package com.swiftflow.data.repository

import com.swiftflow.data.remote.api.ProductApi
import com.swiftflow.domain.model.CreateProductRequest
import com.swiftflow.domain.model.Product
import com.swiftflow.domain.model.UpdateProductRequest
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

    override suspend fun createProduct(request: CreateProductRequest): Flow<Resource<Product>> = flow {
        try {
            emit(Resource.Loading())
            val product = productApi.createProduct(request)
            emit(Resource.Success(product))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create product"))
        }
    }

    override suspend fun updateProduct(id: Int, request: UpdateProductRequest): Flow<Resource<Product>> = flow {
        try {
            emit(Resource.Loading())
            val product = productApi.updateProduct(id, request)
            emit(Resource.Success(product))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update product"))
        }
    }

    override suspend fun deleteProduct(id: Int): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val response = productApi.deleteProduct(id)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to delete product: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete product"))
        }
    }
}
