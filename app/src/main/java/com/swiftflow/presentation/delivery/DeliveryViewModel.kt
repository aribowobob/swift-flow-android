package com.swiftflow.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftflow.domain.model.CreateDeliveryRequest
import com.swiftflow.domain.model.Delivery
import com.swiftflow.domain.model.DeliveryProductInput
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeliveryState(
    val isLoading: Boolean = false,
    val deliveries: List<Delivery> = emptyList(),
    val error: String? = null,
    val createdDelivery: Delivery? = null
)

@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryState())
    val state: StateFlow<DeliveryState> = _state.asStateFlow()

    init {
        loadDeliveries()
    }

    fun loadDeliveries(
        district: String? = null,
        city: String? = null,
        region: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            deliveryRepository.getDeliveries(
                district = district,
                city = city,
                region = region,
                status = status
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                deliveries = result.data ?: emptyList(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun createDelivery(
        locationName: String?,
        street: String?,
        district: String?,
        city: String?,
        region: String?,
        lat: String?,
        lon: String?,
        notes: String?,
        products: List<DeliveryProductInput>
    ) {
        viewModelScope.launch {
            val request = CreateDeliveryRequest(
                locationName = locationName,
                street = street,
                district = district,
                city = city,
                region = region,
                lat = lat,
                lon = lon,
                notes = notes,
                products = products
            )

            deliveryRepository.createDelivery(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                createdDelivery = result.data,
                                error = null
                            )
                        }
                        loadDeliveries() // Refresh list
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearCreatedDelivery() {
        _state.update { it.copy(createdDelivery = null) }
    }
}
