package com.swiftflow.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftflow.domain.model.DeliveryListItem
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeliveryDetailState(
    val isLoading: Boolean = false,
    val delivery: DeliveryListItem? = null,
    val error: String? = null
)

@HiltViewModel
class DeliveryDetailViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryDetailState())
    val state: StateFlow<DeliveryDetailState> = _state.asStateFlow()

    fun loadDelivery(id: Int) {
        viewModelScope.launch {
            deliveryRepository.getDelivery(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                delivery = result.data,
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
}
