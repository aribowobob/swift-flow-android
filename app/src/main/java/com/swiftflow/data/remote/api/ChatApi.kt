package com.swiftflow.data.remote.api

import com.swiftflow.domain.model.ChatMessage
import com.swiftflow.domain.model.MarkReadRequest
import com.swiftflow.domain.model.SendMessageRequest
import com.swiftflow.domain.model.UnreadCount
import retrofit2.http.*

interface ChatApi {
    @GET("deliveries/{id}/messages")
    suspend fun getMessages(
        @Path("id") deliveryId: Int,
        @Query("before_id") beforeId: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<ChatMessage>

    @POST("deliveries/{id}/messages")
    suspend fun sendMessage(
        @Path("id") deliveryId: Int,
        @Body request: SendMessageRequest
    ): ChatMessage

    @POST("deliveries/{id}/messages/read")
    suspend fun markRead(
        @Path("id") deliveryId: Int,
        @Body request: MarkReadRequest
    )

    @GET("messages/unread")
    suspend fun getUnreadCounts(): List<UnreadCount>
}
