package com.rajasthanexams.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.local.SessionManager
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.ReferredUserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReferralUiState {
    object Loading : ReferralUiState()
    data class Success(
        val referCode: String,
        val referredCount: Int,
        val coinsEarned: Int,
        val myReferrals: List<ReferredUserResponse>,
        val playStoreUrl: String,
        val shareMessage: String
    ) : ReferralUiState()
    data class Error(val message: String) : ReferralUiState()
}

class ReferralViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow<ReferralUiState>(ReferralUiState.Loading)
    val uiState: StateFlow<ReferralUiState> = _uiState

    private val sessionManager = SessionManager(app)
    private val api = RetrofitClient.api

    private val defaultPlayStoreUrl = "https://play.google.com/store/apps/details?id=com.rajasthanexams"

    init {
        loadReferralData()
    }

    fun loadReferralData() {
        viewModelScope.launch {
            _uiState.value = ReferralUiState.Loading

            // Try to get referral info from backend
            var referCode = sessionManager.getReferCode()
            var referredCount = 0
            var coinsEarned = 0
            var myReferrals = emptyList<ReferredUserResponse>()
            var playStoreUrl = defaultPlayStoreUrl
            var shareMessage = ""

            try {
                val token = "Bearer ${sessionManager.getAuthToken()}"
                val referralRes = api.getMyReferralInfo(token)
                if (referralRes.isSuccessful && referralRes.body() != null) {
                    val body = referralRes.body()!!
                    referCode = body.referCode
                    referredCount = body.referredCount
                    coinsEarned = body.coinsEarned
                    sessionManager.saveReferCode(body.referCode)
                }
            } catch (_: Exception) { /* Backend not deployed yet or offline — use cached */ }

            try {
                val token = "Bearer ${sessionManager.getAuthToken()}"
                val refsRes = api.getMyReferrals(token)
                if (refsRes.isSuccessful && refsRes.body() != null) {
                    myReferrals = refsRes.body()!!
                }
            } catch (_: Exception) { /* Ignore — show empty list */ }

            try {
                val configRes = api.getAppConfig()
                if (configRes.isSuccessful && configRes.body() != null) {
                    playStoreUrl = configRes.body()!!.playStoreUrl
                    val template = configRes.body()!!.shareMessage
                    shareMessage = template
                        .replace("{CODE}", referCode ?: "")
                        .replace("{URL}", playStoreUrl)
                }
            } catch (_: Exception) { /* Use default share message */ }

            if (shareMessage.isBlank() && referCode != null) {
                shareMessage = "Join Rajasthan Exam Prep and ace your exams! 🎓\n" +
                    "Use my referral code: $referCode when signing up to get FREE coins!\n" +
                    "Download: $playStoreUrl"
            }

            if (referCode != null) {
                _uiState.value = ReferralUiState.Success(
                    referCode = referCode,
                    referredCount = referredCount,
                    coinsEarned = coinsEarned,
                    myReferrals = myReferrals,
                    playStoreUrl = playStoreUrl,
                    shareMessage = shareMessage
                )
            } else {
                // No code at all — backend not deployed + no cache
                _uiState.value = ReferralUiState.Error("Deploy updated backend to activate referral codes")
            }
        }
    }
}
