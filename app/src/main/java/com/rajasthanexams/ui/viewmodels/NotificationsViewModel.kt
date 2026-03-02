package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.NotificationWebSocketClient
import com.rajasthanexams.data.remote.dto.NotificationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        NotificationWebSocketClient.connect()
        viewModelScope.launch {
            NotificationWebSocketClient.notifications.collect { newNotification ->
                val currentState = _uiState.value
                if (currentState is NotificationsUiState.Success) {
                    val mutableList = currentState.notifications.toMutableList()
                    // Prepend the new notification to the top
                    mutableList.add(0, newNotification)
                    _uiState.value = NotificationsUiState.Success(mutableList)
                } else {
                    _uiState.value = NotificationsUiState.Success(listOf(newNotification))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Note: We intentionally do NOT call disconnect() here.
        // NotificationWebSocketClient is a shared singleton; it should remain
        // connected as long as the app is running. Disconnect is only called
        // when the app process exits or the user logs out.
    }

    /** Call when the Notifications screen is opened — clears the bell badge. */
    fun markAllRead() {
        val current = _uiState.value
        if (current is NotificationsUiState.Success) {
            val readList = current.notifications.map { it.copy(isRead = true) }
            _uiState.value = NotificationsUiState.Success(readList)
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
                    _uiState.value = NotificationsUiState.Error("Failed (${response.code()})")
                }
            } catch (e: Exception) {
                if (_uiState.value !is NotificationsUiState.Success) {
                    _uiState.value = NotificationsUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
}
