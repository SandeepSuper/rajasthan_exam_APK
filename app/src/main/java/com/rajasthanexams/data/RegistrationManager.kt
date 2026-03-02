package com.rajasthanexams.data

import android.content.Context
import android.content.SharedPreferences

object RegistrationManager {
    private const val PREF_NAME = "test_registrations"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isRegistered(testId: String): Boolean {
        if (!::prefs.isInitialized) return false
        return prefs.getBoolean(testId, false)
    }

    fun register(testId: String) {
        if (!::prefs.isInitialized) return
        prefs.edit().putBoolean(testId, true).apply()
    }
    
    fun getAllRegistered(): Set<String> {
        if (!::prefs.isInitialized) return emptySet()
        return prefs.all.keys
    }
}
