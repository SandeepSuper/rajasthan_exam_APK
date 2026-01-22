package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.MockData
import com.rajasthanexams.data.Test
import com.rajasthanexams.data.TestType
import com.rajasthanexams.data.remote.dto.TestResponse
import com.rajasthanexams.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.rajasthanexams.data.Category
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val tests: List<Test>,
        val categories: List<Category>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val repository = ContentRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    // Map Backend TestResponse to Frontend Test Model
    fun fetchTests(examId: String? = null) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // 1. Fetch Categories (Exams)
            var currentCategories = emptyList<Category>()
            var targetExamId = examId ?: "249d6387-f822-4414-b44c-9799298bc79d"

            val examsResult = repository.getExams()
            if (examsResult.isSuccess) {
                 val exams = examsResult.getOrNull() ?: emptyList()
                 if (exams.isNotEmpty()) {
                     targetExamId = examId ?: exams[0].id
                     currentCategories = exams.map { exam ->
                         Category(
                             id = exam.id,
                             title = exam.title,
                             icon = Icons.Default.School, // Default icon for dynamic cats
                             testsAvailable = 0 // We don't have this count from backend yet
                         )
                     }
                 }
            }
            
            // 2. Fetch Tests for the Target Exam
            val result = repository.getTests(targetExamId)
            
            result.onSuccess { apiTests ->
                val uiTests = apiTests.map { apiTest ->
                    Test(
                        id = apiTest.id,
                        title = apiTest.title,
                        category = "Exam", // Could map from Exam Title if passed
                        questions = apiTest.totalQuestions,
                        time = apiTest.durationMinutes,
                        attempts = "New",
                        rating = 5.0,
                        isLive = false,
                        type = try { TestType.valueOf(apiTest.type) } catch(e:Exception) { TestType.MOCK }
                    )
                }
                 _uiState.value = HomeUiState.Success(uiTests, currentCategories.ifEmpty { MockData.categories })
            }.onFailure {
                 // Fallback to Mock Data if API fails
                 _uiState.value = HomeUiState.Success(MockData.popularTests, MockData.categories)
            }
        }
    }

    init {
        // Auto fetch on init
        fetchTests()
    }
}
