package com.rajasthanexams.data.remote.dto

data class CreateReportRequest(
    val testId: String,
    val questionId: String,
    val bugType: String,
    val description: String?
)
