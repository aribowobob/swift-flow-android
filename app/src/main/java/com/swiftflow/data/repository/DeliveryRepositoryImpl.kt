package com.swiftflow.data.repository

import android.content.Context
import android.net.Uri
import com.swiftflow.data.remote.api.DeliveryApi
import com.swiftflow.domain.model.CreateDeliveryRequest
import com.swiftflow.domain.model.Delivery
import com.swiftflow.domain.model.DeliveryListItem
import com.swiftflow.domain.model.DeliveryPhoto
import com.swiftflow.domain.model.UpdateDeliveryRequest
import com.swiftflow.domain.repository.DeliveryRepository
import com.swiftflow.utils.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class DeliveryRepositoryImpl @Inject constructor(
    private val deliveryApi: DeliveryApi,
    @ApplicationContext private val context: Context
) : DeliveryRepository {

    override suspend fun getDeliveries(
        page: Int?,
        limit: Int?,
        district: String?,
        city: String?,
        region: String?,
        status: String?
    ): Flow<Resource<List<DeliveryListItem>>> = flow {
        emit(Resource.Loading())
        val deliveries = deliveryApi.getDeliveries(page, limit, district, city, region, status)
        emit(Resource.Success(deliveries))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to fetch deliveries"))
    }

    override suspend fun getDelivery(id: Int): Flow<Resource<DeliveryListItem>> = flow {
        emit(Resource.Loading())
        val delivery = deliveryApi.getDelivery(id)
        emit(Resource.Success(delivery))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to fetch delivery details"))
    }

    override suspend fun createDelivery(request: CreateDeliveryRequest): Flow<Resource<Delivery>> = flow {
        emit(Resource.Loading())
        val delivery = deliveryApi.createDelivery(request)
        emit(Resource.Success(delivery))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to create delivery"))
    }

    override suspend fun updateDelivery(id: Int, request: UpdateDeliveryRequest): Flow<Resource<Delivery>> = flow {
        emit(Resource.Loading())
        val delivery = deliveryApi.updateDelivery(id, request)
        emit(Resource.Success(delivery))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to update delivery"))
    }

    override suspend fun uploadPhotoFile(deliveryId: Int, photoUri: Uri): Flow<Resource<DeliveryPhoto>> = flow {
        emit(Resource.Loading())

        // Read file content from Uri
        val inputStream = context.contentResolver.openInputStream(photoUri)
            ?: throw Exception("Failed to open file")
        val bytes = inputStream.use { it.readBytes() }
        val mimeType = context.contentResolver.getType(photoUri) ?: "image/jpeg"

        // Create multipart request body
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("photo", "photo.jpg", requestBody)

        // Upload to API
        val photo = deliveryApi.uploadPhotoFile(deliveryId, part)
        emit(Resource.Success(photo))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to upload photo"))
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadPhotoBytes(
        deliveryId: Int,
        imageBytes: ByteArray,
        filename: String
    ): Flow<Resource<DeliveryPhoto>> = flow {
        emit(Resource.Loading())

        // Create multipart request body from bytes
        val mimeType = if (filename.endsWith(".png")) "image/png" else "image/jpeg"
        val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("photo", filename, requestBody)

        // Upload to API
        val photo = deliveryApi.uploadPhotoFile(deliveryId, part)
        emit(Resource.Success(photo))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to upload photo"))
    }.flowOn(Dispatchers.IO)

    override suspend fun deletePhoto(photoId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        deliveryApi.deletePhoto(photoId)
        emit(Resource.Success(Unit))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to delete photo"))
    }.flowOn(Dispatchers.IO)
}
