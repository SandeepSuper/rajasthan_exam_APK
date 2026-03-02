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
    
    suspend fun getLiveTests(): Result<List<TestResponse>> {
        return try {
            val response = api.getLiveTests()
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

    suspend fun getTestDetails(testId: String): Result<TestResponse> {
        return try {
            val response = api.getTestDetails(testId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createOrder(examId: String): Result<com.rajasthanexams.data.remote.dto.CreateOrderResponse> {
        return try {
            val token = com.rajasthanexams.data.local.SessionManager(com.rajasthanexams.MainApplication.instance).getAuthToken() 
                ?: return Result.failure(Exception("Not logged in"))
            
            val request = mapOf("examId" to examId)
            val response = api.createOrder("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                android.util.Log.e("ContentRepo", "Create order failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPayment(data: com.rajasthanexams.data.remote.dto.VerifyPaymentRequest): Result<com.rajasthanexams.data.remote.dto.PaymentVerificationResponse> {
        return try {
            val token = com.rajasthanexams.data.local.SessionManager(com.rajasthanexams.MainApplication.instance).getAuthToken() 
                ?: return Result.failure(Exception("Not logged in"))
                
            val response = api.verifyPayment("Bearer $token", data)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error verifying payment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPerformance(): Result<com.rajasthanexams.data.remote.dto.PerformanceResponse> {
        return try {
            val token = com.rajasthanexams.data.local.SessionManager(com.rajasthanexams.MainApplication.instance).getAuthToken()
                ?: return Result.failure(Exception("Not logged in"))
            val response = api.getPerformance("Bearer $token")
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
