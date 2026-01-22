package com.rajasthanexams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rajasthanexams.data.Question
import com.rajasthanexams.ui.components.AppBottomNavigation
import com.rajasthanexams.ui.screens.ExamDetailScreen
import com.rajasthanexams.ui.screens.HomeScreen
import com.rajasthanexams.ui.screens.LoginScreen
import com.rajasthanexams.ui.screens.PracticeScreen
import com.rajasthanexams.ui.screens.ProfileScreen
import com.rajasthanexams.ui.screens.RankersScreen
import com.rajasthanexams.ui.screens.ResultScreen
import com.rajasthanexams.ui.screens.TestTypeScreen
//import com.rajasthanexams.ui.screens.ResultScreen
import com.rajasthanexams.ui.screens.SplashScreen
import com.rajasthanexams.data.TestType
import com.rajasthanexams.ui.theme.RajasthanExamsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Global Crash Handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.app.AlertDialog.Builder(this)
                    .setTitle("App Crash")
                    .setMessage(throwable.stackTraceToString())
                    .setPositiveButton("Close") { _, _ -> finish() }
                    .show()
            }
        }

        com.rajasthanexams.data.remote.RetrofitClient.init(this)
        com.rajasthanexams.data.OfflineManager.init(this)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) } // Default to light
            var isUiHindi by remember { mutableStateOf(false) } // UI Default to English
            
            RajasthanExamsTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    isUiHindi = isUiHindi,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onToggleLanguage = { isUiHindi = !isUiHindi }
                )
            }
        }
    }
}

enum class Screen {
    SPLASH, LOGIN, HOME, TESTS, RANKERS, COMMUNITY, PROFILE, DETAIL, PRACTICE, RESULT, TEST_TYPES, NOTIFICATIONS, BOOKMARKS, REFERRAL, CURRENT_AFFAIRS, PERFORMANCE, TEST_HISTORY, PRIVACY_POLICY
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    isUiHindi: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var selectedExam by remember { mutableStateOf("") }
    var selectedTestId by remember { mutableStateOf("") } // Store ID for API
    var selectedCategory by remember { mutableStateOf("") }
    var selectedTestType by remember { mutableStateOf(TestType.MOCK) }
    var quizScore by remember { mutableStateOf(0) }
    var quizTotalQuestions by remember { mutableStateOf(0) }
    var quizQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var quizUserAnswers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var unreadNotificationCount by remember { mutableStateOf(2) } 
    
    var selectedExamId by remember { mutableStateOf("") }
    
    val showBottomBar = currentScreen in listOf(Screen.HOME, Screen.TESTS, Screen.RANKERS, Screen.COMMUNITY, Screen.PROFILE)
    val context = androidx.compose.ui.platform.LocalContext.current
    
