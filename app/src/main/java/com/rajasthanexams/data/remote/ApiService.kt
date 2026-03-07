package com.rajasthanexams.data.remote

import com.rajasthanexams.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class ReferralInfoResponse(
    val referCode: String,
    val referredCount: Int,
    val coinsEarned: Int
)

data class TopReferrerResponse(
    val name: String,
    val referredCount: Int,
    val avatarId: String?
)

data class AppConfigResponse(
    val playStoreUrl: String,
    val referrerCoinReward: Int = 50,
    val refereeCoinReward: Int = 20,
    val shareMessage: String
)

interface ApiService {

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequest): Response<OtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("auth/update-profile")
    suspend fun updateProfile(@Body request: com.rajasthanexams.data.remote.dto.UpdateProfileRequest): Response<com.rajasthanexams.data.remote.dto.ApiResponse>

    @retrofit2.http.Multipart
    @POST("upload")
    suspend fun uploadFile(
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): Response<Map<String, String>>

    // Content APIs
    @GET("tests/exams")
    suspend fun getExams(): Response<List<com.rajasthanexams.data.remote.dto.ExamResponse>> // Assuming DTO exists or uses same structure

    @GET("/api/tests")
    suspend fun getTests(@Query("examId") examId: String): Response<List<TestResponse>>

    @GET("/api/tests/live")
    suspend fun getLiveTests(): Response<List<TestResponse>>

    @GET("/api/tests/questions")
    suspend fun getQuestions(@Query("testId") testId: String): Response<List<QuestionResponse>>

    @GET("/api/tests/{testId}")
    suspend fun getTestDetails(@retrofit2.http.Path("testId") testId: String): Response<TestResponse>
    
    @POST("/api/tests/submit")
    suspend fun submitTest(
        @Header("Authorization") token: String,
        @Body request: com.rajasthanexams.data.remote.dto.SubmitTestRequest
    ): retrofit2.Response<com.rajasthanexams.data.remote.dto.TestResultResponse>

    @GET("/api/tests/performance")
    suspend fun getPerformance(
        @Header("Authorization") token: String
    ): Response<com.rajasthanexams.data.remote.dto.PerformanceResponse>






    @POST("reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Body request: com.rajasthanexams.data.remote.dto.CreateReportRequest
    ): retrofit2.Response<Map<String, String>>
    
    // Payment APIs
    @POST("payment/create-order")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: com.rajasthanexams.data.remote.dto.CreateOrderRequest
    ): retrofit2.Response<CreateOrderResponse>

    @POST("payment/verify")
    suspend fun verifyPayment(
        @Header("Authorization") token: String,
        @Body request: VerifyPaymentRequest
    ): retrofit2.Response<PaymentVerificationResponse>

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") token: String,
        @Query("testId") testId: String?
    ): Response<List<LeaderboardEntry>>

    @POST("/api/leaderboard/sync-coins")
    suspend fun syncCoins(
        @Header("Authorization") token: String,
        @Body request: Map<String, Int>
    ): Response<Map<String, Any>>

    @GET("/api/news")
    suspend fun getNews(): Response<List<com.rajasthanexams.data.remote.dto.NewsItemResponse>>

    @GET("/api/notifications")
    suspend fun getNotifications(): Response<List<com.rajasthanexams.data.remote.dto.NotificationResponse>>

    @retrofit2.http.PUT("/api/notifications/mark-all-read")
    suspend fun markAllNotificationsRead(): Response<Unit>


    // Community APIs
    @GET("/api/community/posts")
    suspend fun getCommunityPosts(@Query("userId") userId: String?): Response<List<CommunityPostResponse>>

    @POST("/api/community/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<CommunityPostResponse>

    @GET("/api/community/posts/{postId}/comments")
    suspend fun getComments(@retrofit2.http.Path("postId") postId: String): Response<List<CommunityCommentResponse>>

    @POST("/api/community/posts/{postId}/comments")
    suspend fun addComment(
        @retrofit2.http.Path("postId") postId: String,
        @Body request: CreateCommentRequest
    ): Response<CommunityCommentResponse>

    @retrofit2.http.PUT("/api/community/posts/{postId}/view")
    suspend fun incrementView(@retrofit2.http.Path("postId") postId: String): Response<Unit>

    @retrofit2.http.PUT("/api/community/posts/{postId}/like")
    suspend fun toggleLike(
        @retrofit2.http.Path("postId") postId: String,
        @retrofit2.http.Query("userId") userId: String
    ): Response<Boolean>

    // Referral
    @GET("/api/users/referral")
    suspend fun getMyReferralInfo(
        @Header("Authorization") token: String
    ): Response<ReferralInfoResponse>

    @GET("/api/users/top-referrers")
    suspend fun getTopReferrers(): Response<List<TopReferrerResponse>>

    // App Config (Play Store URL, coin rewards, etc.)
    @GET("/api/config")
    suspend fun getAppConfig(): Response<AppConfigResponse>
}
