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
import kotlinx.coroutines.async

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
            
            // Fetch both Exams (to check purchase status) and Tests
            // Since there is no getExam(id) endpoint, we fetch all exams and find the one we need.
            val examsDeferred = async { repository.getExams() }
            val testsDeferred = async { repository.getTests(examId) }

            val examsResult = examsDeferred.await()
            val testsResult = testsDeferred.await()
            
            var isExamPurchased = false
            examsResult.onSuccess { exams ->
                isExamPurchased = exams.find { it.id == examId }?.isPurchased == true
            }
            
            testsResult.onSuccess { apiTests ->
                var uiTests = apiTests.map { apiTest ->
                     Test(
                        id = apiTest.id,
                        title = apiTest.title,
                        category = "Exam", 
                        questions = apiTest.totalQuestions,
                        time = apiTest.durationMinutes,
                        attempts = "New",
                        rating = 5.0,
                        isLive = apiTest.isLive,
                        startsAt = apiTest.startsAt,
                        endsAt = apiTest.endsAt,
                        allowPrevious = apiTest.allowPrevious,
                        sectionLock = apiTest.sectionLock,
                        type = try { TestType.valueOf(apiTest.type) } catch(e:Exception) { TestType.MOCK },
                        negativeMarks = apiTest.negativeMarks,
                        totalMarks = apiTest.totalMarks,
                        isPremium = apiTest.isPremium,
                        isAttempted = apiTest.isAttempted,
                        examId = examId,
                        isPurchased = isExamPurchased // Inherit from Exam
                    )
                }
                
                if (type != null) {
                    uiTests = uiTests.filter { test ->
                         if (type == TestType.LIVE) {
                             test.isLive
                         } else {
                             test.type == type && !test.isLive
                         }
                    }
                } else {
                    // Hide live tests by default if no criteria provided
                    uiTests = uiTests.filter { !it.isLive }
                }

                _uiState.value = ExamDetailUiState.Success(uiTests.sortedBy { it.isPremium })
            }.onFailure {
                 // On failure, show Mock Data (demo mode) OR Error
                 // For smooth user exp in this phase, logic:
                 val mockTests = if (type != null) {
                    MockData.popularTests.filter { test ->
                        if (type == TestType.LIVE) {
                            test.isLive
                        } else {
                            test.type == type && !test.isLive
                        }
                    }
                 } else {
                    MockData.popularTests.filter { !it.isLive }
                 }
                 _uiState.value = ExamDetailUiState.Success(mockTests)
            }
        }
    }

    fun downloadTestContent(
        testId: String, 
        title: String, 
        category: String,
        negativeMarks: Double,
        marksPerQuestion: Double,
        questionsCount: Int,
        durationMinutes: Int,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Fetch questions from API
            val result = repository.getQuestions(testId)
            
            result.onSuccess { apiQuestions ->
                 // 2. Convert to UI Model (Offline Storage Model)
                 val questions = apiQuestions.map { q ->
                     com.rajasthanexams.data.Question(
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
                 
                 // 3. Save Questions
                 com.rajasthanexams.data.OfflineManager.saveQuestions(testId, questions)
                 
                 // 4. Mark as Downloaded
                 com.rajasthanexams.data.OfflineManager.downloadTest(testId, title, category, negativeMarks, marksPerQuestion, questionsCount, durationMinutes)
                 
                 onComplete(true)
            }.onFailure {
                 // If mock ID, save mock data
                 if (testId.length < 5) {
                      com.rajasthanexams.data.OfflineManager.saveQuestions(testId, MockData.sampleQuestions)
                      com.rajasthanexams.data.OfflineManager.downloadTest(testId, title, category, 0.0, 1.0, 10, 15)
                      onComplete(true)
                 } else {
                      onComplete(false)
                 }
            }
        }
    }
}
