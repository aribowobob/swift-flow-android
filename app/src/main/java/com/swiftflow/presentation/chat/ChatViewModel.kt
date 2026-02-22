package com.swiftflow.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftflow.data.remote.ChatWebSocketClient
import com.swiftflow.domain.model.ChatMessage
import com.swiftflow.domain.repository.ChatRepository
import com.swiftflow.utils.Resource
import com.swiftflow.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val currentUserId: Int? = null,
    val isConnected: Boolean = false,
    val canLoadMore: Boolean = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatWebSocketClient: ChatWebSocketClient,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var deliveryId: Int = -1
    private val messageIds = mutableSetOf<Int>()

    fun initialize(deliveryId: Int) {
        if (this.deliveryId == deliveryId) return
        this.deliveryId = deliveryId
        loadCurrentUser()
        loadMessages()
        connectWebSocket()
        observeIncomingMessages()
        observeConnectionState()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId().first()
            _state.update { it.copy(currentUserId = userId) }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(deliveryId, limit = 50).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val messages = result.data ?: emptyList()
                        // Messages come in DESC order from API, reverse for display
                        val sorted = messages.sortedBy { it.id }
                        messageIds.addAll(sorted.map { it.id })
                        _state.update {
                            it.copy(
                                isLoading = false,
                                messages = sorted,
                                canLoadMore = messages.size >= 50
                            )
                        }
                        // Mark the latest message as read
                        if (sorted.isNotEmpty()) {
                            markAsRead(sorted.last().id)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    fun loadOlderMessages() {
        if (!_state.value.canLoadMore || _state.value.isLoading) return
        val oldestId = _state.value.messages.firstOrNull()?.id ?: return

        viewModelScope.launch {
            chatRepository.getMessages(deliveryId, beforeId = oldestId, limit = 50).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Don't show full loading for pagination
                    }
                    is Resource.Success -> {
                        val older = (result.data ?: emptyList()).sortedBy { it.id }
                        messageIds.addAll(older.map { it.id })
                        _state.update {
                            it.copy(
                                messages = older + it.messages,
                                canLoadMore = older.size >= 50
                            )
                        }
                    }
                    is Resource.Error -> {
                        // Silent fail for pagination
                    }
                }
            }
        }
    }

    private fun connectWebSocket() {
        viewModelScope.launch {
            chatWebSocketClient.connect(deliveryId)
        }
    }

    private fun observeIncomingMessages() {
        viewModelScope.launch {
            chatWebSocketClient.messages.collect { message ->
                if (message.deliveryId == deliveryId && !messageIds.contains(message.id)) {
                    messageIds.add(message.id)
                    _state.update { it.copy(messages = it.messages + message) }
                    markAsRead(message.id)
                }
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            chatWebSocketClient.connectionState.collect { connectionState ->
                _state.update {
                    it.copy(isConnected = connectionState == ChatWebSocketClient.ConnectionState.CONNECTED)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            chatRepository.sendMessage(deliveryId, content).collect { result ->
                when (result) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _state.update { it.copy(isSending = false) }
                        // Message will arrive via WebSocket broadcast
                        // Add it directly if not yet received via WS
                        result.data?.let { message ->
                            if (!messageIds.contains(message.id)) {
                                messageIds.add(message.id)
                                _state.update { it.copy(messages = it.messages + message) }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(isSending = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    private fun markAsRead(messageId: Int) {
        viewModelScope.launch {
            chatRepository.markRead(deliveryId, messageId).collect { }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        chatWebSocketClient.disconnect()
        super.onCleared()
    }
}
