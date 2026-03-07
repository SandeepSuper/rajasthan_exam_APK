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

class LeaderboardViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val api = RetrofitClient.getService()
    private val sessionManager = com.rajasthanexams.data.local.SessionManager(application)

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    fun fetchLeaderboard(testId: String?) {
        viewModelScope.launch {
            _uiState.value = LeaderboardUiState.Loading

            val token = sessionManager.getAuthToken()
            if (token == null) {
                _uiState.value = LeaderboardUiState.Error("Please login to view leaderboard")
                return@launch
            }

            // Sync local coins to server first (for global leaderboard)
            if (testId.isNullOrEmpty()) {
                syncCoinsToServer(token)
            }

            try {
                val safeTestId = if (testId.isNullOrEmpty()) null else testId
                val response = api.getLeaderboard("Bearer $token", safeTestId)
                if (response.isSuccessful && response.body() != null) {
                    val entries = response.body()!!
                    val uiRankers = entries.map { entry ->
                        Ranker(
                            userId = entry.userId,
                            name = entry.name,
                            score = entry.score.toInt(),
                            totalMarks = entry.totalMarks.toInt(),
                            exam = entry.exam ?: "Rank #${entry.rank}",
                            coins = entry.coins,
                            avatarUrl = entry.avatarUrl ?: "",
                            rank = entry.rank
                        )
                    }
                    _uiState.value = LeaderboardUiState.Success(uiRankers)
                } else {
                    _uiState.value = LeaderboardUiState.Error("Failed to load leaderboard (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = LeaderboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun syncCoinsToServer(token: String) {
        try {
            val localCoins = sessionManager.getCoins()
            if (localCoins > 0) {
                api.syncCoins("Bearer $token", mapOf("coins" to localCoins))
            }
        } catch (e: Exception) {
            println("Coin sync failed: ${e.message}")
        }
    }
}
