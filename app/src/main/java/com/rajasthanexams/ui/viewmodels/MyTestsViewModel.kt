package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.Test
import com.rajasthanexams.data.TestType
import com.rajasthanexams.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

data class PurchasedExam(
    val id: String,
    val title: String,
    val iconUrl: String?,
    val testCount: Int
)

sealed class MyTestsUiState {
    object Loading : MyTestsUiState()
    data class Success(
        val exams: List<PurchasedExam>,
        val examTests: Map<String, List<Test>> // Key = examId
    ) : MyTestsUiState()
    data class Error(val message: String) : MyTestsUiState()
}

class MyTestsViewModel : ViewModel() {
    private val repository = ContentRepository()

    private val _uiState = MutableStateFlow<MyTestsUiState>(MyTestsUiState.Loading)
    val uiState: StateFlow<MyTestsUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MyTestsUiState.Loading

            // 1. Fetch all exams
            val examsResult = repository.getExams()
            examsResult.onFailure {
                _uiState.value = MyTestsUiState.Error("Failed to load exams: ${it.message}")
                return@launch
            }

            val allExams = examsResult.getOrNull() ?: emptyList()

            // 2. Filter only purchased exams
            val purchasedExams = allExams.filter { it.isPurchased }

            if (purchasedExams.isEmpty()) {
                _uiState.value = MyTestsUiState.Success(emptyList(), emptyMap())
                return@launch
            }

            // 3. Fetch tests for each purchased exam concurrently
            val examTestsMap = mutableMapOf<String, List<Test>>()

            val jobs = purchasedExams.map { exam ->
                async {
                    val testsResult = repository.getTests(exam.id)
                    testsResult.onSuccess { apiTests ->
                        val tests = apiTests
                            .filter { !it.isLive }  // exclude live tests
                            .map { t ->
                                Test(
                                    id = t.id,
                                    title = t.title,
                                    category = exam.title,
                                    questions = t.totalQuestions,
                                    time = t.durationMinutes,
                                    attempts = "",
                                    rating = 0.0,
                                    isPremium = t.isPremium,
                                    isLive = t.isLive ?: false,
                                    startsAt = t.startsAt,
                                    endsAt = t.endsAt,
                                    allowPrevious = t.allowPrevious,
                                    allowSolution = t.allowSolution ?: true,
                                    sectionLock = t.sectionLock,
                                    showResultImmediately = t.showResultImmediately,
                                    type = try { TestType.valueOf(t.type.uppercase()) } catch (e: Exception) { TestType.MOCK },
                                    negativeMarks = t.negativeMarks,
                                    totalMarks = t.totalMarks,
                                    isAttempted = t.isAttempted ||
                                        com.rajasthanexams.data.OfflineManager.isTestAttempted(t.id),
                                    isDownloaded = com.rajasthanexams.data.OfflineManager.isTestDownloaded(t.id),
                                    examId = exam.id,
                                    isPurchased = true,
                                    price = exam.price
                                )
                            }
                        synchronized(examTestsMap) {
                            examTestsMap[exam.id] = tests
                        }
                    }
                }
            }

            jobs.forEach { it.await() }

            val purchasedExamList = purchasedExams.map { exam ->
                PurchasedExam(
                    id = exam.id,
                    title = exam.title,
                    iconUrl = exam.iconUrl,
                    testCount = examTestsMap[exam.id]?.size ?: 0
                )
            }.filter { (examTestsMap[it.id]?.size ?: 0) > 0 }

            _uiState.value = MyTestsUiState.Success(purchasedExamList, examTestsMap.toMap())
        }
    }
}
