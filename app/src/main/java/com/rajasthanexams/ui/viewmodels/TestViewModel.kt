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
    data class Success(val questions: List<Question>, val test: com.rajasthanexams.data.Test? = null) : TestUiState()
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

            // 1. Check Offline Storage
            val offlineQuestions = com.rajasthanexams.data.OfflineManager.getStoredQuestions(testId)
            
            // Try determine Test Details from Cache or MockData or Downloads
            var cachedTest: com.rajasthanexams.data.Test? = null
             
            if (offlineQuestions != null && offlineQuestions.isNotEmpty()) {
                 // Try Cache First
                 val cachedMeta = com.rajasthanexams.data.OfflineManager.getTestMetadata(testId, com.rajasthanexams.data.remote.dto.TestResponse::class.java) as? com.rajasthanexams.data.remote.dto.TestResponse
                 if (cachedMeta != null) {
                     cachedTest = com.rajasthanexams.data.Test(
                        id = cachedMeta.id,
                        title = cachedMeta.title,
                        category = "Exam",
                        questions = cachedMeta.totalQuestions,
                        time = cachedMeta.durationMinutes,
                        attempts = "",
                        rating = 0.0,
                        isLive = cachedMeta.isLive ?: false,
                        startsAt = cachedMeta.startsAt,
                        endsAt = cachedMeta.endsAt,
                        allowPrevious = cachedMeta.allowPrevious,
                        allowSolution = cachedMeta.allowSolution ?: true,
                        sectionLock = cachedMeta.sectionLock,
                        showResultImmediately = cachedMeta.showResultImmediately,
                        type = try { com.rajasthanexams.data.TestType.valueOf(cachedMeta.type.uppercase()) } catch(e:Exception) { com.rajasthanexams.data.TestType.MOCK },
                        isAttempted = cachedMeta.isAttempted
                     )
                 }
                 
                 // Fallback to Downloaded Metadata
                 if (cachedTest == null) {
                     val downloaded = com.rajasthanexams.data.OfflineManager.getDownloadedTests().find { it.id == testId }
                     if (downloaded != null) {
                         cachedTest = com.rajasthanexams.data.Test(
                             id = downloaded.id,
                             title = downloaded.title,
                             category = "Exam",
                             questions = downloaded.questions,
                             time = downloaded.time,
                             attempts = "",
                             rating = 0.0,
                             isLive = false, // Downloaded implies offline/mock usually, but check type
                             startsAt = null,
                             endsAt = null,
                             allowPrevious = true,
                             allowSolution = true,
                             sectionLock = false,
                             showResultImmediately = true,
                             type = try { com.rajasthanexams.data.TestType.valueOf(downloaded.type.uppercase()) } catch(e:Exception) { com.rajasthanexams.data.TestType.MOCK },
                             isAttempted = false
                         )
                         // Specific check: if type is LIVE, mark isLive=true? 
                         if (cachedTest!!.type == com.rajasthanexams.data.TestType.LIVE) {
                             cachedTest = cachedTest!!.copy(isLive = true)
                         }
                     }
                 }

                 // Fallback to MockData
                 if (cachedTest == null) {
                    cachedTest = com.rajasthanexams.data.MockData.popularTests.find { it.id == testId } 
                        ?: com.rajasthanexams.data.MockData.demoLiveTests.find { it.id == testId }
                 }
                 
                 // Override isAttempted from Local Status
                 if (cachedTest != null) {
                     val localAttempt = com.rajasthanexams.data.OfflineManager.isTestAttempted(testId)
                     val isRetake = com.rajasthanexams.data.OfflineManager.isRetakeMode(testId)
                     
                     if (isRetake) {
                         cachedTest = cachedTest!!.copy(isAttempted = false)
                     } else if (localAttempt) {
                         cachedTest = cachedTest!!.copy(isAttempted = true)
                     }
                 }
                 
                 // CRITICAL: Only return early if we actually have TEST METADATA.
                 // Otherwise, proceed to API to try and fetch details (even if questions exist).
                 if (cachedTest != null) {
                     _uiState.value = TestUiState.Success(offlineQuestions, cachedTest)
                     return@launch
                 }
            }

            // 2. Fetch from API
            val questionsResult = repository.getQuestions(testId)
            val testDetailsResult = repository.getTestDetails(testId)
            
            questionsResult.onSuccess { apiQuestions ->
                val uiQuestions = apiQuestions.map { q ->
                    Question(
                        id = q.id,
                        questionEn = q.textEn,
                        questionHi = q.textHi,
                        optionsEn = q.optionsEn,
                        optionsHi = q.optionsHi,
                        correctOptionIndex = q.correctOptionIndex,
                        solutionEn = q.solutionEn ?: "",
                        solutionHi = q.solutionHi ?: "",
                        marksPerQuestion = q.marksPerQuestion,
                        negativeMarks = q.negativeMarks,
                        subject = q.subject
                    )
                }
                
                // Cache Questions
                com.rajasthanexams.data.OfflineManager.saveQuestions(testId, uiQuestions)
                
                var uiTest: com.rajasthanexams.data.Test? = null
                testDetailsResult.onSuccess { details ->
                    // Cache Metadata
                    com.rajasthanexams.data.OfflineManager.saveTestMetadata(testId, details)
                    
                    uiTest = com.rajasthanexams.data.Test(
                        id = details.id,
                        title = details.title,
                        category = "Exam",
                        questions = details.totalQuestions,
                        time = details.durationMinutes,
                        attempts = "",
                        rating = 0.0,
                        isLive = details.isLive ?: false,
                        startsAt = details.startsAt,
                        endsAt = details.endsAt,
                        allowPrevious = details.allowPrevious,
                        allowSolution = details.allowSolution ?: true,
                        sectionLock = details.sectionLock,
                        showResultImmediately = details.showResultImmediately,
                        type = try { com.rajasthanexams.data.TestType.valueOf(details.type.uppercase()) } catch(e:Exception) { com.rajasthanexams.data.TestType.MOCK },
                        isAttempted = details.isAttempted
                    )
                }

                // Override Attempted if API says false but local says true?
                if (uiTest != null) {
                     if (com.rajasthanexams.data.OfflineManager.isRetakeMode(testId)) {
                         uiTest = uiTest!!.copy(isAttempted = false)
                     } else if (com.rajasthanexams.data.OfflineManager.isTestAttempted(testId)) {
                         uiTest = uiTest!!.copy(isAttempted = true)
                     }
                }

                _uiState.value = TestUiState.Success(uiQuestions, uiTest)
            }.onFailure {
                // Determine if we should fallback by ID or just show error
                // Only fallback if it's explicitly a mock ID (e.g. 101, 102)
                if (testId.length < 5) { // Assuming Mock IDs are short
                     val mockTest = com.rajasthanexams.data.MockData.popularTests.find { it.id == testId }
                        ?: com.rajasthanexams.data.MockData.demoLiveTests.find { it.id == testId }
                     
                     // Check Local Attempt
                     var finalTest = mockTest
                     if (finalTest != null && com.rajasthanexams.data.OfflineManager.isTestAttempted(testId)) {
                         finalTest = finalTest.copy(isAttempted = true)
                     }
                        
                     _uiState.value = TestUiState.Success(com.rajasthanexams.data.MockData.sampleQuestions, finalTest)
                } else {
                     // FINAL FALLBACK: If we have stored questions (even without metadata), show them?
                     // If we are here, 'cachedTest' was null, but 'offlineQuestions' might not be null if we skipped early return.
                     if (offlineQuestions != null && offlineQuestions.isNotEmpty()) {
                         _uiState.value = TestUiState.Success(offlineQuestions, null) // Better than Error
                     } else {
                         _uiState.value = TestUiState.Error("Failed to load questions: ${it.message}")
                     }
                }
            }
        }
    }
    
    fun submitTest(
        testId: String, 
        userAnswers: Map<String, Int>, 
        timeTaken: Int,
        onResult: (score: Double, total: Int, solution: Map<String, String>, coinsEarned: Int) -> Unit
    ) {
        viewModelScope.launch {
            // Mark as Attempted Locally
            com.rajasthanexams.data.OfflineManager.markTestAttempted(testId)
            // Save User Answers Locally
            com.rajasthanexams.data.OfflineManager.saveUserAnswers(testId, userAnswers)
            
            val token = sessionManager.getAuthToken()
            var isOnlineSuccess = false
            
            if (token != null) {
                try {
                    val response = api.submitTest(
                        "Bearer $token",
                        SubmitTestRequest(testId, userAnswers, timeTaken)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        // Update Coins
                        sessionManager.updateCoins(result.newTotalCoins)
                        
                        onResult(result.score, result.totalQuestions, result.solutions, result.coinsEarned)
                        isOnlineSuccess = true
                    }
                } catch (e: Exception) {
                    // Network error, fall back to offline
                }
            } 
            
            if (!isOnlineSuccess) {
                // calculateLocalResult(testId, userAnswers, onResult) // Old
                // New: loadLocalResult to recalculate
                loadLocalResult(testId) { score, total, questions, _, _, _, coinsEarned ->
                     val solutions = questions.associate { q -> 
                         q.id to (if (q.solutionEn.isNotEmpty()) q.solutionEn else q.solutionHi) 
                     }
                     // Update Local Coins immediately for feedback
                     val currentCoins = sessionManager.getCoins()
                     sessionManager.updateCoins(currentCoins + coinsEarned)
                     
                     onResult(score, total, solutions, coinsEarned)
                }
            }
        }
    }

    fun loadLocalResult(
        testId: String,
        onResult: (score: Double, total: Int, questions: List<Question>, userAnswers: Map<Int, Int>, timeTaken: Long, timePerQuestion: Map<Int, Long>, coinsEarned: Int) -> Unit
    ) {
        viewModelScope.launch {
            val questions = com.rajasthanexams.data.OfflineManager.getStoredQuestions(testId) ?: emptyList()
            if (questions.isEmpty()) return@launch

            val userAnswersMap = com.rajasthanexams.data.OfflineManager.getUserAnswers(testId)
            // Convert Map<String, Int> to Map<Int, Int>
            val userAnswers = mutableMapOf<Int, Int>()
            userAnswersMap.forEach { (qId, optionIndex) ->
                val index = questions.indexOfFirst { it.id == qId }
                if (index != -1) userAnswers[index] = optionIndex
            }

            val timePerQuestionMap = com.rajasthanexams.data.OfflineManager.getTimeSpent(testId)
            val timePerQuestion = mutableMapOf<Int, Long>()
            timePerQuestionMap.forEach { (qId, time) ->
                val index = questions.indexOfFirst { it.id == qId }
                if (index != -1) timePerQuestion[index] = time
            }
            
            // Assume total time or stored? 
            // We don't strictly store "Time Taken" separately in offline manager except in progress? 
            // But we do track total duration in test.
            // Let's use sum of timePerQuestion as approx or 0 if not needed? 
            // In submitTest we pass timeTaken.
            // Let's retrieve from Metadata if possible or calculate.
            // For now, let's sum up timePerQuestion
            val timeTaken = timePerQuestion.values.sum()

            // Calculate Score
            // Fetch Negative Marks & Marks Per Question
            val offlineTest = com.rajasthanexams.data.OfflineManager.getDownloadedTests().find { it.id == testId }
            // Try to find metadata from cache too
             val cachedMeta = com.rajasthanexams.data.OfflineManager.getTestMetadata(testId, com.rajasthanexams.data.remote.dto.TestResponse::class.java) as? com.rajasthanexams.data.remote.dto.TestResponse

            val negativeMarks = offlineTest?.negativeMarks ?: cachedMeta?.negativeMarks ?: 0.0
            val marksPerQuestion = offlineTest?.marksPerQuestion ?: cachedMeta?.marksPerQuestion ?: 1.0
            
            var score = 0.0
            
            questions.forEachIndexed { index, q ->
                val correct = q.correctOptionIndex
                val userAns = userAnswers[index]
                
                val qMarks = q.marksPerQuestion ?: marksPerQuestion
                val qNegative = q.negativeMarks ?: negativeMarks

                if (userAns != null) {
                    if (userAns == correct) {
                         score += qMarks
                    } else {
                         score -= qNegative
                    }
                }
            }
            
            // Calculate Coins
            val participationCoins = 10
            val performanceCoins = if (score > 0) score.toInt() else 0
            
            // Accuracy Logic
            var correctCount = 0
            var attemptedCount = 0
            questions.forEachIndexed { index, q ->
                 val userAns = userAnswers[index]
                 if (userAns != null) {
                     attemptedCount++
                     if (userAns == q.correctOptionIndex) correctCount++
                 }
            }
            val realAccuracy = if (attemptedCount > 0) (correctCount.toDouble() / attemptedCount) * 100 else 0.0
            
            val bonusCoins = if (realAccuracy > 80.0) 20 else 0
            val coinsEarned = participationCoins + performanceCoins + bonusCoins

            onResult(score, questions.size, questions, userAnswers, timeTaken, timePerQuestion, coinsEarned)
        }
    }

    fun reportQuestion(
        testId: String,
        questionId: String,
        bugType: String,
        description: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val token = sessionManager.getAuthToken()
            
            if (token != null) {
                try {
                    val response = api.createReport(
                        "Bearer $token",
                        com.rajasthanexams.data.remote.dto.CreateReportRequest(
                            testId = testId,
                            questionId = questionId,
                            bugType = bugType,
                            description = description
                        )
                    )
                    onResult(response.isSuccessful)
                } catch (e: Exception) {
                    onResult(false)
                }
            } else {
                onResult(false) // Authentication required
            }
        }
    }


}
