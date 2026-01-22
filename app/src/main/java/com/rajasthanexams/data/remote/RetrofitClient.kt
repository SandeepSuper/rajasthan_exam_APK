package com.rajasthanexams.data.remote

import com.rajasthanexams.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Use BuildConfig.API_BASE_URL if available, otherwise default to localhost (emulator)
    private const val BASE_URL = "http://13.126.216.84:8080/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var sessionManager: com.rajasthanexams.data.local.SessionManager? = null

    fun init(context: android.content.Context) {
        sessionManager = com.rajasthanexams.data.local.SessionManager(context)
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val token = sessionManager?.getAuthToken()
        val request = if (!token.isNullOrEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(ApiService::class.java)
    }
    
    // Helper to get service easily
    fun getService(): ApiService = api
}
