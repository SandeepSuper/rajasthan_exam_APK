package com.rajasthanexams.data.remote.dto

import java.util.UUID
import com.google.gson.annotations.SerializedName

data class ExamResponse(
    val id: String,
    val title: String,
    val category: String,
    val iconUrl: String?,
    val testCount: Int = 0,
    val languageSupported: String?,
    val isPremium: Boolean = false,
    val price: Double = 0.0,
    val isPurchased: Boolean = false,
    val discountPercent: Int = 0       // ← From backend admin discount setting
)


// Match Backend TestResponse
data class TestResponse(
    val id: String,
    val title: String,
    val type: String,
    val durationMinutes: Int,
    val totalQuestions: Int,
    val isPremium: Boolean,
    val isLive: Boolean = false,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val allowPrevious: Boolean = true,
    val allowSolution: Boolean? = true,
    val sectionLock: Boolean = false,
    val showResultImmediately: Boolean = true,
    val negativeMarks: Double = 0.0,
    val marksPerQuestion: Double = 1.0,
    val totalMarks: Double? = null,
    val isAttempted: Boolean = false
)

// Match Backend QuestionResponse
data class QuestionResponse(
    val id: String,
    val textHi: String,
    val textEn: String,
    val optionsHi: List<String>,
    val optionsEn: List<String>,
    val correctOptionIndex: Int,
    val solutionHi: String?,
    val solutionEn: String?,
    val marksPerQuestion: Double? = null,
    val negativeMarks: Double? = null,
    val subject: String? = null
)

data class SubmitTestRequest(
    val testId: String,
    val answers: Map<String, Int>, // QuestionID -> OptionIndex
    val timeTaken: Int
)

data class TestResultResponse(
    val score: Double,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Double,
    val coinsEarned: Int = 0,
    val newTotalCoins: Int = 0, // Added
    val solutions: Map<String, String> // QuestionID -> Solution Text
)

data class LeaderboardEntry(
    val userId: String,
    val name: String,
    val rank: Int,
    val score: Double,
    val totalMarks: Double = 0.0,
    val timeTaken: Int,
    val coins: Int = 0,
    val exam: String? = null,
    val avatarUrl: String? = null
)

data class CreateOrderRequest(
    val examId: String,
    val useCoins: Boolean = false
)

data class CreateOrderResponse(
    val orderId: String? = null,
    val amount: Int? = null,
    val key: String? = null,
    val examName: String? = null,
    val description: String? = null,
    val free: Boolean? = false,       // true when coins cover 100% — skip Razorpay
    val coinsUsed: Int? = 0,
    val coinDiscount: Double? = 0.0,
    val success: Boolean? = false,
    val message: String? = null
)

data class VerifyPaymentRequest(
    val razorpay_order_id: String,
    val razorpay_payment_id: String,
    val razorpay_signature: String
)

data class PaymentVerificationResponse(
    val success: Boolean,
    val message: String
)

data class PerformanceResponse(
    val totalTests: Int = 0,
    val avgAccuracy: Double = 0.0,
    val bestScore: Double = 0.0,
    val totalTimeSecs: Int = 0,
    val weeklyScores: List<Double> = emptyList(),
    val weeklyAccuracies: List<Double> = emptyList(),
    val weeklyDates: List<String> = emptyList(),
    val weakTopics: List<String> = emptyList()
)

data class NewsItemResponse(
    val id: String,
    val titleHi: String,
    val titleEn: String? = null,
    val descHi: String? = null,
    val descEn: String? = null,
    val date: String,          // LocalDate serialised as "yyyy-MM-dd"
    val imageUrl: String? = null
)

data class NotificationResponse(
    val id: String,
    val title: String,
    val message: String,
    val createdAt: String? = null, // nullable — guards against unexpected date formats from WebSocket
    val isRead: Boolean = false,
    val iconType: String = "info"
)

