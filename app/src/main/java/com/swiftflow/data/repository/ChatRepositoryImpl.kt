package com.swiftflow.data.repository

import com.swiftflow.data.remote.api.ChatApi
import com.swiftflow.domain.model.ChatMessage
import com.swiftflow.domain.model.MarkReadRequest
import com.swiftflow.domain.model.SendMessageRequest
import com.swiftflow.domain.model.UnreadCount
import com.swiftflow.domain.repository.ChatRepository
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi
) : ChatRepository {

    override fun getMessages(
        deliveryId: Int,
        beforeId: Int?,
        limit: Int?
    ): Flow<Resource<List<ChatMessage>>> = flow {
        emit(Resource.Loading())
        val messages = chatApi.getMessages(deliveryId, beforeId, limit)
        emit(Resource.Success(messages))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to fetch messages"))
    }

    override fun sendMessage(
        deliveryId: Int,
        content: String
    ): Flow<Resource<ChatMessage>> = flow {
        emit(Resource.Loading())
        val message = chatApi.sendMessage(deliveryId, SendMessageRequest(content))
        emit(Resource.Success(message))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to send message"))
    }

    override fun markRead(
        deliveryId: Int,
        lastReadMessageId: Int
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        chatApi.markRead(deliveryId, MarkReadRequest(lastReadMessageId))
        emit(Resource.Success(Unit))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to mark messages as read"))
    }

    override fun getUnreadCounts(): Flow<Resource<List<UnreadCount>>> = flow {
        emit(Resource.Loading())
        val counts = chatApi.getUnreadCounts()
        emit(Resource.Success(counts))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to fetch unread counts"))
    }
}
