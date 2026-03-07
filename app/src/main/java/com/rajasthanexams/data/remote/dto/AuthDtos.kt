package com.rajasthanexams.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    @SerializedName("mobile")
    val mobile: String
)

data class OtpResponse(
    val message: String,
    val otp: String? = null
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
    val name: String? = null,
    val email: String? = null,
    val profilePicture: String? = null,
    val isPremium: Boolean = false,
    val coins: Int = 0,
    val referCode: String? = null
)

data class UpdateProfileRequest(
    val userId: String,
    val name: String,
    val email: String,
    val profilePicture: String? = null,
    val referredByCode: String? = null
)

data class ApiResponse(
    val message: String,
    val success: Boolean = true
)
