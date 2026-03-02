package com.rajasthanexams.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN = "jwt_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_PROFILE_PIC = "user_profile_pic"
        const val KEY_USER_COINS = "user_coins"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(userId: String, name: String?, email: String?, profilePicture: String? = null, coins: Int = 0) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        if (profilePicture != null) {
            editor.putString(KEY_USER_PROFILE_PIC, profilePicture)
        }
        editor.putInt(KEY_USER_COINS, coins)
        editor.apply()
    }
    
    fun getProfilePicture(): String? {
        return prefs.getString(KEY_USER_PROFILE_PIC, null)
    }

    fun getCoins(): Int {
        return prefs.getInt(KEY_USER_COINS, 0)
    }

    fun updateCoins(coins: Int) {
        prefs.edit().putInt(KEY_USER_COINS, coins).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
}
