package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.NotificationWebSocketClient
import com.rajasthanexams.data.remote.dto.NotificationResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Success(val notifications: List<NotificationResponse>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

class NotificationsViewModel : ViewModel() {
    private val api = RetrofitClient.getService()

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState

    init {
        load()

        // 1. WebSocket — receive new notifications instantly when WS is connected
        viewModelScope.launch {
            NotificationWebSocketClient.notifications.collect { newNotification ->
                val currentState = _uiState.value
                if (currentState is NotificationsUiState.Success) {
                    val alreadyExists = currentState.notifications.any { it.id == newNotification.id }
                    if (!alreadyExists) {
                        val mutableList = currentState.notifications.toMutableList()
                        mutableList.add(0, newNotification)
                        _uiState.value = NotificationsUiState.Success(mutableList)
                    }
                } else {
                    _uiState.value = NotificationsUiState.Success(listOf(newNotification))
                }
            }
        }

        // 2. Polling fallback — poll every 5s when WebSocket is NOT connected.
        // This ensures notifications appear even if WSS/Nginx is misconfigured.
        viewModelScope.launch {
            while (true) {
                delay(5_000L)
                val wsConnected = NotificationWebSocketClient.isConnected.value
                if (!wsConnected) {
                    // WS is down — silently refresh from REST so user still gets updates
                    silentRefresh()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Do NOT disconnect — WebSocket singleton stays alive while the app runs
    }

    /** Call when the Notifications screen is opened — clears the bell badge. */
    fun markAllRead() {
        val current = _uiState.value
        if (current is NotificationsUiState.Success) {
            val readList = current.notifications.map { it.copy(isRead = true) }
            _uiState.value = NotificationsUiState.Success(readList)
        }
        // Persist to backend so read state survives app restarts
        viewModelScope.launch {
            try {
                api.markAllNotificationsRead()
            } catch (_: Exception) {
                // Best-effort — UI is already updated in-memory
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            if (_uiState.value !is NotificationsUiState.Success) {
                _uiState.value = NotificationsUiState.Loading
            }
            try {
                val response = api.getNotifications()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = NotificationsUiState.Success(response.body()!!)
                } else {
                    if (_uiState.value !is NotificationsUiState.Success) {
                        _uiState.value = NotificationsUiState.Error("Failed (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                if (_uiState.value !is NotificationsUiState.Success) {
                    _uiState.value = NotificationsUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /** Silently fetch from REST and merge new items without showing loading state. */
    private suspend fun silentRefresh() {
        try {
            val response = api.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                val serverList = response.body()!!
                val currentState = _uiState.value
                if (currentState is NotificationsUiState.Success) {
                    val currentIds = currentState.notifications.map { it.id }.toSet()
                    val newItems = serverList.filter { it.id !in currentIds }
                    if (newItems.isNotEmpty()) {
                        // Prepend any new items that arrived since last poll
                        _uiState.value = NotificationsUiState.Success(newItems + currentState.notifications)
                    }
                } else {
                    _uiState.value = NotificationsUiState.Success(serverList)
                }
            }
        } catch (_: Exception) {
            // Ignore silently — polling is best-effort
        }
    }
}

