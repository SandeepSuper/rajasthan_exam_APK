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
    // Format: "id|title|type|negativeMarks|marksPerQuestion"
    fun getDownloadedTests(): List<OfflineTest> {
        val set = prefs.getStringSet(KEY_DOWNLOADED_TESTS, emptySet()) ?: emptySet()
        return set.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 3) {
                val neg = if (parts.size >= 4) parts[3].toDoubleOrNull() ?: 0.0 else 0.0
                val marks = if (parts.size >= 5) parts[4].toDoubleOrNull() ?: 1.0 else 1.0
                val questions = if (parts.size >= 6) parts[5].toIntOrNull() ?: 0 else 0
                val time = if (parts.size >= 7) parts[6].toIntOrNull() ?: 0 else 0
                OfflineTest(parts[0], parts[1], parts[2], neg, marks, questions, time)
            } else {
                null
            }
        }
    }

    fun downloadTest(id: String, title: String, type: String, negativeMarks: Double = 0.0, marksPerQuestion: Double = 1.0, questions: Int, time: Int) {
        val current = prefs.getStringSet(KEY_DOWNLOADED_TESTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        // Remove existing if any (to update)
        current.removeAll { it.startsWith("$id|") }
        current.add("$id|$title|$type|$negativeMarks|$marksPerQuestion|$questions|$time")
        prefs.edit().putStringSet(KEY_DOWNLOADED_TESTS, current).apply()
    }

    fun isTestDownloaded(testId: String): Boolean {
        val set = prefs.getStringSet(KEY_DOWNLOADED_TESTS, emptySet()) ?: emptySet()
        return set.any { it.startsWith("$testId|") }
    }

    fun removeDownload(testId: String) {
        val current = prefs.getStringSet(KEY_DOWNLOADED_TESTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.removeAll { it.startsWith("$testId|") }
        prefs.edit().putStringSet(KEY_DOWNLOADED_TESTS, current).apply()
    }

    data class OfflineTest(
        val id: String, 
        val title: String, 
        val type: String, 
        val negativeMarks: Double = 0.0, 
        val marksPerQuestion: Double = 1.0,
        val questions: Int = 0,
        val time: Int = 0
    )

    // --- Question Storage ---
    private val gson = com.google.gson.Gson()

    fun saveQuestions(testId: String, questions: List<Question>) {
        val json = gson.toJson(questions)
        prefs.edit().putString("questions_$testId", json).apply()
    }

    fun getStoredQuestions(testId: String): List<Question>? {
        val json = prefs.getString("questions_$testId", null) ?: return null
        val type = object : com.google.gson.reflect.TypeToken<List<Question>>() {}.type
        return gson.fromJson(json, type)
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

    // --- Bookmarks ---
    private const val KEY_BOOKMARKS = "bookmarks"

    fun saveBookmark(question: Question) {
        val current = getBookmarkedQuestions().toMutableList()
        if (current.none { it.id == question.id }) {
            current.add(question)
            val json = gson.toJson(current)
            prefs.edit().putString(KEY_BOOKMARKS, json).apply()
        }
    }

    fun removeBookmark(questionId: String) {
        val current = getBookmarkedQuestions().toMutableList()
        val removed = current.removeAll { it.id == questionId }
        if (removed) {
            val json = gson.toJson(current)
            prefs.edit().putString(KEY_BOOKMARKS, json).apply()
        }
    }

    fun isBookmarked(questionId: String): Boolean {
        return getBookmarkedQuestions().any { it.id == questionId }
    }

    fun getBookmarkedQuestions(): List<Question> {
        val json = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<Question>>() {}.type
        return gson.fromJson(json, type)
    }

    // --- Temporary Test Metadata Cache (for non-downloaded but viewed tests) ---
    private const val KEY_TEST_CACHE = "test_meta_"
    private const val KEY_ATTEMPTED = "attempted_tests"

    fun saveTestMetadata(testId: String, details: Any) { // Using Any to avoid DTO dependency here if possible, or pass fields
        // Serialize details object
        val json = gson.toJson(details)
        prefs.edit().putString(KEY_TEST_CACHE + testId, json).apply()
    }

    fun getTestMetadata(testId: String, classOfT: Class<*>): Any? {
        val json = prefs.getString(KEY_TEST_CACHE + testId, null) ?: return null
        return gson.fromJson(json, classOfT)
    }

    fun markTestAttempted(testId: String) {
        val current = prefs.getStringSet(KEY_ATTEMPTED, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(testId)
        prefs.edit().putStringSet(KEY_ATTEMPTED, current).apply()
        // Clear retake mode as the attempt is now complete
        setRetakeMode(testId, false)
    }

    fun isTestAttempted(testId: String): Boolean {
        val set = prefs.getStringSet(KEY_ATTEMPTED, emptySet()) ?: emptySet()
        return set.contains(testId)
    }

    // --- Persist User Answers for Review ---
    private const val KEY_ANSWERS_PREFIX = "answers_"

    fun saveUserAnswers(testId: String, answers: Map<String, Int>) {
        val json = gson.toJson(answers)
        prefs.edit().putString(KEY_ANSWERS_PREFIX + testId, json).apply()
    }

    fun getUserAnswers(testId: String): Map<String, Int> {
        val json = prefs.getString(KEY_ANSWERS_PREFIX + testId, null) ?: return emptyMap()
        val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearTestUserData(testId: String) {
        val attempts = prefs.getStringSet(KEY_ATTEMPTED, emptySet())?.toMutableSet() ?: mutableSetOf()
        attempts.remove(testId)
        prefs.edit().putStringSet(KEY_ATTEMPTED, attempts).apply()
        
        prefs.edit().remove(KEY_ANSWERS_PREFIX + testId).apply()
        prefs.edit().remove(KEY_MARKED_PREFIX + testId).apply()
        prefs.edit().remove(KEY_TIME_PREFIX + testId).apply()
        // Do NOT clear KEY_TEST_CACHE here. Metadata should persist for retakes.
        // prefs.edit().remove(KEY_TEST_CACHE + testId).apply()
        clearTestProgress(testId)
    }

    private const val KEY_RETAKE = "retake_mode_"
    
    fun setRetakeMode(testId: String, enable: Boolean) {
        prefs.edit().putBoolean(KEY_RETAKE + testId, enable).apply()
    }
    
    fun isRetakeMode(testId: String): Boolean {
        return prefs.getBoolean(KEY_RETAKE + testId, false)
    }

    // --- Marked Questions Persistence ---
    private const val KEY_MARKED_PREFIX = "marked_"

    fun saveMarkedQuestions(testId: String, questionIds: List<String>) {
        val json = gson.toJson(questionIds)
        prefs.edit().putString(KEY_MARKED_PREFIX + testId, json).apply()
    }

    fun getMarkedQuestions(testId: String): List<String> {
        val json = prefs.getString(KEY_MARKED_PREFIX + testId, null) ?: return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    // --- Time Spent Persistence ---
    private const val KEY_TIME_PREFIX = "time_spent_"

    fun saveTimeSpent(testId: String, timeMap: Map<String, Long>) {
        val json = gson.toJson(timeMap)
        prefs.edit().putString(KEY_TIME_PREFIX + testId, json).apply()
    }

    fun getTimeSpent(testId: String): Map<String, Long> {
        val json = prefs.getString(KEY_TIME_PREFIX + testId, null) ?: return emptyMap()
        val type = object : com.google.gson.reflect.TypeToken<Map<String, Long>>() {}.type
        return gson.fromJson(json, type)
    }

    // --- Pause/Resume Progress ---
    private const val KEY_PROGRESS_PREFIX = "progress_"

    data class TestProgress(
        val timeLeft: Int,
        val currentQuestionIndex: Int
    )

    fun saveTestProgress(testId: String, timeLeft: Int, currentIndex: Int) {
        val json = gson.toJson(TestProgress(timeLeft, currentIndex))
        prefs.edit().putString(KEY_PROGRESS_PREFIX + testId, json).apply()
    }

    fun getTestProgress(testId: String): TestProgress? {
        val json = prefs.getString(KEY_PROGRESS_PREFIX + testId, null) ?: return null
        return gson.fromJson(json, TestProgress::class.java)
    }

    fun clearTestProgress(testId: String) {
        prefs.edit().remove(KEY_PROGRESS_PREFIX + testId).apply()
    }
}
