package com.swiftflow.presentation.delivery.editor

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftflow.domain.model.DrawPath
import com.swiftflow.domain.model.PhotoEditorState
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class PhotoEditorViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhotoEditorState())
    val state: StateFlow<PhotoEditorState> = _state.asStateFlow()

    fun addPath(path: DrawPath) {
        _state.update { it.copy(paths = it.paths + path) }
    }

    fun undoLastPath() {
        _state.update {
            if (it.paths.isNotEmpty()) {
                it.copy(paths = it.paths.dropLast(1))
            } else {
                it
            }
        }
    }

    fun clearAllPaths() {
        _state.update { it.copy(paths = emptyList()) }
    }

    fun setColor(color: Color) {
        _state.update { it.copy(currentColor = color) }
    }

    fun setStrokeWidth(width: Float) {
        _state.update { it.copy(strokeWidth = width) }
    }

    fun saveEditedPhoto(
        deliveryId: Int,
        photoId: Int,
        editedBitmap: Bitmap,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            // Convert bitmap to bytes
            val outputStream = ByteArrayOutputStream()
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val imageBytes = outputStream.toByteArray()

            // Upload the new edited photo
            deliveryRepository.uploadPhotoBytes(
                deliveryId,
                imageBytes,
                "edited_${System.currentTimeMillis()}.jpg"
            ).collect { uploadResult ->
                when (uploadResult) {
                    is Resource.Loading -> {
                        // Already showing saving state
                    }
                    is Resource.Success -> {
                        // Now delete the old photo
                        deliveryRepository.deletePhoto(photoId).collect { deleteResult ->
                            when (deleteResult) {
                                is Resource.Loading -> {}
                                is Resource.Success -> {
                                    _state.update { it.copy(isSaving = false) }
                                    onSuccess()
                                }
                                is Resource.Error -> {
                                    // Photo uploaded but old one not deleted - still consider success
                                    _state.update { it.copy(isSaving = false) }
                                    onSuccess()
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                error = uploadResult.message
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
