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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import com.rajasthanexams.data.Category
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val tests: List<Test>,
        val categories: List<Category>,
        val liveTests: List<Test> = emptyList()
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val repository = ContentRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private var currentExamId: String? = null

    // Map Backend TestResponse to Frontend Test Model
    fun fetchTests(examId: String? = null) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Sync user profile coins silently in the background
            try {
                val context = com.rajasthanexams.MainApplication.instance
                val sessionManager = com.rajasthanexams.data.local.SessionManager(context)
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val profileRes = com.rajasthanexams.data.remote.RetrofitClient.api.getProfile("Bearer $token")
                    if (profileRes.isSuccessful && profileRes.body() != null) {
                        sessionManager.updateCoins(profileRes.body()!!.coins)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Profile sync failed", e)
            }
            
            // Update currentExamId if provided, otherwise fallback to existing or default
            if (examId != null) {
                currentExamId = examId
            }
            
            android.util.Log.d("HomeViewModel", "Fetching tests for examId: $examId (current: $currentExamId)")

            // 1. Fetch Categories (Exams)
            var currentCategories = emptyList<Category>()
            var targetExamId = currentExamId ?: "249d6387-f822-4414-b44c-9799298bc79d"

            val examsResult = repository.getExams()
            if (examsResult.isSuccess) {
                 val exams = examsResult.getOrNull() ?: emptyList()
                 android.util.Log.d("HomeViewModel", "Fetched ${exams.size} exams")
                 if (exams.isNotEmpty()) {
                     // If currentExamId logic didn't set a valid target, take first from list
                     if (currentExamId == null) {
                         targetExamId = exams[0].id
                         currentExamId = targetExamId
                     } else {
                         targetExamId = currentExamId!!
                     }
                     
                     currentCategories = exams.map { exam ->
                         Category(
                             id = exam.id,
                             title = exam.title,
                             icon = Icons.Default.School, 
                             iconUrl = exam.iconUrl,
                             testsAvailable = exam.testCount,
                             isPremium = exam.isPremium,
                             price = exam.price,
                             isPurchased = exam.isPurchased,
                             discountPercent = exam.discountPercent  // ← from backend
                         )
                     }
                 }
            } else {
                android.util.Log.e("HomeViewModel", "Failed to fetch exams: ${examsResult.exceptionOrNull()}")
            }
    
            android.util.Log.d("HomeViewModel", "Target Exam ID: $targetExamId")

            // Determine purchase status and price from the current target exam
            val targetExam = currentCategories.find { it.id == targetExamId }
            val isExamPurchased = targetExam?.isPurchased ?: false
            val examPrice = targetExam?.price ?: 0.0

            // 2. Fetch Tests for the Target Exam
            val result = repository.getTests(targetExamId)
            
            // 3. Fetch Live Tests
            val liveTestsResult = repository.getLiveTests()
            val liveTests = if (liveTestsResult.isSuccess) {
                liveTestsResult.getOrNull()?.map { apiTest ->
                     Test(
                        id = apiTest.id,
                        title = apiTest.title,
                        category = "Live",
                        questions = apiTest.totalQuestions,
                        time = apiTest.durationMinutes,
                        attempts = "Active",
                        rating = 5.0,
                        isLive = true,
                        startsAt = apiTest.startsAt,
                        endsAt = apiTest.endsAt,
                        allowPrevious = apiTest.allowPrevious,
                        sectionLock = apiTest.sectionLock,
                        type = try { TestType.valueOf(apiTest.type) } catch(e:Exception) { TestType.MOCK },
                        negativeMarks = apiTest.negativeMarks,
                        marksPerQuestion = apiTest.marksPerQuestion,
                        totalMarks = apiTest.totalMarks,
                        isPremium = apiTest.isPremium,
                        isPurchased = false // Live tests separate logic for now
                    )
                } ?: emptyList()
            } else {
                android.util.Log.e("HomeViewModel", "Failed to fetch live tests: ${liveTestsResult.exceptionOrNull()}")
                emptyList()
            }
            
            result.onSuccess { apiTests ->
                android.util.Log.d("HomeViewModel", "Fetched ${apiTests.size} tests")
                val uiTests = apiTests.map { apiTest ->
                    Test(
                        id = apiTest.id,
                        title = apiTest.title,
                        category = "Exam", 
                        questions = apiTest.totalQuestions,
                        time = apiTest.durationMinutes,
                        attempts = "New",
                        rating = 5.0,
                        isLive = false,
                        startsAt = apiTest.startsAt,
                        endsAt = apiTest.endsAt,
                        allowPrevious = apiTest.allowPrevious,
                        sectionLock = apiTest.sectionLock,
                        type = try { TestType.valueOf(apiTest.type) } catch(e:Exception) { TestType.MOCK },
                        negativeMarks = apiTest.negativeMarks,
                        marksPerQuestion = apiTest.marksPerQuestion,
                        totalMarks = apiTest.totalMarks,
                        isPremium = apiTest.isPremium,
                        isPurchased = isExamPurchased, // Inherit from Exam
                        isAttempted = apiTest.isAttempted || com.rajasthanexams.data.OfflineManager.isTestAttempted(apiTest.id),
                        examId = targetExamId,
                        price = examPrice // Inherit from Exam
                    )
                }
                 _uiState.value = HomeUiState.Success(uiTests, currentCategories, liveTests)
            }.onFailure {
                 android.util.Log.e("HomeViewModel", "Failed to fetch tests: ${it.message}")
                 _uiState.value = HomeUiState.Error("Failed to connect to server: ${it.message}")
            }
        }
    }

    fun initiatePurchase(context: android.content.Context, examId: String, price: Double, useCoins: Boolean = false) {
        viewModelScope.launch {
            val sessionManager = com.rajasthanexams.data.local.SessionManager(context)
            if (sessionManager.getAuthToken() == null) {
                android.widget.Toast.makeText(context, "Please login to purchase", android.widget.Toast.LENGTH_SHORT).show()
                return@launch
            }

            val result = repository.createOrder(examId, useCoins)
            result.onSuccess { orderResponse ->
                if (orderResponse.free == true) {
                    // Paid entirely by coins — no Razorpay needed
                    android.widget.Toast.makeText(context, "🎉 Exam unlocked with coins!", android.widget.Toast.LENGTH_LONG).show()
                    fetchTests() // Refresh purchase status
                    _purchaseSuccess.emit(Unit)
                } else {
                    com.rajasthanexams.utils.PaymentManager.startPayment(context as android.app.Activity, orderResponse)
                }
            }.onFailure {
                android.widget.Toast.makeText(context, "Failed to create order: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private val _purchaseSuccess = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val purchaseSuccess: kotlinx.coroutines.flow.SharedFlow<Unit> = _purchaseSuccess.asSharedFlow()

    private fun verifyPayment(orderId: String, paymentId: String, signature: String) {
        viewModelScope.launch {
            val request = com.rajasthanexams.data.remote.dto.VerifyPaymentRequest(
                razorpay_order_id = orderId,
                razorpay_payment_id = paymentId,
                razorpay_signature = signature
            )
            
            val result = repository.verifyPayment(request)
            result.onSuccess {
                // Refresh data to update "isPurchased" status
                fetchTests()
                _purchaseSuccess.emit(Unit)
                // Optionally show success toast/dialog
            }.onFailure {
                // Handle error
            }
        }
    }

    init {
        fetchTests()
        
        viewModelScope.launch {
            com.rajasthanexams.utils.PaymentManager.paymentResult.collect { result ->
                when(result) {
                    is com.rajasthanexams.utils.PaymentResult.Success -> {
                         verifyPayment(result.orderId, result.paymentId, result.signature)
                    }
                    is com.rajasthanexams.utils.PaymentResult.Error -> {
                        // Handle error
                    }
                }
            }
        }
    }
    
    // ... existing registration methods ...
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
}
