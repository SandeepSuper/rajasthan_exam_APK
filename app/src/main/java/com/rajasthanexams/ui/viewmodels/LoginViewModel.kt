package com.rajasthanexams.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.local.SessionManager
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.dto.OtpRequest
import com.rajasthanexams.data.remote.dto.VerifyOtpRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object OtpSent : LoginUiState()
    object LoggedIn : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.getService()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    var mobileNumber = MutableStateFlow("")
    var otp = MutableStateFlow("")

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = LoginUiState.Error("Something went wrong")
        Log.e("LoginViewModel", "Error", throwable)
    }

    private val _otpReceived = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val otpReceived = _otpReceived.asSharedFlow()

    fun sendOtp() {
        val mobile = mobileNumber.value
        if (mobile.length != 10) return
        val fullMobileNumber = "+91$mobile"

        viewModelScope.launch(coroutineExceptionHandler) {
            _uiState.value = LoginUiState.Loading
            try {
                val response = api.sendOtp(OtpRequest(fullMobileNumber))
                if (response.isSuccessful && response.body() != null) {
                    val otpResponse = response.body()!!
                    _uiState.value = LoginUiState.OtpSent
                    if (!otpResponse.otp.isNullOrEmpty()) {
                        _otpReceived.emit("Your OTP is ${otpResponse.otp}")
                    }
                } else {
                    _uiState.value = LoginUiState.Error("Failed to send OTP. Please check your mobile number and try again.")
                }
            } catch (e: Throwable) {
                _uiState.value = LoginUiState.Error("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> = _isNewUser

    fun verifyOtp() {
        val mobile = mobileNumber.value
        val otpValue = otp.value
        if (otpValue.isEmpty()) return
 
        val fullMobileNumber = "+91$mobile"

        viewModelScope.launch(coroutineExceptionHandler) {
            _uiState.value = LoginUiState.Loading
            try {
                val response = api.verifyOtp(VerifyOtpRequest(fullMobileNumber, otpValue))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    sessionManager.saveAuthToken(authResponse.token)
                    sessionManager.saveUser(
                        userId = authResponse.userId ?: "",
                        name = authResponse.name,
                        email = authResponse.email,
                        profilePicture = authResponse.profilePicture, // Added
                        coins = authResponse.coins // Added
                    )
                    
                    if (authResponse.isNewUser) {
                        _isNewUser.value = true
                        _uiState.value = LoginUiState.LoggedIn // Or a specific state like NeedsProfile
                    } else {
                        _uiState.value = LoginUiState.LoggedIn
                    }
                } else {
                    _uiState.value = LoginUiState.Error("Invalid OTP")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Connection Error: ${e.message}")
            }
        }
    }

    fun updateProfile(name: String, email: String, profilePicture: String? = null, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: return@launch onResult(false, "User ID not found")
                val response = api.updateProfile(com.rajasthanexams.data.remote.dto.UpdateProfileRequest(userId, name, email, profilePicture))
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val currentCoins = sessionManager.getCoins()
                        sessionManager.saveUser(userId, name, email, profilePicture, currentCoins) // Update local name and email and pic
                        onResult(true, null)
                    } else {
                        onResult(false, apiResponse.message)
                    }
                } else {
                    onResult(false, "Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Exception: ${e.localizedMessage}")
            }
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri, context: android.content.Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // Get file from URI
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val file = java.io.File(context.cacheDir, "profile_pic.jpg") // specific file name or simple temp
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val start = System.currentTimeMillis()
                val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val response = api.uploadFile(body)
                if (response.isSuccessful && response.body() != null) {
                    val url = response.body()!!["url"]
                    if (url != null) {
                       // We have the URL, can just return it or save it temporarily
                       onResult(true, url)
                    } else {
                        onResult(false, "Upload successful but URL missing")
                    }
                } else {
                    onResult(false, "Upload failed: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Exception: ${e.localizedMessage}")
            }
        }
    }
}
