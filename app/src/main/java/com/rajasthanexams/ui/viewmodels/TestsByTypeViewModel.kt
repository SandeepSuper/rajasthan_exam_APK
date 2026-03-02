package com.rajasthanexams.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.Icons
import com.rajasthanexams.data.Category
import com.rajasthanexams.data.Test
import com.rajasthanexams.data.TestType
import com.rajasthanexams.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TestsByTypeUiState {
    object Loading : TestsByTypeUiState()
    data class Success(
        val categories: List<Category>,
        val selectedCategoryIndex: Int,
        val tests: Map<String, List<Test>> // Key is Category ID
    ) : TestsByTypeUiState()
    data class Error(val message: String) : TestsByTypeUiState()
}

class TestsByTypeViewModel : ViewModel() {
    private val repository = ContentRepository()
    
    private val _uiState = MutableStateFlow<TestsByTypeUiState>(TestsByTypeUiState.Loading)
    val uiState: StateFlow<TestsByTypeUiState> = _uiState
    
    private val _registeredState = MutableStateFlow<Set<String>>(emptySet())
    val registeredState: StateFlow<Set<String>> = _registeredState
    
    fun initRegistration(context: android.content.Context) {
        com.rajasthanexams.data.RegistrationManager.init(context)
        refreshRegistrations()
    }

    fun registerForTest(testId: String) {
        com.rajasthanexams.data.RegistrationManager.register(testId)
        refreshRegistrations()
    }

    private fun refreshRegistrations() {
        val allEntries = com.rajasthanexams.data.RegistrationManager.getAllRegistered()
        _registeredState.value = allEntries
    }

    // Cache categories to avoid re-fetching
    private var cachedCategories: List<Category> = emptyList()
    // Local cache for tests to progressively update UI state
    private val cachedTests = mutableMapOf<String, List<Test>>()
    private var currentTestType: TestType? = null
    private var lastSelectedCategoryIndex: Int = 0
    
    fun loadData(testType: TestType) {
        viewModelScope.launch {
            // Invalidate cache if TestType changes
            if (currentTestType != testType) {
                cachedCategories = emptyList()
                cachedTests.clear()
                currentTestType = testType
                lastSelectedCategoryIndex = 0 // Reset index on type change
            }

            if (cachedCategories.isNotEmpty()) {
                 // Refresh Attempted/Downloaded Status from Local Storage
                 synchronized(cachedTests) {
                     cachedTests.keys.forEach { catId ->
                         val tests = cachedTests[catId] ?: emptyList()
                         val updatedTests = tests.map { t ->
                             t.copy(
                                 isAttempted = t.isAttempted || com.rajasthanexams.data.OfflineManager.isTestAttempted(t.id),
                                 isDownloaded = com.rajasthanexams.data.OfflineManager.isTestDownloaded(t.id)
                             )
                         }
                         cachedTests[catId] = updatedTests
                     }
                 }
                 _uiState.value = TestsByTypeUiState.Success(cachedCategories, lastSelectedCategoryIndex, cachedTests.toMap())
                 return@launch
            }

            _uiState.value = TestsByTypeUiState.Loading
            
            // 1. Fetch Categories (Exams)
            val categoriesResult = repository.getExams()
            
            categoriesResult.onSuccess { examResponses ->
                val allCategories = examResponses.map { 
                    Category(
                        id = it.id, 
                        title = it.title, 
                        icon = androidx.compose.material.icons.Icons.Filled.List,
                        iconUrl = it.iconUrl,
                        testsAvailable = it.testCount,
                        isPurchased = it.isPurchased,
                        isPremium = it.isPremium,
                        price = it.price
                    ) 
                }
                
                // 2. Pre-fetch tests for ALL categories to filter empty ones
                val validCategories = mutableListOf<Category>()
                
                // Create a list of async jobs to fetch tests concurrently
                val fetchJobs = allCategories.map { category ->
                    launch {
                         repository.getTests(category.id).onSuccess { apiTests ->
                             // Check if this category has ANY test of the requested type
                             val hasContent = apiTests.any { 
                                try {
                                    if (testType == TestType.LIVE) {
                                        it.isLive == true
                                    } else {
                                        val backendTypeString = it.type.uppercase()
                                        TestType.valueOf(backendTypeString) == testType && it.isLive != true
                                    }
                                } catch(e: Exception) { false }
                             }

                             if (hasContent) {
                                 synchronized(validCategories) {
                                     validCategories.add(category)
                                 }
                                 
                                 // Cache the filtered tests since we already fetched them
                                 val filteredTests = apiTests.filter { 
                                     try {
                                        if (testType == TestType.LIVE) {
                                            it.isLive == true
                                        } else {
                                            val backendTypeString = it.type.uppercase()
                                            TestType.valueOf(backendTypeString) == testType && it.isLive != true
                                        }
                                     } catch(e: Exception) { false }
                                 }.map { t ->
                                     Test(
                                        id = t.id,
                                        title = t.title,
                                        category = category.title,
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
                                        type = try { TestType.valueOf(t.type.uppercase()) } catch(e:Exception) { TestType.MOCK },
                                        isAttempted = t.isAttempted || com.rajasthanexams.data.OfflineManager.isTestAttempted(t.id),
                                        isDownloaded = com.rajasthanexams.data.OfflineManager.isTestDownloaded(t.id),
                                        examId = category.id,
                                        isPurchased = category.isPurchased, // Inherit from Exam
                                        price = category.price // Inherit from Exam
                                     )
                                 }.sortedBy { it.isPremium }
                                 synchronized(cachedTests) {
                                     cachedTests[category.id] = filteredTests
                                 }
                             }
                         }
                    }
                }
                
                // Wait for all fetches to complete
                fetchJobs.forEach { it.join() }
                
                // Sort valid categories to maintain original order
                val sortedCategories = allCategories.filter { validCategories.contains(it) }
                
                cachedCategories = sortedCategories
                if (sortedCategories.isNotEmpty()) {
                    // Ensure lastSelectedCategoryIndex is within bounds
                    if (lastSelectedCategoryIndex >= sortedCategories.size) {
                        lastSelectedCategoryIndex = 0
                    }
                    _uiState.value = TestsByTypeUiState.Success(sortedCategories, lastSelectedCategoryIndex, cachedTests.toMap())
                } else {
                    _uiState.value = TestsByTypeUiState.Success(emptyList(), 0, emptyMap())
                }
                
            }.onFailure {
                // Fallback to Mock Data logic
                val mockCategories = com.rajasthanexams.data.MockData.categories
                val filteredMockCategories = mockCategories.filter { category ->
                     val hasContent = com.rajasthanexams.data.MockData.popularTests.any { 
                         it.category.contains(category.title.split(" ")[0], ignoreCase = true) && 
                         ((testType == TestType.LIVE && it.isLive) || (it.type == testType && !it.isLive))
                     }
                     
                     if (hasContent) {
                         val filteredTests = com.rajasthanexams.data.MockData.popularTests.filter { 
                             it.category.contains(category.title.split(" ")[0], ignoreCase = true) && 
                             ((testType == TestType.LIVE && it.isLive) || (it.type == testType && !it.isLive))
                         }.sortedBy { it.isPremium }
                         cachedTests[category.id] = filteredTests
                     }
                     hasContent
                }
                
                cachedCategories = filteredMockCategories
                if (lastSelectedCategoryIndex >= cachedCategories.size) {
                    lastSelectedCategoryIndex = 0
                }
                _uiState.value = TestsByTypeUiState.Success(cachedCategories, lastSelectedCategoryIndex, cachedTests.toMap())
            }
        }
    }
    
