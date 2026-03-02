package com.rajasthanexams.data.remote

import android.util.Log
import com.google.gson.Gson
import com.rajasthanexams.data.remote.dto.NotificationResponse
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

object NotificationWebSocketClient {
    private var webSocket: WebSocket? = null

    // Use a SEPARATE OkHttpClient for WebSocket so it doesn't share the
    // Retrofit dispatcher/connection pool. This prevents WebSocket from
    // starving HTTP requests (getExams, getTests) of OkHttp threads.
    private val wsClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // No read timeout for WebSocket
            .build()
    }

    private val gson = Gson()

    private val _notifications = MutableSharedFlow<NotificationResponse>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val notifications: SharedFlow<NotificationResponse> = _notifications

    fun connect() {
        if (webSocket != null) return

        val request = Request.Builder()
            .url("ws://13.126.216.84:8080/api/ws/notifications")
            .build()

        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to Notifications Stream")
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
                this@NotificationWebSocketClient.webSocket = null
                // Do NOT auto-reconnect here — reconnection should happen from the ViewModel
                // to avoid rapid retry loops that block the thread pool.
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "WebSocket Closed: $reason")
                this@NotificationWebSocketClient.webSocket = null
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        webSocket = null
    }
}
