package com.swiftflow.data.remote

import com.google.gson.Gson
import com.swiftflow.BuildConfig
import com.swiftflow.domain.model.ChatMessage
import com.swiftflow.utils.TokenManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenManager: TokenManager,
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null

    private val _messages = MutableSharedFlow<ChatMessage>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<ChatMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    enum class ConnectionState { CONNECTING, CONNECTED, DISCONNECTED, ERROR }

    suspend fun connect(deliveryId: Int) {
        disconnect()

        val token = tokenManager.getToken().first() ?: return

        val baseUrl = BuildConfig.API_BASE_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .replace("/api/", "")
        val wsUrl = "${baseUrl}/api/ws/deliveries/$deliveryId/chat?token=$token"

        val request = Request.Builder().url(wsUrl).build()
        _connectionState.value = ConnectionState.CONNECTING

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, ChatMessage::class.java)
                    _messages.tryEmit(message)
                } catch (e: Exception) {
                    // Ignore malformed messages (e.g. error objects)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.ERROR
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User left")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
