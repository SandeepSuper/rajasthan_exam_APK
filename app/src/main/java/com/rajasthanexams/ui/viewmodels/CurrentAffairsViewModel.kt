package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.dto.NewsItemResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CurrentAffairsUiState {
    object Loading : CurrentAffairsUiState()
    data class Success(val news: List<NewsItemResponse>) : CurrentAffairsUiState()
    data class Error(val message: String) : CurrentAffairsUiState()
}

class CurrentAffairsViewModel : ViewModel() {
    private val api = RetrofitClient.getService()

    private val _uiState = MutableStateFlow<CurrentAffairsUiState>(CurrentAffairsUiState.Loading)
    val uiState: StateFlow<CurrentAffairsUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CurrentAffairsUiState.Loading
            try {
                val response = api.getNews()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = CurrentAffairsUiState.Success(response.body()!!)
                } else {
                    _uiState.value = CurrentAffairsUiState.Error("Failed (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = CurrentAffairsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
