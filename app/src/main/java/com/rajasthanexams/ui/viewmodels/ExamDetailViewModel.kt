package com.rajasthanexams.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.MockData
import com.rajasthanexams.data.Test
import com.rajasthanexams.data.TestType
import com.rajasthanexams.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ExamDetailUiState {
    object Loading : ExamDetailUiState()
    data class Success(val tests: List<Test>) : ExamDetailUiState()
    data class Error(val message: String) : ExamDetailUiState()
}

class ExamDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ContentRepository()

    private val _uiState = MutableStateFlow<ExamDetailUiState>(ExamDetailUiState.Loading)
    val uiState: StateFlow<ExamDetailUiState> = _uiState

    fun fetchTests(examId: String, type: TestType?) {
        viewModelScope.launch {
            _uiState.value = ExamDetailUiState.Loading
            
            // For now, getTests endpoint returns ALL tests for an exam.
            // We might need to filter by Type purely on frontend if backend doesn't support it yet.
            val result = repository.getTests(examId)
            
            result.onSuccess { apiTests ->
                var uiTests = apiTests.map { apiTest ->
                     Test(
                        id = apiTest.id,
                        title = apiTest.title,
                        category = "Exam", 
                        questions = apiTest.totalQuestions,
                        time = apiTest.durationMinutes,
                        attempts = "New",
                        rating = 5.0,
                        isLive = false,
                        type = try { TestType.valueOf(apiTest.type) } catch(e:Exception) { TestType.MOCK }
                    )
                }
                
                if (type != null) {
                    uiTests = uiTests.filter { it.type == type }
                }

                _uiState.value = ExamDetailUiState.Success(uiTests)
            }.onFailure {
                 // On failure, show Mock Data (demo mode) OR Error
                 // For smooth user exp in this phase, logic:
                 val mockTests = if (type != null) {
                    MockData.popularTests.filter { it.type == type }
                 } else {
                    MockData.popularTests
                 }
                 _uiState.value = ExamDetailUiState.Success(mockTests)
            }
        }
    }
}
