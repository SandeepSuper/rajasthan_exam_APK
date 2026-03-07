package com.rajasthanexams.data.remote

import android.util.Log
import com.google.gson.Gson
import com.rajasthanexams.data.remote.dto.NotificationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object NotificationWebSocketClient {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private val isConnecting = AtomicBoolean(false)
    private var shouldReconnect = true

    private val wsClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(20, TimeUnit.SECONDS) // Send pings to keep connection alive
            .build()
    }

    private val gson = Gson()

    private val _notifications = MutableSharedFlow<NotificationResponse>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val notifications: SharedFlow<NotificationResponse> = _notifications

    /** True when the WebSocket is successfully connected. */
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    /** Call once at app startup. Handles connect + auto-reconnect. */
    fun connect() {
        if (!isConnecting.compareAndSet(false, true)) return
        shouldReconnect = true
        scope.launch { connectWithRetry() }
    }

    private suspend fun connectWithRetry() {
        var delayMs = 1_000L
        while (shouldReconnect) {
            if (webSocket == null) {
                openSocket()
            }
            // Wait and check if still disconnected
            delay(delayMs)
            if (_isConnected.value) {
                // Connected — reset backoff and just wait
                delayMs = 1_000L
                isConnecting.set(false)
                // Park here until disconnected
                while (shouldReconnect && _isConnected.value) {
                    delay(5_000L)
                }
            } else {
                // Still not connected — exponential backoff up to 30s
                delayMs = minOf(delayMs * 2, 30_000L)
                Log.d("WebSocket", "Reconnecting in ${delayMs / 1000}s...")
            }
        }
        isConnecting.set(false)
    }

    private fun openSocket() {
        val request = Request.Builder()
            .url("wss://exam.photopassport.in/api/ws/notifications")
            .build()

        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to Notifications Stream")
                _isConnected.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val notification = gson.fromJson(text, NotificationResponse::class.java)
                    _notifications.tryEmit(notification)
                } catch (e: Exception) {
                    Log.e("WebSocket", "Failed to parse notification: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "WebSocket Failure: ${t.message}")
                _isConnected.value = false
                this@NotificationWebSocketClient.webSocket = null
                // connectWithRetry() loop will handle reconnection
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "WebSocket Closed: $reason")
                _isConnected.value = false
                this@NotificationWebSocketClient.webSocket = null
            }
        })
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "App closed")
        webSocket = null
        _isConnected.value = false
    }
}
