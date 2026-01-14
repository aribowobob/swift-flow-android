package com.swiftflow.presentation.delivery

import android.net.Uri
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
    val error: String? = null,
    val isEditMode: Boolean = false,
    val deletingPhotoId: Int? = null,
    val isUploadingPhoto: Boolean = false
)

@HiltViewModel
class DeliveryDetailViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryDetailState())
    val state: StateFlow<DeliveryDetailState> = _state.asStateFlow()

    private var currentDeliveryId: Int = -1

    fun loadDelivery(id: Int) {
        currentDeliveryId = id
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

    fun toggleEditMode() {
        _state.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun deletePhoto(photoId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(deletingPhotoId = photoId) }

            deliveryRepository.deletePhoto(photoId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already showing loading state via deletingPhotoId
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(deletingPhotoId = null) }
                        // Reload delivery to refresh photos
                        if (currentDeliveryId != -1) {
                            loadDelivery(currentDeliveryId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                deletingPhotoId = null,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun uploadPhoto(photoUri: Uri) {
        if (currentDeliveryId == -1) return

        viewModelScope.launch {
            _state.update { it.copy(isUploadingPhoto = true) }

            deliveryRepository.uploadPhotoFile(currentDeliveryId, photoUri).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already showing loading state
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isUploadingPhoto = false) }
                        // Reload delivery to refresh photos
                        loadDelivery(currentDeliveryId)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isUploadingPhoto = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun uploadEditedPhoto(oldPhotoId: Int, imageBytes: ByteArray) {
        if (currentDeliveryId == -1) return

        viewModelScope.launch {
            _state.update { it.copy(isUploadingPhoto = true) }

            // First upload the new photo
            deliveryRepository.uploadPhotoBytes(
                currentDeliveryId,
                imageBytes,
                "edited_${System.currentTimeMillis()}.jpg"
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already showing loading state
                    }
                    is Resource.Success -> {
                        // Now delete the old photo
                        deliveryRepository.deletePhoto(oldPhotoId).collect { deleteResult ->
                            when (deleteResult) {
                                is Resource.Loading -> {}
                                is Resource.Success, is Resource.Error -> {
                                    _state.update { it.copy(isUploadingPhoto = false) }
                                    // Reload delivery to refresh photos
                                    loadDelivery(currentDeliveryId)
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isUploadingPhoto = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
