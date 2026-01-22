package com.rajasthanexams.data.remote

import com.rajasthanexams.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequest): Response<ResponseBody>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    // Content APIs
    @GET("tests/exams")
    suspend fun getExams(): Response<List<com.rajasthanexams.data.remote.dto.ExamResponse>> // Assuming DTO exists or uses same structure

    @GET("tests")
    suspend fun getTests(@Query("examId") examId: String): Response<List<TestResponse>>

    @GET("tests/questions")
    suspend fun getQuestions(@Query("testId") testId: String): Response<List<QuestionResponse>>
    
    @POST("tests/submit")
    suspend fun submitTest(
        @Header("Authorization") token: String,
        @Body request: SubmitTestRequest
    ): Response<TestResultResponse>

    @GET("leaderboard")
    suspend fun getLeaderboard(@Query("examId") examId: String): Response<List<LeaderboardEntry>>
}
