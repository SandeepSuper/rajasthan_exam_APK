package com.rajasthanexams.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("referredByCode") val referredByCode: String? = null
)

// ── Email + Password Auth ─────────────────────────────────────

data class EmailRegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("referredByCode") val referredByCode: String? = null
)

data class EmailLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class SendEmailOtpRequest(
    @SerializedName("email") val email: String
)

data class VerifyEmailOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("newPassword") val newPassword: String
)

// ── Legacy DTOs (kept in case old sessions use them) ─────────

data class OtpRequest(
    @SerializedName("mobile") val mobile: String
)

data class OtpResponse(
    val message: String,
    val otp: String? = null
)

data class VerifyOtpRequest(
    @SerializedName("mobile") val mobile: String,
    @SerializedName("otp") val otp: String
)

// ── Shared Responses ─────────────────────────────────────────

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

data class UserProfileResponse(
    val id: String,
    val name: String?,
    val email: String?,
    val mobile: String?,
    val profilePicture: String?,
    val coins: Int,
    val referCode: String?,
    val isPremium: Boolean
)
