package com.swiftflow.domain.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    val id: Int,
    @SerializedName("delivery_id")
    val deliveryId: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("sender_username")
    val senderUsername: String,
    @SerializedName("sender_initial")
    val senderInitial: String,
    val content: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class SendMessageRequest(
    val content: String
)

data class MarkReadRequest(
    @SerializedName("last_read_message_id")
    val lastReadMessageId: Int
)

data class UnreadCount(
    @SerializedName("delivery_id")
    val deliveryId: Int,
    val count: Int
)
