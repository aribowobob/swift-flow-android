package com.swiftflow.domain.repository

import com.swiftflow.domain.model.ChatMessage
import com.swiftflow.domain.model.UnreadCount
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(deliveryId: Int, beforeId: Int? = null, limit: Int? = null): Flow<Resource<List<ChatMessage>>>
    fun sendMessage(deliveryId: Int, content: String): Flow<Resource<ChatMessage>>
    fun markRead(deliveryId: Int, lastReadMessageId: Int): Flow<Resource<Unit>>
    fun getUnreadCounts(): Flow<Resource<List<UnreadCount>>>
}
