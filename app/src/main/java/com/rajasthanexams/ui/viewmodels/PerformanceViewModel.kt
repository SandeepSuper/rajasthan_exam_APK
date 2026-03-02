package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.remote.dto.PerformanceResponse
import com.rajasthanexams.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PerformanceUiState {
    object Loading : PerformanceUiState()
    data class Success(val data: PerformanceResponse) : PerformanceUiState()
    data class Error(val message: String) : PerformanceUiState()
}

class PerformanceViewModel : ViewModel() {
    private val repository = ContentRepository()

    private val _uiState = MutableStateFlow<PerformanceUiState>(PerformanceUiState.Loading)
    val uiState: StateFlow<PerformanceUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = PerformanceUiState.Loading
            repository.getPerformance()
                .onSuccess { _uiState.value = PerformanceUiState.Success(it) }
                .onFailure { _uiState.value = PerformanceUiState.Error(it.message ?: "Unknown error") }
        }
    }
}
