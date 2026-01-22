package com.rajasthanexams.data.remote.dto

import java.util.UUID

data class ExamResponse(
    val id: String,
    val title: String,
    val category: String,
    val iconUrl: String?,
    val languageSupported: String?
)

// Match Backend TestResponse
data class TestResponse(
    val id: String,
    val title: String,
    val type: String,
    val durationMinutes: Int,
    val totalQuestions: Int,
    val isPremium: Boolean
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
    val solutionEn: String?
)

data class SubmitTestRequest(
    val answers: Map<String, Int>, // QuestionID -> OptionIndex
    val timeTakenSeconds: Int
)

data class TestResultResponse(
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Double,
    val solutions: Map<String, String> // QuestionID -> Solution Text
)

data class LeaderboardEntry(
    val userId: String,
    val name: String,
    val rank: Int
)
