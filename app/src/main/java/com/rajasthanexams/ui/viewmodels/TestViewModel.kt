package com.rajasthanexams.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.Question
import com.rajasthanexams.data.local.SessionManager
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.dto.SubmitTestRequest
import com.rajasthanexams.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TestUiState {
    object Loading : TestUiState()
    data class Success(val questions: List<Question>) : TestUiState()
    data class Error(val message: String) : TestUiState()
}

class TestViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ContentRepository()
    private val sessionManager = SessionManager(application)
    private val api = RetrofitClient.getService() // Direct API for submit for now

    private val _uiState = MutableStateFlow<TestUiState>(TestUiState.Loading)
    val uiState: StateFlow<TestUiState> = _uiState

    // Map Backend QuestionResponse to Frontend Question Model
    fun fetchQuestions(testId: String) {
        viewModelScope.launch {
            _uiState.value = TestUiState.Loading
            val result = repository.getQuestions(testId)
            
            result.onSuccess { apiQuestions ->
                val uiQuestions = apiQuestions.map { q ->
                    Question(
                        id = q.id,
                        questionEn = q.textEn,
                        questionHi = q.textHi,
                        optionsEn = q.optionsEn,
                        optionsHi = q.optionsHi,
                        correctOptionIndex = q.correctOptionIndex,
                        solutionEn = q.solutionEn ?: "",
                        solutionHi = q.solutionHi ?: ""
                    )
                }
                _uiState.value = TestUiState.Success(uiQuestions)
            }.onFailure {
                // Determine if we should fallback by ID or just show error
                // For demo, if ID is 'mock', use mock data
                if (testId.startsWith("1")) { // Mock IDs in MockData start with 100 series
                     _uiState.value = TestUiState.Success(com.rajasthanexams.data.MockData.sampleQuestions)
                } else {
                     _uiState.value = TestUiState.Error("Failed to load questions: ${it.message}")
                }
            }
        }
    }
    
    fun submitTest(
        testId: String, 
        userAnswers: Map<String, Int>, 
        timeTaken: Int,
        onResult: (score: Int, total: Int, solution: Map<String, String>) -> Unit
    ) {
        viewModelScope.launch {
            val token = sessionManager.getAuthToken()
            if (token != null) {
                try {
                    val response = api.submitTest(
                        "Bearer $token",
                        SubmitTestRequest(userAnswers, timeTaken)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        onResult(result.score, result.totalQuestions, result.solutions)
                    } else {
                        // Handle error (offline calc?)
                    }
                } catch (e: Exception) {
                    // Network error
                }
            } else {
                // Offline mode calculation possible here if questions had solutions
            }
        }
    }
}
