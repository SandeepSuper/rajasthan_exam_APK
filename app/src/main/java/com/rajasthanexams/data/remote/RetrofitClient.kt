package com.rajasthanexams.data.remote

import com.rajasthanexams.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://exam.photopassport.in/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var sessionManager: com.rajasthanexams.data.local.SessionManager? = null

    fun init(context: android.content.Context) {
        sessionManager = com.rajasthanexams.data.local.SessionManager(context)
    }

    // Event for session expiry
    private val _logoutEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val logoutEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _logoutEvent

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
        
        val response = chain.proceed(request)
        
        if (response.code == 401 || response.code == 403) {
            // Token expired or invalid
            sessionManager?.clearSession()
            _logoutEvent.tryEmit(Unit)
        }
        
        response
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .build()

    fun getHttpClient(): OkHttpClient = httpClient

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
