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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collect
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
import com.rajasthanexams.ui.screens.LiveTestsScreen
//import com.rajasthanexams.ui.screens.ResultScreen
import com.rajasthanexams.ui.screens.SplashScreen
import com.rajasthanexams.ui.screens.UserInfoScreen
import com.rajasthanexams.data.TestType
import com.rajasthanexams.ui.theme.RajasthanExamsTheme

import com.razorpay.PaymentResultWithDataListener
import com.razorpay.PaymentData

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    // We need access to ViewModel to handle payment verification
    // Since ViewModel is scoped to composables, we can use a shared flow or static access, 
    // or just broadcast the result. 
    // Creating a static listener or using a Bus is common for this legacy callback style.
    // For simplicity, we can use a global event bus or just a simple static callback in PaymentManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... existing code ...
        
        com.rajasthanexams.data.remote.RetrofitClient.init(this)
        com.rajasthanexams.data.OfflineManager.init(this)
        setContent {
            val sessionManager = remember { com.rajasthanexams.data.local.SessionManager(this) }
            var isDarkTheme by remember { mutableStateOf(sessionManager.isDarkMode()) }
            var isUiHindi by remember { mutableStateOf(sessionManager.isUiHindi()) }

            RajasthanExamsTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    isUiHindi = isUiHindi,
                    onToggleTheme = {
                        isDarkTheme = !isDarkTheme
                        sessionManager.setDarkMode(isDarkTheme)
                    },
                    onToggleLanguage = {
                        isUiHindi = !isUiHindi
                        sessionManager.setUiHindi(isUiHindi)
                    },
                    activity = this@MainActivity
                )
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData?) {
        // Handle Success
        com.rajasthanexams.utils.PaymentManager.onPaymentSuccess(paymentData)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        // Handle Error
        com.rajasthanexams.utils.PaymentManager.onPaymentError(code, response ?: "Unknown Error")
    }
}

enum class Screen {
    SPLASH, LOGIN, HOME, TESTS, RANKERS, COMMUNITY, PROFILE, DETAIL, PRACTICE, RESULT, TEST_TYPES, TESTS_BY_TYPE, LIVE_TESTS, NOTIFICATIONS, USER_INFO, BOOKMARKS, REFERRAL, CURRENT_AFFAIRS, PERFORMANCE, TEST_HISTORY, PRIVACY_POLICY, DOWNLOADS, INSTRUCTIONS, EXAM_PURCHASE, POST_DETAIL
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    isUiHindi: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    activity: android.app.Activity
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var selectedExam by remember { mutableStateOf("") }
    var selectedTestId by remember { mutableStateOf("") } // Store ID for API
    var selectedCategory by remember { mutableStateOf("") }
    var selectedTestType by remember { mutableStateOf(TestType.MOCK) }
    var quizScore by remember { mutableStateOf(0.0) }
    var quizTotalQuestions by remember { mutableStateOf(0) }
    var quizTimeTaken by remember { mutableStateOf(0L) }
    var quizQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var quizUserAnswers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var quizTimePerQuestion by remember { mutableStateOf<Map<Int, Long>>(emptyMap()) }
    var quizCoinsEarned by remember { mutableStateOf(0) } // Added
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManagerNav = remember { com.rajasthanexams.data.local.SessionManager(context) }
    var unreadNotificationCount by remember { mutableStateOf(sessionManagerNav.getUnreadNotificationCount()) }

    // ViewModels
    val loginViewModel: com.rajasthanexams.ui.viewmodels.LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val homeViewModel: com.rajasthanexams.ui.viewmodels.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val communityViewModel: com.rajasthanexams.ui.viewmodels.CommunityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val testViewModel: com.rajasthanexams.ui.viewmodels.TestViewModel = androidx.lifecycle.viewmodel.compose.viewModel() 
    val testsByTypeViewModel: com.rajasthanexams.ui.viewmodels.TestsByTypeViewModel = androidx.lifecycle.viewmodel.compose.viewModel() // For refreshing tests
    val notificationsViewModel: com.rajasthanexams.ui.viewmodels.NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    // ...


