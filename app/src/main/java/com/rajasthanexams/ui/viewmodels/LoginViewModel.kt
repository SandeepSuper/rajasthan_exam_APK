package com.rajasthanexams.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.local.SessionManager
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.dto.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object EmailOtpSent : LoginUiState()
    object LoggedIn : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.getService()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // Login fields
    var email = MutableStateFlow("")
    var password = MutableStateFlow("")

    // OTP field (used after register)
    var otp = MutableStateFlow("")

    // Pending email address awaiting OTP verification (set after register)
    var pendingEmail = MutableStateFlow("")

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> = _isNewUser

    // ─── Email Login ────────────────────────────────────────────────

    fun loginWithEmail() {
        val emailVal = email.value.trim()
        val passwordVal = password.value
        if (emailVal.isBlank() || passwordVal.isBlank()) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = api.loginWithEmail(EmailLoginRequest(emailVal, passwordVal))
                if (response.isSuccessful && response.body() != null) {
                    handleAuthResponse(response.body()!!, isNew = false)
                } else {
                    val errorMsg = parseErrorMessage(response.code())
                    _uiState.value = LoginUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(friendlyNetworkError(e))
            }
        }
    }

    // ─── Email Register → triggers OTP ──────────────────────────────

    fun registerWithEmail(name: String, emailVal: String, passwordVal: String, referralCode: String?) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = api.registerWithEmail(
                    EmailRegisterRequest(
                        name = name,
                        email = emailVal.trim(),
                        password = passwordVal,
                        referredByCode = referralCode?.takeIf { it.isNotBlank() }
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    pendingEmail.value = emailVal.trim()
                    otp.value = ""
                    _uiState.value = LoginUiState.EmailOtpSent
                } else {
                    val msg = response.body()?.message ?: "Registration failed. Please try again."
                    _uiState.value = LoginUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(friendlyNetworkError(e))
            }
        }
    }

    // ─── Verify Email OTP ───────────────────────────────────────────

    fun verifyEmailOtp() {
        val emailVal = pendingEmail.value
        val otpVal = otp.value
        if (emailVal.isBlank() || otpVal.length != 6) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = api.verifyEmailOtp(VerifyEmailOtpRequest(emailVal, otpVal))
                if (response.isSuccessful && response.body() != null) {
                    handleAuthResponse(response.body()!!, isNew = true)
                } else {
                    _uiState.value = LoginUiState.Error("Invalid OTP. Please check and try again.")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(friendlyNetworkError(e))
            }
        }
    }

    // ─── Resend OTP ─────────────────────────────────────────────────

    fun resendEmailOtp(emailVal: String) {
        viewModelScope.launch {
            try {
                api.sendEmailOtp(SendEmailOtpRequest(emailVal))
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Resend OTP error", e)
            }
        }
    }

    // ─── Google Login ───────────────────────────────────────────────

    fun loginWithGoogle(idToken: String, referredByCode: String? = null) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = api.loginWithGoogle(GoogleLoginRequest(idToken, referredByCode))
                if (response.isSuccessful && response.body() != null) {
                    handleAuthResponse(response.body()!!, isNew = response.body()!!.isNewUser)
                } else {
                    _uiState.value = LoginUiState.Error("Google Login Failed")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(friendlyNetworkError(e))
            }
        }
    }

    // ─── Profile Update ─────────────────────────────────────────────

    fun updateProfile(name: String, emailVal: String, mobileVal: String?, profilePicture: String? = null, referredByCode: String? = null, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: return@launch onResult(false, "User ID not found")
                val response = api.updateProfile(UpdateProfileRequest(userId, name, emailVal, profilePicture, referredByCode))
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val currentCoins = sessionManager.getCoins()
                        sessionManager.saveUser(userId, name, emailVal, profilePicture, currentCoins)
                        
                        // Also update mobile if provided or changed
                        val token = sessionManager.getAuthToken()
                        if (token != null) {
                            try {
                                val mobileResponse = api.updateMobile("Bearer $token", mapOf("mobile" to mobileVal))
                                if (mobileResponse.isSuccessful) {
                                    sessionManager.saveMobileNumber(mobileVal)
                                } else {
                                    Log.e("LoginViewModel", "Failed to update mobile: ${mobileResponse.code()}")
                                }
                            } catch(e: Exception) {
                                Log.e("LoginViewModel", "Error updating mobile", e)
                            }
                        }
                        
                        onResult(true, null)
                    } else {
                        onResult(false, apiResponse.message)
                    }
                } else {
                    onResult(false, "Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                onResult(false, friendlyNetworkError(e))
            }
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri, context: android.content.Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val file = java.io.File(context.cacheDir, "profile_pic.jpg")
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = api.uploadFile(body)
                if (response.isSuccessful && response.body() != null) {
                    val url = response.body()!!["url"]
                    if (url != null) onResult(true, url) else onResult(false, "Upload successful but URL missing")
                } else {
                    onResult(false, "Upload failed: ${response.message()}")
                }
            } catch (e: Exception) {
                onResult(false, friendlyNetworkError(e))
            }
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun handleAuthResponse(authResponse: AuthResponse, isNew: Boolean) {
        sessionManager.saveAuthToken(authResponse.token)
        sessionManager.saveUser(
            userId = authResponse.userId ?: "",
            name = authResponse.name,
            email = authResponse.email,
            profilePicture = authResponse.profilePicture,
            coins = authResponse.coins
        )
        if (!authResponse.referCode.isNullOrBlank()) {
            sessionManager.saveReferCode(authResponse.referCode)
        }
        _isNewUser.value = isNew || authResponse.isNewUser
        _uiState.value = LoginUiState.LoggedIn
    }

    private fun parseErrorMessage(code: Int): String = when (code) {
        400 -> "Galat email ya password. Please check karein."
        401 -> "Unauthorized. Please login again."
        409 -> "Yeh email already registered hai. Please login karein."
        else -> "Server error. Please try again later."
    }

    private fun friendlyNetworkError(e: Throwable): String {
        val msg = e.message ?: e.localizedMessage ?: ""
        if (e is java.net.UnknownHostException || msg.contains("UnknownHost", ignoreCase = true)) {
            return "Internet connection check karein. Server se connect nahi ho pa raha."
        }
        if (e is java.net.SocketTimeoutException || msg.contains("timeout", ignoreCase = true)) {
            return "Server response time out ho gaya. Thodi der baad try karein."
        }
        if (e is retrofit2.HttpException) {
            return when (e.code()) {
                429 -> "Too many requests. Please wait before trying again."
                403 -> "Access forbidden."
                else -> "Server error occurred. Please try again later."
            }
        }
        if (e is java.net.ConnectException || msg.contains("Failed to connect", ignoreCase = true)) {
            return "Server down ya internet issue hai. Kripya check karein."
        }
        return "An unexpected network error occurred."
    }
}
