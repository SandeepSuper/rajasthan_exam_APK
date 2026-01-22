package com.rajasthanexams.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.local.SessionManager
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.dto.OtpRequest
import com.rajasthanexams.data.remote.dto.VerifyOtpRequest
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun sendOtp() {
        val mobile = mobileNumber.value
        if (mobile.length != 10) return

        val fullMobileNumber = "+91$mobile"

        viewModelScope.launch(coroutineExceptionHandler) {
            _uiState.value = LoginUiState.Loading
            try {
                // TODO: 10.0.2.2 is local dev.
                val response = api.sendOtp(OtpRequest(fullMobileNumber))
                if (response.isSuccessful) {
                    _uiState.value = LoginUiState.OtpSent
                } else {
                    _uiState.value = LoginUiState.Error("Failed to send OTP. Please check your mobile number and try again.")
                }
            } catch (e: Throwable) {
                _uiState.value = LoginUiState.Error("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

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
                    sessionManager.saveUser(authResponse.userId ?: "", authResponse.name)
                    _uiState.value = LoginUiState.LoggedIn
                } else {
                    _uiState.value = LoginUiState.Error("Invalid OTP")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Connection Error: ${e.message}")
            }
        }
    }
}