    fun onCategorySelected(index: Int) {
        lastSelectedCategoryIndex = index
        val currentState = _uiState.value
        if (currentState is TestsByTypeUiState.Success) {
            _uiState.value = currentState.copy(selectedCategoryIndex = index)
        }
    }

    fun loadTestsForCategory(category: Category, testType: TestType) {
        // No-op: Data is pre-fetched in loadData
    }

    fun downloadTest(test: Test) {
        viewModelScope.launch {
            // 1. Mark as downloaded in UI immediately (optimistic)
            updateTestStatus(test.id, isDownloaded = true)
            
            // 2. Fetch Questions
            repository.getQuestions(test.id).onSuccess { questionResponses ->
                // Map API Response to Local Data Model
                val questions = questionResponses.map { qr ->
                    com.rajasthanexams.data.Question(
                        id = qr.id,
                        questionEn = qr.textEn,
                        questionHi = qr.textHi,
                        optionsEn = qr.optionsEn,
                        optionsHi = qr.optionsHi,
                        correctOptionIndex = qr.correctOptionIndex,
                        solutionEn = qr.solutionEn ?: "",
                        solutionHi = qr.solutionHi ?: "",
                        marksPerQuestion = qr.marksPerQuestion,
                        negativeMarks = qr.negativeMarks,
                        subject = qr.subject
                    )
                }

                // 3. Save Questions
                com.rajasthanexams.data.OfflineManager.saveQuestions(test.id, questions)
                
                // 4. Save Metadata
                com.rajasthanexams.data.OfflineManager.downloadTest(
                    id = test.id,
                    title = test.title,
                    type = test.type.name,
                    questions = test.questions,
                    time = test.time,
                    negativeMarks = test.negativeMarks,
                    marksPerQuestion = test.marksPerQuestion
                )
            }.onFailure {
                // Revert UI if failed
                updateTestStatus(test.id, isDownloaded = false)
            }
        }
    }

    private fun updateTestStatus(testId: String, isDownloaded: Boolean) {
        val currentCategories = (uiState.value as? TestsByTypeUiState.Success)?.categories ?: return
        
        synchronized(cachedTests) {
             cachedTests.forEach { (categoryId, tests) ->
                 val updatedList = tests.map { t ->
                     if (t.id == testId) t.copy(isDownloaded = isDownloaded) else t
                 }
                 cachedTests[categoryId] = updatedList
             }
        }
        _uiState.value = TestsByTypeUiState.Success(currentCategories, 0, cachedTests.toMap())
    }

    fun refreshData() {
        cachedCategories = emptyList()
        cachedTests.clear()
        currentTestType?.let { loadData(it) }
    }
}