    androidx.compose.runtime.LaunchedEffect(currentScreen) {
        val window = (context as? android.app.Activity)?.window
        if (currentScreen == Screen.PRACTICE) {
            window?.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        val modifier = if (showBottomBar) Modifier.padding(paddingValues) else Modifier

        when (currentScreen) {
            Screen.SPLASH -> {
                SplashScreen {
                    val sessionManager = com.rajasthanexams.data.local.SessionManager(context)
                    if (sessionManager.getAuthToken() != null) {
                        currentScreen = Screen.HOME
                    } else {
                        currentScreen = Screen.LOGIN
                    }
                }
            }
            Screen.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = {
                        currentScreen = Screen.HOME
                    }
                )
            }
            Screen.HOME -> {
                HomeScreen(
                    isDarkTheme = isDarkTheme,
                    onExamClick = { test ->
                        selectedExam = test.title
                        selectedTestId = test.id
                        selectedExamId = test.id // If mapping exam->test directly
                        currentScreen = Screen.DETAIL
                    },
                    onCategoryClick = { category ->
                        selectedCategory = category.title 
                        selectedExamId = category.id
                        currentScreen = Screen.TEST_TYPES
                    },
                    onNotificationClick = {
                        unreadNotificationCount = 0
                        currentScreen = Screen.NOTIFICATIONS
                    },
                    onReferralClick = {
                        currentScreen = Screen.REFERRAL
                    },
                    onCurrentAffairsClick = {
                        currentScreen = Screen.CURRENT_AFFAIRS
                    },
                    unreadNotifications = unreadNotificationCount
                )
            }
            Screen.TESTS -> {
                HomeScreen(
                    isDarkTheme = isDarkTheme,
                    onExamClick = { test ->
                        selectedExam = test.title
                        selectedTestId = test.id
                        selectedExamId = test.id // Assuming explicit selection logic
                        currentScreen = Screen.DETAIL
                    },
                    onCategoryClick = { category ->
                        selectedCategory = category.title
                        selectedExamId = category.id
                        currentScreen = Screen.TEST_TYPES
                    },
                    onNotificationClick = {
                        unreadNotificationCount = 0
                        currentScreen = Screen.NOTIFICATIONS
                    },
                    onReferralClick = {
                         currentScreen = Screen.REFERRAL
                    },
                    onCurrentAffairsClick = {
                        currentScreen = Screen.CURRENT_AFFAIRS
                    },
                    unreadNotifications = unreadNotificationCount
                )
            }
            Screen.TEST_TYPES -> {
                TestTypeScreen(
                    categoryName = selectedCategory,
                    onBackClick = { currentScreen = Screen.HOME },
                    onTypeSelect = { type ->
                        selectedTestType = type
                        selectedExam = selectedCategory 
                        currentScreen = Screen.DETAIL
                    }
                )
            }
            Screen.RANKERS -> {
                RankersScreen(modifier = Modifier.padding(paddingValues))
            }
            Screen.COMMUNITY -> {
                com.rajasthanexams.ui.screens.CommunityScreen()
            }
            Screen.PROFILE -> {
                ProfileScreen(
                    isDark = isDarkTheme,
                    isHindi = isUiHindi,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onBookmarksClick = { currentScreen = Screen.BOOKMARKS },
                    onReferralClick = { currentScreen = Screen.REFERRAL },
                    onPerformanceClick = { currentScreen = Screen.PERFORMANCE },
                    onTestHistoryClick = { currentScreen = Screen.TEST_HISTORY },
                    onPrivacyClick = { currentScreen = Screen.PRIVACY_POLICY }
                )
            }
            Screen.DETAIL -> {
                ExamDetailScreen(
                    examId = selectedExamId,
                    examName = selectedExam,
                    testType = selectedTestType,
                    onBackClick = { currentScreen = Screen.HOME },
                    onStartPractice = { testId -> 
                        selectedTestId = testId
                        currentScreen = Screen.PRACTICE 
                    }
                )
            }
            Screen.PRACTICE -> {
                PracticeScreen(
                    testId = selectedTestId,
                    isUiHindi = isUiHindi,
                    isPracticeMode = selectedTestType == TestType.TOPIC,
                    onFinish = { score, total, questions, answers -> 
                        quizScore = score
                        quizTotalQuestions = total
                        quizQuestions = questions
                        quizUserAnswers = answers
                        currentScreen = Screen.RESULT 
                    }
                )
            }
            Screen.RESULT -> {
                ResultScreen(
                    score = quizScore,
                    totalQuestions = quizTotalQuestions,
                    questions = quizQuestions,
                    userAnswers = quizUserAnswers,
                    isUiHindi = isUiHindi,
                    onHomeClick = { currentScreen = Screen.HOME },
                    onRetakeClick = { currentScreen = Screen.PRACTICE }
                )
            }
            Screen.NOTIFICATIONS -> {
                com.rajasthanexams.ui.screens.NotificationScreen(
                    onBackClick = { currentScreen = Screen.HOME }
                )
            }
            Screen.BOOKMARKS -> {
                com.rajasthanexams.ui.screens.BookmarkScreen(
                    isHindi = isUiHindi,
                    onBackClick = { currentScreen = Screen.PROFILE }
                )
            }
            Screen.REFERRAL -> {
                 com.rajasthanexams.ui.screens.ReferralScreen(
                     onBackClick = { currentScreen = Screen.HOME }
                 )
            }
            Screen.CURRENT_AFFAIRS -> {
                com.rajasthanexams.ui.screens.CurrentAffairsScreen(
                    isHindi = isUiHindi,
                    onBackClick = { currentScreen = Screen.HOME }
                )
            }
            Screen.PERFORMANCE -> {
                com.rajasthanexams.ui.screens.PerformanceScreen(
                    isHindi = isUiHindi,
                    onBackClick = { currentScreen = Screen.PROFILE }
                )
            }
            Screen.TEST_HISTORY -> {
                com.rajasthanexams.ui.screens.TestHistoryScreen(
                    isHindi = isUiHindi,
                    onBackClick = { currentScreen = Screen.PROFILE }
                )
            }
            Screen.PRIVACY_POLICY -> {
                com.rajasthanexams.ui.screens.PrivacyPolicyScreen(
                    onBackClick = { currentScreen = Screen.PROFILE }
                )
            }
        }
    }
}
