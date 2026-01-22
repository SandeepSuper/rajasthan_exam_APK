package com.rajasthanexams.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    @SerializedName("mobile")
    val mobile: String
)

data class OtpResponse(
    val message: String
)

data class VerifyOtpRequest(
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("otp")
    val otp: String
)

data class AuthResponse(
    val token: String,
    val isNewUser: Boolean,
    val userId: String? = null,
    val name: String? = null
)