    var previousScreen by remember { mutableStateOf(Screen.HOME) }
    var isRetakeFlow by remember { mutableStateOf(false) } // Track explicit retake intent
    
    // Test Metadata for Instructions
    var selectedTestQuestions by remember { mutableStateOf(0) }
    var selectedTestDuration by remember { mutableStateOf(0) }
    var selectedTestMarks by remember { mutableStateOf(0.0) }
    var selectedTestNegative by remember { mutableStateOf(0.0) }
    
    var selectedExamId by remember { mutableStateOf("") }
    
    // Purchase Flow State
    var purchaseExamId by remember { mutableStateOf("") }
    var purchaseTitle by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf(0.0) }
    var purchaseDiscount by remember { mutableStateOf(0) }  // ← from backend discountPercent
    
    val showBottomBar = currentScreen in listOf(Screen.HOME, Screen.TESTS, Screen.RANKERS, Screen.COMMUNITY, Screen.PROFILE)

    
    // Listen for Purchase Success (Moved here to access previousScreen)
    androidx.compose.runtime.LaunchedEffect(Unit) {
         homeViewModel.purchaseSuccess.collect {
              if (currentScreen == Screen.EXAM_PURCHASE) {
                  testsByTypeViewModel.refreshData()
                  if (previousScreen != Screen.EXAM_PURCHASE && previousScreen != Screen.SPLASH) {
                      currentScreen = previousScreen
                  } else {
                      selectedTestType = TestType.MOCK
                      currentScreen = Screen.TESTS_BY_TYPE
                  }
              }
         }
    }
    
    
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
    

    // Listen for Session Expiry
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.rajasthanexams.data.remote.RetrofitClient.logoutEvent.collect {
            currentScreen = Screen.LOGIN
        }
    }

    // Listen for Login Success and New User State
    val loginState by loginViewModel.uiState.collectAsState()
    val isNewUser by loginViewModel.isNewUser.collectAsState()

    androidx.compose.runtime.LaunchedEffect(loginState, isNewUser) {
        if (loginState is com.rajasthanexams.ui.viewmodels.LoginUiState.LoggedIn) {
             if (isNewUser) {
                 currentScreen = Screen.USER_INFO
             } else {
                 if (currentScreen == Screen.LOGIN || currentScreen == Screen.USER_INFO) {
                    currentScreen = Screen.HOME 
                 }
             }
        }
    }

    // Listen for Purchase Success to Navigate away from Purchase Screen


     // Global Back Handler
    androidx.activity.compose.BackHandler(enabled = currentScreen != Screen.HOME) {
        when (currentScreen) {
            Screen.SPLASH, Screen.LOGIN -> {
                (context as? android.app.Activity)?.finish()
            }
            Screen.HOME -> {
                (context as? android.app.Activity)?.finish()
            }
            // Top Level Destinations -> Home
            Screen.TESTS, Screen.COMMUNITY, Screen.PROFILE -> {
                currentScreen = Screen.HOME
            }
            Screen.RANKERS -> {
                 if (previousScreen == Screen.RESULT) {
                     currentScreen = Screen.RESULT
                 } else {
                     currentScreen = Screen.HOME
                 }
            }
            // Second Level -> Home or Parent
            Screen.TEST_TYPES -> currentScreen = if (previousScreen == Screen.TESTS) Screen.TESTS else Screen.HOME
            Screen.TESTS_BY_TYPE -> currentScreen = Screen.HOME
            Screen.LIVE_TESTS -> currentScreen = Screen.HOME
            Screen.DETAIL -> currentScreen = Screen.TEST_TYPES
            Screen.NOTIFICATIONS -> currentScreen = Screen.HOME
            Screen.EXAM_PURCHASE -> {
                if (previousScreen != Screen.EXAM_PURCHASE) {
                    currentScreen = previousScreen
                } else {
                    currentScreen = Screen.HOME
                }
            }
            
            // Third Level / Features
            Screen.POST_DETAIL -> currentScreen = Screen.COMMUNITY
            
            // Profile Children -> Profile
            Screen.BOOKMARKS, Screen.REFERRAL, Screen.CURRENT_AFFAIRS, 
            Screen.PERFORMANCE, Screen.TEST_HISTORY, Screen.PRIVACY_POLICY, 
            Screen.DOWNLOADS, Screen.USER_INFO -> {
                currentScreen = Screen.PROFILE
            }
            
            // Test Flow
            Screen.INSTRUCTIONS -> {
                 if (previousScreen != Screen.INSTRUCTIONS && previousScreen != Screen.PRACTICE) {
                     currentScreen = previousScreen
                 } else {
                     currentScreen = Screen.HOME
                 }
            }
            Screen.PRACTICE -> {
                // If PracticeScreen didn't consume it (e.g. Review Mode), go back
                 if (previousScreen != Screen.PRACTICE) {
                     currentScreen = previousScreen
                 } else {
                     currentScreen = Screen.HOME
                 }
            }
            Screen.RESULT -> {
                if (previousScreen != Screen.RESULT && previousScreen != Screen.PRACTICE && previousScreen != Screen.INSTRUCTIONS) {
                    currentScreen = previousScreen
                } else {
                    currentScreen = Screen.HOME
                }
            }
            
            else -> currentScreen = Screen.HOME
        }
    }

    Scaffold(
        bottomBar = {
             if (showBottomBar) {
                AppBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { 
                        if (it == Screen.RANKERS) {
                            previousScreen = Screen.HOME
                            selectedTestId = "" // Clear test context for Global Leaderboard tab
                        }
                        currentScreen = it 
                    }
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
                         // Check if name is missing (redundant safety check, usually backend handles isNewUser on login)
                        if (sessionManager.getUserName().isNullOrBlank()) {
                             // If token exists but no name, maybe force profile update?
                             // For now, let's just go Home, user can update via Profile
                             currentScreen = Screen.HOME
                        } else {
                             currentScreen = Screen.HOME
                        }
                    } else {
                        currentScreen = Screen.LOGIN
                    }
                }
            }
            Screen.LOGIN -> {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                       // Handled by LaunchedEffect
                    }
                )
            }
            Screen.USER_INFO -> {
                val sessionManager = androidx.compose.runtime.remember { com.rajasthanexams.data.local.SessionManager(context) }
                val initialName = sessionManager.getUserName()
                val initialEmail = sessionManager.getUserEmail()
                val initialProfilePicture = sessionManager.getProfilePicture()
                
                UserInfoScreen(
                    viewModel = loginViewModel,
                    initialName = initialName,
                    initialEmail = initialEmail,
                    initialProfilePicture = initialProfilePicture,
                    onBackClick = if (!isNewUser) { { currentScreen = Screen.PROFILE } } else null,
                    onContinueClick = {
                        if (isNewUser) {
                            currentScreen = Screen.HOME
                        } else {
                            currentScreen = Screen.PROFILE 
                        }
                    }
                )
            }
            Screen.HOME -> {
                HomeScreen(
                    isDarkTheme = isDarkTheme,
                    profileImageUrl = sessionManagerNav.getProfilePicture(),
                    viewModel = homeViewModel,
                    notificationsViewModel = notificationsViewModel,
                    onExamClick = { test ->
                        val hasProgress = com.rajasthanexams.data.OfflineManager.getTestProgress(test.id) != null
                        if (test.isPremium && !test.isPurchased && !test.isAttempted && !hasProgress) {
                             // Use examId if available, otherwise fallback to id (though likely wrong for tests)
                             val idToBuy = if (test.examId.isNotEmpty()) test.examId else test.id
                             purchaseExamId = idToBuy
                             purchaseTitle = test.title
                             purchasePrice = test.price
                             previousScreen = Screen.HOME
                             currentScreen = Screen.EXAM_PURCHASE
                        } else {
                            selectedExam = test.title
                            selectedTestId = test.id
                            selectedTestType = test.type
                            isRetakeFlow = false // Reset
                            
                            selectedTestQuestions = test.questions
                            selectedTestDuration = test.time
                            selectedTestMarks = test.totalMarks ?: (test.questions * test.marksPerQuestion)
                            selectedTestNegative = test.negativeMarks
                            
                            // Direct to Practice/Instructions for specific tests
                            val progress = com.rajasthanexams.data.OfflineManager.getTestProgress(test.id)
                            if (progress != null) {
                                previousScreen = Screen.HOME
                                currentScreen = Screen.PRACTICE
                            } else if (test.isAttempted) {
                                // Load Result Data for this specific test
                                testViewModel.loadLocalResult(test.id) { score, total, questions, answers, timeTaken, timeStats, coinsEarned ->
                                    quizScore = score
                                    quizTotalQuestions = total
                                    quizQuestions = questions
                                    quizUserAnswers = answers
                                    quizTimeTaken = timeTaken
                                    quizTimePerQuestion = timeStats
                                    quizCoinsEarned = coinsEarned
                                    
                                    previousScreen = Screen.HOME
                                    currentScreen = Screen.RESULT
                                }
                            } else {
                                previousScreen = Screen.HOME
                                currentScreen = Screen.INSTRUCTIONS
                            }
                        }
                    },
                    onTestTypeClick = { type ->
                        selectedTestType = type
                        if (type == TestType.LIVE) {
                            currentScreen = Screen.LIVE_TESTS
                        } else {
                            currentScreen = Screen.TESTS_BY_TYPE
                        }
                    },
                    onCategoryClick = { category ->
                         if (category.isPremium && !category.isPurchased) {
                             purchaseExamId = category.id
                             purchaseTitle = category.title
                             purchasePrice = category.price
                             purchaseDiscount = category.discountPercent   // ← read from API
                             previousScreen = Screen.HOME
                             currentScreen = Screen.EXAM_PURCHASE
                         } else {
                             selectedCategory = category.title 
                             selectedExamId = category.id
                             currentScreen = Screen.TEST_TYPES
                         }
                    },
                    onNotificationClick = {
                        unreadNotificationCount = 0
                        sessionManagerNav.saveUnreadNotificationCount(0)
                        currentScreen = Screen.NOTIFICATIONS
                    },
                    onReferralClick = {
                        currentScreen = Screen.REFERRAL
                    },
                    onCurrentAffairsClick = {
                        currentScreen = Screen.CURRENT_AFFAIRS
                    }
                )
            }
            Screen.TESTS ->
                com.rajasthanexams.ui.screens.MyTestsScreen(
                    onBrowseClick = { currentScreen = Screen.HOME },
                    onExamClick = { exam ->
                        selectedCategory = exam.title
                        selectedExamId = exam.id
                        previousScreen = Screen.TESTS
                        currentScreen = Screen.TEST_TYPES
                    },
                    onTestClick = { test ->
                        val hasProgress = com.rajasthanexams.data.OfflineManager.getTestProgress(test.id) != null
                        selectedExam = test.title
                        selectedTestId = test.id
                        selectedTestType = test.type
                        isRetakeFlow = false
                        selectedTestQuestions = test.questions
                        selectedTestDuration = test.time
                        selectedTestMarks = test.totalMarks ?: (test.questions * test.marksPerQuestion)
                        selectedTestNegative = test.negativeMarks

                        val progress = com.rajasthanexams.data.OfflineManager.getTestProgress(test.id)
                        if (progress != null) {
                            previousScreen = Screen.TESTS
                            currentScreen = Screen.PRACTICE
                        } else if (test.isAttempted) {
                            testViewModel.loadLocalResult(test.id) { score, total, questions, answers, timeTaken, timeStats, coinsEarned ->
                                quizScore = score
                                quizTotalQuestions = total
                                quizQuestions = questions
                                quizUserAnswers = answers
                                quizTimeTaken = timeTaken
                                quizTimePerQuestion = timeStats
                                quizCoinsEarned = coinsEarned
                                previousScreen = Screen.TESTS
                                currentScreen = Screen.RESULT
                            }
                        } else {
                            previousScreen = Screen.TESTS
                            currentScreen = Screen.INSTRUCTIONS
                        }
                    }
                )
            Screen.TEST_TYPES -> {
                TestTypeScreen(
                    categoryName = selectedCategory,
                    onBackClick = { currentScreen = if (previousScreen == Screen.TESTS) Screen.TESTS else Screen.HOME },
                    onTypeSelect = { type ->
                        selectedTestType = type
                        selectedExam = selectedCategory
                        currentScreen = Screen.DETAIL
                    }
                )
            }
            Screen.TESTS_BY_TYPE -> {
                com.rajasthanexams.ui.screens.TestsByTypeScreen(
                    testType = selectedTestType,
                    onBackClick = { currentScreen = Screen.HOME },
                    onStartPractice = { test ->
                        if (test.isPremium && !test.isPurchased) {
                             val idToBuy = if (test.examId.isNotEmpty()) test.examId else test.id
                             purchaseExamId = idToBuy
                             purchaseTitle = test.title
                             purchasePrice = test.price
                             previousScreen = Screen.TESTS_BY_TYPE
                             currentScreen = Screen.EXAM_PURCHASE
                        } else {
                            selectedTestId = test.id
                            selectedTestQuestions = test.questions
                            selectedTestDuration = test.time
                            selectedTestMarks = test.totalMarks ?: (test.questions * test.marksPerQuestion)
                            selectedTestNegative = test.negativeMarks
                            isRetakeFlow = false // Reset
                            
                            val progress = com.rajasthanexams.data.OfflineManager.getTestProgress(test.id)
                            if (progress != null) {
                                previousScreen = Screen.TESTS_BY_TYPE
                                currentScreen = Screen.PRACTICE
                            } else if (test.isAttempted) {
                                // Load Result Data for this specific test
                                testViewModel.loadLocalResult(test.id) { score, total, questions, answers, timeTaken, timeStats, coinsEarned ->
                                    quizScore = score
                                    quizTotalQuestions = total
                                    quizQuestions = questions
                                    quizUserAnswers = answers
                                    quizTimeTaken = timeTaken
                                    quizTimePerQuestion = timeStats
                                    quizCoinsEarned = coinsEarned
                                    
                                    previousScreen = Screen.TESTS_BY_TYPE
                                    currentScreen = Screen.RESULT
                                }
                            } else {
                                previousScreen = Screen.TESTS_BY_TYPE
                                currentScreen = Screen.INSTRUCTIONS
                            }
                        }
                    }
                )
            }
            Screen.LIVE_TESTS -> {
                LiveTestsScreen(
                    onBackClick = { currentScreen = Screen.HOME },
                    onStartPractice = { test ->
                        selectedTestId = test.id
                        selectedExam = test.title
                        selectedTestType = test.type
                        isRetakeFlow = false
                        selectedTestQuestions = test.questions
                        selectedTestDuration = test.time
                        selectedTestMarks = test.totalMarks ?: (test.questions * test.marksPerQuestion)
                        selectedTestNegative = test.negativeMarks
                        previousScreen = Screen.LIVE_TESTS
                        currentScreen = Screen.INSTRUCTIONS
                    }
                )
            }
            Screen.RANKERS -> {
                RankersScreen(
                    testId = selectedTestId,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Screen.COMMUNITY -> {
                com.rajasthanexams.ui.screens.CommunityScreen(
                    modifier = modifier,
                    viewModel = communityViewModel,
                    onPostClick = { post ->
                        communityViewModel.selectPost(post)
                        currentScreen = Screen.POST_DETAIL
                    }
                )
            }
            Screen.POST_DETAIL -> {
                 // We don't have a NavController here, so we pass a lambda or context to PostDetailScreen if needed
                 // But PostDetailScreen expects NavController in my implementation.
                 // I should Refactor PostDetailScreen to take onBackClick instead of NavController.
                 // OR I can't instantiate NavController here easily since I am doing manual nav.
                 // Let's Refactor PostDetailScreen to take onBackClick.
                 com.rajasthanexams.ui.screens.PostDetailScreen(
                     viewModel = communityViewModel,
                     onBackClick = { currentScreen = Screen.COMMUNITY } // Or previous screen
                 )
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
                    onDownloadsClick = { currentScreen = Screen.DOWNLOADS },
                    onPrivacyClick = { currentScreen = Screen.PRIVACY_POLICY },
                    onEditProfileClick = { currentScreen = Screen.USER_INFO }
                )
            }
            Screen.DETAIL -> {
                ExamDetailScreen(
                    examId = selectedExamId,
                    examName = selectedExam,
                    testType = selectedTestType,
                    onBackClick = { currentScreen = Screen.HOME },
                    onStartPractice = { test -> 
                         if (test.isPremium && !test.isPurchased) {
                             val idToBuy = if (test.examId.isNotEmpty()) test.examId else test.id
                             purchaseExamId = idToBuy
                             purchaseTitle = test.title
                             purchasePrice = test.price
                             previousScreen = Screen.DETAIL
                             currentScreen = Screen.EXAM_PURCHASE
                         } else {
                            selectedTestId = test.id
                            selectedTestQuestions = test.questions
                            selectedTestDuration = test.time
                            selectedTestMarks = test.totalMarks ?: (test.questions * test.marksPerQuestion)
                            selectedTestNegative = test.negativeMarks
                            isRetakeFlow = false // Reset
                            
                            val progress = com.rajasthanexams.data.OfflineManager.getTestProgress(test.id)
                            if (progress != null) {
                                previousScreen = Screen.DETAIL
                                currentScreen = Screen.PRACTICE
                            } else if (test.isAttempted) {
                                // Load Result Data for this specific test
                                testViewModel.loadLocalResult(test.id) { score, total, questions, answers, timeTaken, timeStats, coinsEarned ->
                                    quizScore = score
                                    quizTotalQuestions = total
                                    quizQuestions = questions
                                    quizUserAnswers = answers
                                    quizTimeTaken = timeTaken
                                    quizTimePerQuestion = timeStats
                                    quizCoinsEarned = coinsEarned
                                    
                                    previousScreen = Screen.DETAIL
                                    currentScreen = Screen.RESULT
                                }
                            } else {
                                previousScreen = Screen.DETAIL
                                currentScreen = Screen.INSTRUCTIONS
                            }
                         }
                    }
                )
            }
            Screen.PRACTICE -> {
                PracticeScreen(
                    testId = selectedTestId,
                    isUiHindi = isUiHindi,
                    isPracticeMode = selectedTestType == TestType.TOPIC,
                    onFinish = { score, total, questions, answers, timeTaken, timeStats, coinsEarned ->
                        quizScore = score
                        quizTotalQuestions = total
                        quizQuestions = questions
                        quizUserAnswers = answers
                        quizTimeTaken = timeTaken
                        quizTimePerQuestion = timeStats
                        quizCoinsEarned = coinsEarned
                        currentScreen = Screen.RESULT 
                    },
                    onPause = {
                        currentScreen = previousScreen // Navigate to where we came from
                    }
                )
            }
            Screen.RESULT -> {
                ResultScreen(
                    score = quizScore,
                    totalQuestions = quizTotalQuestions,
                    questions = quizQuestions,
                    userAnswers = quizUserAnswers,
                    timeTaken = quizTimeTaken,
                    timePerQuestion = quizTimePerQuestion,
                    coinsEarned = quizCoinsEarned, // Added
                    isUiHindi = isUiHindi,
                    onHomeClick = { currentScreen = Screen.HOME },
                    onRetakeClick = { 
                        // Navigate to Instructions for consistency with other exams
                        // Data clearing will happen when "Start Test" is clicked in Instructions
                        isRetakeFlow = true // Explicit retake intent
                        previousScreen = Screen.RESULT
                        currentScreen = Screen.INSTRUCTIONS
                    },
                    onLeaderboardClick = {
                         // Should not be needed if internal nav works, but kept for compatibility
                        previousScreen = Screen.RESULT 
                        currentScreen = Screen.RANKERS 
                    },
                    testId = selectedTestId // Pass selected Test ID
                )
            }
            Screen.NOTIFICATIONS -> {
                com.rajasthanexams.ui.screens.NotificationScreen(
                    viewModel = notificationsViewModel,
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
            Screen.INSTRUCTIONS -> {
                com.rajasthanexams.ui.screens.InstructionScreen(
                    testId = selectedTestId,
                    testTitle = selectedExam,
                    totalQuestions = selectedTestQuestions,
                    totalMarks = selectedTestMarks,
                    durationMinutes = selectedTestDuration,
                    markingScheme = "${selectedTestNegative}", // e.g. "0.33"
                    isUiHindi = isUiHindi,
                    onBackClick = { currentScreen = Screen.HOME },
                    onStartTest = { 
                        // Clear data if we are restarting an attempted test (Retake flow)
                        if (isRetakeFlow || com.rajasthanexams.data.OfflineManager.isTestAttempted(selectedTestId)) {
                            com.rajasthanexams.data.OfflineManager.clearTestUserData(selectedTestId)
                            com.rajasthanexams.data.OfflineManager.setRetakeMode(selectedTestId, true)
                        }
                        // previousScreen for PRACTICE is already set when entering INSTRUCTIONS (e.g. from HOME/TESTS)
                        // However, if we came from RESULT (Retake), previousScreen is RESULT.
                        // We want PRACTICE to return to RESULT on pause? Or back to Start?
                        // If we pause Retake, usually we go back to instructions.
                        // But let's trust previousScreen logic in BackHandler of PRACTICE.
                        currentScreen = Screen.PRACTICE 
                    }
                )
            }
            Screen.DOWNLOADS -> {
                com.rajasthanexams.ui.screens.DownloadsScreen(
                    isHindi = isUiHindi,
                    onBackClick = { currentScreen = Screen.PROFILE },
                    onTestClick = { testId -> 
                        selectedTestId = testId
                        isRetakeFlow = false // Reset
                        currentScreen = Screen.INSTRUCTIONS 
                    }
                )
            }
            Screen.EXAM_PURCHASE -> {
                val sessionManager = remember { com.rajasthanexams.data.local.SessionManager(context) }
                com.rajasthanexams.ui.screens.ExamPurchaseScreen(
                    examTitle = purchaseTitle,
                    examPrice = purchasePrice,
                    discountPercent = if (purchaseDiscount > 0) purchaseDiscount else 0,
                    userCoins = sessionManager.getCoins() ?: 0,
                    onBackClick = {
                        if (previousScreen != Screen.EXAM_PURCHASE) {
                            currentScreen = previousScreen
                        } else {
                            currentScreen = Screen.HOME
                        }
                    },
                    onBuyClick = { useCoins ->
                        val finalPrice = if (purchaseDiscount > 0)
                            kotlin.math.round(purchasePrice * (1.0 - purchaseDiscount / 100.0)).toDouble()
                        else
                            purchasePrice
                        homeViewModel.initiatePurchase(context, purchaseExamId, finalPrice, useCoins)
                    }
                )
            }
        }
    }
}
