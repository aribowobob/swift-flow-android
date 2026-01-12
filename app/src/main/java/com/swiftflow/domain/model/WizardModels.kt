package com.swiftflow.domain.model

import android.net.Uri

/**
 * Represents a photo selected for upload with extracted metadata
 */
data class SelectedPhoto(
    val uri: Uri,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val uploadState: PhotoUploadState = PhotoUploadState.PENDING
)

/**
 * State of a photo in the upload process
 */
enum class PhotoUploadState {
    PENDING,
    UPLOADING,
    UPLOADED,
    FAILED
}

/**
 * Current step in the delivery creation wizard
 */
sealed class WizardStep {
    data object SelectPhotos : WizardStep()
    data object ReviewLocation : WizardStep()
    data object AddProducts : WizardStep()
}

/**
 * Product with quantity for wizard state
 */
data class ProductQuantity(
    val product: Product,
    val quantity: Double
)

/**
 * State for the create delivery wizard
 */
data class CreateDeliveryWizardState(
    val currentStep: WizardStep = WizardStep.SelectPhotos,
    val selectedPhotos: List<SelectedPhoto> = emptyList(),
    val deliveryId: Int? = null,
    val uploadProgress: Int = 0,
    val totalPhotos: Int = 0,
    val isUploading: Boolean = false,

    // Location fields (populated from EXIF + Geocoding)
    val latitude: String = "",
    val longitude: String = "",
    val locationName: String = "",
    val street: String = "",
    val district: String = "",
    val city: String = "",
    val region: String = "",

    // Products (reuse existing functionality)
    val selectedProducts: List<ProductQuantity> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val notes: String = "",

    // State flags
    val error: String? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false
)

/**
 * Result from geocoding API
 */
data class GeocodingResult(
    val locationName: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val region: String?,
    val formattedAddress: String?
)
