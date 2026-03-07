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
        const val KEY_REFER_CODE = "refer_code"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_UI_HINDI = "ui_hindi"
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

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)
    fun setDarkMode(enabled: Boolean) { prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply() }

    fun isUiHindi(): Boolean = prefs.getBoolean(KEY_UI_HINDI, false)
    fun setUiHindi(enabled: Boolean) { prefs.edit().putBoolean(KEY_UI_HINDI, enabled).apply() }

    fun getUnreadNotificationCount(): Int = prefs.getInt("unread_notifications", 0)
    fun saveUnreadNotificationCount(count: Int) { prefs.edit().putInt("unread_notifications", count).apply() }

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

    fun saveReferCode(code: String) {
        prefs.edit().putString(KEY_REFER_CODE, code).apply()
    }

    fun getReferCode(): String? {
        return prefs.getString(KEY_REFER_CODE, null)
    }
}
