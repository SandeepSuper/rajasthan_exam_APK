package com.rajasthanexams.data

import android.content.Context
import android.content.SharedPreferences
import com.rajasthanexams.data.MockData // Assuming MockData access for Test details if needed, or just IDs

object OfflineManager {
    private const val PREF_NAME = "offline_data"
    private const val KEY_DOWNLOADED_TESTS = "downloaded_tests"
    private const val KEY_PENDING_RESULTS = "pending_results"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // --- Downloaded Tests ---
    fun getDownloadedTestIds(): Set<String> {
        return prefs.getStringSet(KEY_DOWNLOADED_TESTS, emptySet()) ?: emptySet()
    }

    fun downloadTest(testId: String) {
        val current = getDownloadedTestIds().toMutableSet()
        current.add(testId)
        prefs.edit().putStringSet(KEY_DOWNLOADED_TESTS, current).apply()
    }

    fun isTestDownloaded(testId: String): Boolean {
        return getDownloadedTestIds().contains(testId)
    }

    fun removeDownload(testId: String) {
        val current = getDownloadedTestIds().toMutableSet()
        current.remove(testId)
        prefs.edit().putStringSet(KEY_DOWNLOADED_TESTS, current).apply()
    }

    // --- Offline Results (Simulated Sync) ---
    // Storing as JSON string or simple delimiter string for prototype
    fun saveOfflineResult(resultId: String, score: String) {
        val current = prefs.getStringSet(KEY_PENDING_RESULTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add("$resultId:$score")
        prefs.edit().putStringSet(KEY_PENDING_RESULTS, current).apply()
    }

    fun getPendingResults(): Set<String> {
        return prefs.getStringSet(KEY_PENDING_RESULTS, emptySet()) ?: emptySet()
    }
    
    fun clearPendingResults() {
        prefs.edit().remove(KEY_PENDING_RESULTS).apply()
    }
}
