package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.ui.screens.Ranker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LeaderboardUiState {
    object Loading : LeaderboardUiState()
    data class Success(val rankers: List<Ranker>) : LeaderboardUiState()
    data class Error(val message: String) : LeaderboardUiState()
}

class LeaderboardViewModel : ViewModel() {
    private val api = RetrofitClient.getService()

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    fun fetchLeaderboard(examId: String = "249d6387-f822-4414-b44c-9799298bc79d") {
        viewModelScope.launch {
            _uiState.value = LeaderboardUiState.Loading
            try {
                // Using Retrofit for generic call or ContentRepository if strictly layered
                // For simplicity/speed in this phase, calling API directly or via repo wrapper
                val response = api.getLeaderboard(examId)
                if (response.isSuccessful && response.body() != null) {
                    val entries = response.body()!!
                    val uiRankers = entries.mapIndexed { index, entry ->
                        Ranker(
                            name = entry.name,
                            score = 1000 - (index * 50), // Mock score logic as API only returns names currently ordered
                            exam = "Patwari", // Mock exam name
                            coins = 500 - (index * 10),
                            avatarUrl = ""
                        )
                    }
                    _uiState.value = LeaderboardUiState.Success(uiRankers)
                } else {
                    _uiState.value = LeaderboardUiState.Error("Failed to load leaderboard")
                }
            } catch (e: Exception) {
                 _uiState.value = LeaderboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
