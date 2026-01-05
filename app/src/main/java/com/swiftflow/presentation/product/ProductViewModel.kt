package com.swiftflow.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftflow.domain.model.CreateProductRequest
import com.swiftflow.domain.model.Product
import com.swiftflow.domain.model.UpdateProductRequest
import com.swiftflow.domain.repository.ProductRepository
import com.swiftflow.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null,
    val operationSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                products = result.data ?: emptyList(),
                                error = null,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                errorMessage = "Failed to load products"
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun reloadProductsAndComplete(successMessage: String) {
        productRepository.getProducts().collect { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            products = result.data ?: emptyList(),
                            isLoading = false,
                            operationSuccess = true,
                            successMessage = successMessage,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    // Even if reload fails, mark operation as successful
                    _state.update {
                        it.copy(
                            isLoading = false,
                            operationSuccess = true,
                            successMessage = successMessage,
                            errorMessage = null
                        )
                    }
                }
                else -> {
                    // Ignore loading state during reload
                }
            }
        }
    }

    fun createProduct(sku: String, name: String, unit: String) {
        viewModelScope.launch {
            val request = CreateProductRequest(sku, name, unit)
            productRepository.createProduct(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null, operationSuccess = false, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        // Reload products and mark operation as complete
                        reloadProductsAndComplete("Product created successfully")
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                operationSuccess = false,
                                errorMessage = result.message ?: "Failed to create product"
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateProduct(id: Int, name: String?, unit: String?) {
        viewModelScope.launch {
            val request = UpdateProductRequest(name = name, unit = unit)
            productRepository.updateProduct(id, request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null, operationSuccess = false, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        // Reload products and mark operation as complete
                        reloadProductsAndComplete("Product updated successfully")
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                operationSuccess = false,
                                errorMessage = result.message ?: "Failed to update product"
                            )
                        }
                    }
                }
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            productRepository.deleteProduct(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        // Reload products after delete
                        productRepository.getProducts().collect { productsResult ->
                            when (productsResult) {
                                is Resource.Success -> {
                                    _state.update {
                                        it.copy(
                                            products = productsResult.data ?: emptyList(),
                                            isLoading = false,
                                            error = null,
                                            successMessage = "Product deleted successfully",
                                            errorMessage = null
                                        )
                                    }
                                }
                                is Resource.Error -> {
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            error = null,
                                            successMessage = "Product deleted successfully",
                                            errorMessage = null
                                        )
                                    }
                                }
                                else -> {
                                    // Ignore loading state
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                errorMessage = result.message ?: "Failed to delete product"
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearOperationSuccess() {
        _state.update { it.copy(operationSuccess = false) }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }
}
