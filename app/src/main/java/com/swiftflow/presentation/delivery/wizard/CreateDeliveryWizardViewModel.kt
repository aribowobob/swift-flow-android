package com.swiftflow.presentation.delivery.wizard

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftflow.domain.model.CreateDeliveryRequest
import com.swiftflow.domain.model.CreateDeliveryWizardState
import com.swiftflow.domain.model.DeliveryProductInput
import com.swiftflow.domain.model.DeliveryStatus
import com.swiftflow.domain.model.PhotoUploadState
import com.swiftflow.domain.model.Product
import com.swiftflow.domain.model.ProductQuantity
import com.swiftflow.domain.model.SelectedPhoto
import com.swiftflow.domain.model.UpdateDeliveryRequest
import com.swiftflow.domain.model.WizardStep
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.domain.repository.GeocodingRepository
import com.swiftflow.domain.repository.ProductRepository
import com.swiftflow.utils.ExifUtils
import com.swiftflow.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateDeliveryWizardViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val geocodingRepository: GeocodingRepository,
    private val productRepository: ProductRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CreateDeliveryWizardState())
    val state: StateFlow<CreateDeliveryWizardState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update { it.copy(availableProducts = resource.data ?: emptyList()) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(error = resource.message) }
                    }
                    is Resource.Loading -> { /* Ignore loading for products */ }
                }
            }
        }
    }

    // ==================== Step 1: Photo Selection ====================

    fun addPhotos(uris: List<Uri>) {
        val currentCount = _state.value.selectedPhotos.size
        val remainingSlots = MAX_PHOTOS - currentCount
        val newUris = uris.take(remainingSlots)

        val newPhotos = newUris.map { uri ->
            val location = ExifUtils.extractLocation(context, uri)
            SelectedPhoto(
                uri = uri,
                latitude = location?.first,
                longitude = location?.second
            )
        }

        _state.update {
            it.copy(selectedPhotos = it.selectedPhotos + newPhotos)
        }
    }

    fun removePhoto(photo: SelectedPhoto) {
        _state.update {
            it.copy(selectedPhotos = it.selectedPhotos.filter { p -> p.uri != photo.uri })
        }
    }

    fun proceedToStep2() {
        if (_state.value.selectedPhotos.isEmpty()) {
            _state.update { it.copy(error = "Please select at least one photo") }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUploading = true,
                    error = null,
                    uploadProgress = 0,
                    totalPhotos = it.selectedPhotos.size
                )
            }

            try {
                // Step 1: Create delivery with NEED_TO_CONFIRM status
                val createRequest = CreateDeliveryRequest(
                    locationName = null,
                    street = null,
                    district = null,
                    city = null,
                    region = null,
                    lat = null,
                    lon = null,
                    notes = null,
                    status = DeliveryStatus.NEED_TO_CONFIRM,
                    products = emptyList()
                )

                val createResult = deliveryRepository.createDelivery(createRequest).first { it !is Resource.Loading }
                if (createResult is Resource.Error) {
                    _state.update { it.copy(isUploading = false, error = createResult.message) }
                    return@launch
                }

                val delivery = (createResult as Resource.Success).data!!
                _state.update { it.copy(deliveryId = delivery.id) }

                // Step 2: Extract location from first photo and geocode
                val firstPhoto = _state.value.selectedPhotos.first()
                var lat: String? = null
                var lon: String? = null

                if (firstPhoto.latitude != null && firstPhoto.longitude != null) {
                    lat = firstPhoto.latitude.toString()
                    lon = firstPhoto.longitude.toString()

                    // Call geocoding API
                    val geocodeResult = geocodingRepository.reverseGeocode(
                        firstPhoto.latitude,
                        firstPhoto.longitude
                    ).first { it !is Resource.Loading }

                    if (geocodeResult is Resource.Success) {
                        val geoData = geocodeResult.data!!
                        _state.update {
                            it.copy(
                                latitude = lat ?: "",
                                longitude = lon ?: "",
                                locationName = geoData.locationName ?: "",
                                street = geoData.street ?: "",
                                district = geoData.district ?: "",
                                city = geoData.city ?: "",
                                region = geoData.region ?: ""
                            )
                        }
                    } else {
                        // Geocoding failed, just use coordinates
                        _state.update {
                            it.copy(
                                latitude = lat ?: "",
                                longitude = lon ?: ""
                            )
                        }
                    }
                }

                // Step 3: Upload photos sequentially
                val photos = _state.value.selectedPhotos
                for ((index, photo) in photos.withIndex()) {
                    _state.update {
                        val updatedPhotos = it.selectedPhotos.map { p ->
                            if (p.uri == photo.uri) p.copy(uploadState = PhotoUploadState.UPLOADING)
                            else p
                        }
                        it.copy(
                            selectedPhotos = updatedPhotos,
                            uploadProgress = index + 1
                        )
                    }

                    val uploadResult = deliveryRepository.uploadPhotoFile(delivery.id, photo.uri)
                        .first { it !is Resource.Loading }

                    if (uploadResult is Resource.Error) {
                        _state.update {
                            val updatedPhotos = it.selectedPhotos.map { p ->
                                if (p.uri == photo.uri) p.copy(uploadState = PhotoUploadState.FAILED)
                                else p
                            }
                            it.copy(
                                selectedPhotos = updatedPhotos,
                                isUploading = false,
                                error = "Failed to upload photo ${index + 1}: ${uploadResult.message}"
                            )
                        }
                        return@launch
                    }

                    _state.update {
                        val updatedPhotos = it.selectedPhotos.map { p ->
                            if (p.uri == photo.uri) p.copy(uploadState = PhotoUploadState.UPLOADED)
                            else p
                        }
                        it.copy(selectedPhotos = updatedPhotos)
                    }
                }

                // Step 4: Update delivery with location data
                if (lat != null && lon != null) {
                    val updateRequest = UpdateDeliveryRequest(
                        lat = lat,
                        lon = lon,
                        locationName = _state.value.locationName.ifBlank { null },
                        street = _state.value.street.ifBlank { null },
                        district = _state.value.district.ifBlank { null },
                        city = _state.value.city.ifBlank { null },
                        region = _state.value.region.ifBlank { null }
                    )
                    deliveryRepository.updateDelivery(delivery.id, updateRequest).first { it !is Resource.Loading }
                }

                // Move to Step 2
                _state.update {
                    it.copy(
                        isUploading = false,
                        currentStep = WizardStep.ReviewLocation
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    // ==================== Step 2: Review Location ====================

    fun updateLocationName(value: String) {
        _state.update { it.copy(locationName = value) }
    }

    fun updateStreet(value: String) {
        _state.update { it.copy(street = value) }
    }

    fun updateDistrict(value: String) {
        _state.update { it.copy(district = value) }
    }

    fun updateCity(value: String) {
        _state.update { it.copy(city = value) }
    }

    fun updateRegion(value: String) {
        _state.update { it.copy(region = value) }
    }

    fun updateLatitude(value: String) {
        _state.update { it.copy(latitude = value) }
    }

    fun updateLongitude(value: String) {
        _state.update { it.copy(longitude = value) }
    }

    fun proceedToStep3() {
        // Validate all fields are filled
        val currentState = _state.value
        if (currentState.locationName.isBlank() ||
            currentState.street.isBlank() ||
            currentState.district.isBlank() ||
            currentState.city.isBlank() ||
            currentState.region.isBlank()
        ) {
            _state.update { it.copy(error = "All location fields are required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val deliveryId = _state.value.deliveryId ?: throw Exception("Delivery ID not found")

                val updateRequest = UpdateDeliveryRequest(
                    locationName = currentState.locationName,
                    street = currentState.street,
                    district = currentState.district,
                    city = currentState.city,
                    region = currentState.region,
                    lat = currentState.latitude.ifBlank { null },
                    lon = currentState.longitude.ifBlank { null }
                )

                val result = deliveryRepository.updateDelivery(deliveryId, updateRequest)
                    .first { it !is Resource.Loading }

                if (result is Resource.Error) {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    return@launch
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        currentStep = WizardStep.AddProducts
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update location"
                    )
                }
            }
        }
    }

    // ==================== Step 3: Add Products ====================

    fun addProduct(product: Product, quantity: Double = 1.0) {
        val alreadySelected = _state.value.selectedProducts.any { it.product.id == product.id }
        if (alreadySelected) return

        _state.update {
            it.copy(
                selectedProducts = it.selectedProducts + ProductQuantity(product, quantity)
            )
        }
    }

    fun removeProduct(productId: Int) {
        _state.update {
            it.copy(
                selectedProducts = it.selectedProducts.filter { p -> p.product.id != productId }
            )
        }
    }

    fun updateProductQuantity(productId: Int, quantity: Double) {
        if (quantity <= 0) return

        _state.update {
            it.copy(
                selectedProducts = it.selectedProducts.map { p ->
                    if (p.product.id == productId) p.copy(quantity = quantity)
                    else p
                }
            )
        }
    }

    fun updateNotes(value: String) {
        _state.update { it.copy(notes = value) }
    }

    fun saveDelivery() {
        if (_state.value.selectedProducts.isEmpty()) {
            _state.update { it.copy(error = "Please add at least one product") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val deliveryId = _state.value.deliveryId ?: throw Exception("Delivery ID not found")

                val productInputs = _state.value.selectedProducts.map {
                    DeliveryProductInput(
                        productId = it.product.id,
                        qty = it.quantity
                    )
                }

                val updateRequest = UpdateDeliveryRequest(
                    notes = _state.value.notes.ifBlank { null },
                    status = DeliveryStatus.READY,
                    products = productInputs
                )

                val result = deliveryRepository.updateDelivery(deliveryId, updateRequest)
                    .first { it !is Resource.Loading }

                if (result is Resource.Error) {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    return@launch
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isCompleted = true
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save delivery"
                    )
                }
            }
        }
    }

    // ==================== Common ====================

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    companion object {
        const val MAX_PHOTOS = 10
    }
}
