package com.rajasthanexams.data.repository

import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.dto.ExamResponse
import com.rajasthanexams.data.remote.dto.TestResponse
import com.rajasthanexams.data.remote.dto.QuestionResponse
import retrofit2.Response

class ContentRepository {
    private val api = RetrofitClient.getService()

    suspend fun getExams(): Result<List<ExamResponse>> {
        return try {
             val response = api.getExams()
             if (response.isSuccessful && response.body() != null) {
                 Result.success(response.body()!!)
             } else {
                 Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
             }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTests(examId: String): Result<List<TestResponse>> {
        return try {
            val response = api.getTests(examId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getQuestions(testId: String): Result<List<QuestionResponse>> {
        return try {
            val response = api.getQuestions(testId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                 Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
