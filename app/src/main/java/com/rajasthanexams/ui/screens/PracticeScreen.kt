package com.rajasthanexams.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // Removed incompatible import
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause

import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import com.rajasthanexams.ui.components.ReportDialog
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.ui.components.AppButton
import androidx.compose.foundation.verticalScroll
import com.rajasthanexams.data.MockData
import com.rajasthanexams.ui.components.HeritagePatternBackground

import com.rajasthanexams.data.Question
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.rajasthanexams.ui.viewmodels.TestViewModel
import com.rajasthanexams.ui.viewmodels.TestUiState
import androidx.compose.runtime.LaunchedEffect
import com.rajasthanexams.data.TestType

import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PracticeScreen(
    testId: String,
    isUiHindi: Boolean, // Global UI Language (Default English)
    // onToggleLanguage removed, managed locally for content
    isPracticeMode: Boolean = false,
    viewModel: TestViewModel = viewModel(),
    onFinish: (Double, Int, List<Question>, Map<Int, Int>, Long, Map<Int, Long>, Int) -> Unit,
    onPause: () -> Unit
) {
    // 1. Live Data
    val uiState by viewModel.uiState.collectAsState()
    
    // Fetch on Launch
    LaunchedEffect(testId) {
        viewModel.fetchQuestions(testId)
    }

    // 2. Load Questions & Test from State
    val questions = remember(uiState) {
        if (uiState is TestUiState.Success) (uiState as TestUiState.Success).questions else emptyList()
    }
    val test = remember(uiState) {
        if (uiState is TestUiState.Success) (uiState as TestUiState.Success).test else null
    }
    val totalQuestions = questions.size

    // Section/Subject Handling
    val subjects = remember(questions) {
        questions.map { it.subject ?: if(isUiHindi) "सामान्य" else "General" }.distinct()
    }
    var selectedTabIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }



    // Loading State
    if (uiState is TestUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    if (uiState is TestUiState.Error) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Text("Error: ${(uiState as TestUiState.Error).message}")
        }
        return
    }

    // 2. Local State
    var currentQuestionIndex by remember(testId) { mutableStateOf(0) }
    var showResumeDialog by remember(testId) { mutableStateOf(false) }
    // Remove simplistic selectedOption, use draft map for Pager compatibility
    val draftSelections = remember(testId) { mutableStateMapOf<Int, Int>() }
    // var selectedOption by remember { mutableStateOf<Int?>(null) } // Removed
    val selectedAnswers = remember(testId) { mutableStateMapOf<Int, Int>() } 

    val pagerState = rememberPagerState(pageCount = { totalQuestions })
    val scope = rememberCoroutineScope()

    // Sync Pager -> Current Index (Swipe)
    LaunchedEffect(pagerState.currentPage) {
        if (currentQuestionIndex != pagerState.currentPage) {
            currentQuestionIndex = pagerState.currentPage
        }
    }
    
    // Sync Pager -> Tab
    LaunchedEffect(pagerState.currentPage) {
        val currentQ = questions.getOrNull(pagerState.currentPage)
        val currentSub = currentQ?.subject ?: if(isUiHindi) "सामान्य" else "General"
        val index = subjects.indexOf(currentSub)
        if (index != -1 && index != selectedTabIndex) {
            selectedTabIndex = index
        }
    }

    // Sync Current Index -> Pager (Button Click)
    LaunchedEffect(currentQuestionIndex) {
        if (pagerState.currentPage != currentQuestionIndex) {
            pagerState.animateScrollToPage(currentQuestionIndex)
        }
    }

    // Mark for Review State (Local only for duration of test)
    val markedQuestions = remember(testId) { androidx.compose.runtime.mutableStateListOf<Int>() } 
    // Visited State
    val visitedQuestions = remember(testId) { androidx.compose.runtime.mutableStateListOf<Int>() }

    // Track Visited
    // Track Visited
    LaunchedEffect(currentQuestionIndex) {
        if (!visitedQuestions.contains(currentQuestionIndex)) {
            visitedQuestions.add(currentQuestionIndex)
        }
    }

    // Time Tracking per Question
    // Map: Question Index -> Time in Milliseconds
    val timeSpentPerQuestion = remember(testId) { mutableStateMapOf<Int, Long>() }
    var questionStartTime by remember(testId) { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }

    // Logic: When currentQuestionIndex changes (or screen pauses), accumulation happens.
    // We use a separate side-effect to track duration.
    androidx.compose.runtime.DisposableEffect(currentQuestionIndex) {
        val capturedIndex = currentQuestionIndex
        questionStartTime = System.currentTimeMillis()
        onDispose {
            val endTime = System.currentTimeMillis()
            val duration = endTime - questionStartTime
            
            // Accumulate time
            val currentTotal = timeSpentPerQuestion.getOrDefault(capturedIndex, 0L)
            timeSpentPerQuestion[capturedIndex] = currentTotal + duration
        }
    }

    var timeLeft by remember(testId) { mutableStateOf(10 * 60) } 
    var showSolution by remember { mutableStateOf(false) }
    
    // Content Language State (Default: Hindi)
    var isContentHindi by remember { mutableStateOf(true) }

    // Check Attempt Status
    // Strict Logic: Only LIVE tests effectively use "Attempted/Review" mode on entry.
    // Mocks/Topic/PYQ always open in "New Attempt" mode (Retake) as per user preference.
    val isAttemptedGlobal = (test?.isAttempted == true) && (test?.isLive == true)
    
    val isReviewMode = isAttemptedGlobal || isPracticeMode

    // Load Saved Answers if Attempted (Only for Live Tests now)
    LaunchedEffect(isAttemptedGlobal) {
        if (isAttemptedGlobal) {
             val savedAnswers = com.rajasthanexams.data.OfflineManager.getUserAnswers(testId)
             // Map from ID (String) back to Index (Int)
             // This requires efficient lookup. List is small < 200 usually.
             savedAnswers.forEach { (qId, optionIndex) ->
                 val index = questions.indexOfFirst { it.id == qId }
                 if (index != -1) {
                     selectedAnswers[index] = optionIndex
                 }
             }
        }
    }

    // Update Timer from Test Rules
    LaunchedEffect(test) {
        if (test != null && !isAttemptedGlobal) { // Only run timer if NOT attempted
            // User requested to show only Duration based timer, ignoring EndsAt window calculation
            timeLeft = test.time * 60
        } else if (isAttemptedGlobal) {
             timeLeft = 0 // No timer in review
        }
    }

    // 3. Timer Effect
    androidx.compose.runtime.LaunchedEffect(key1 = isReviewMode, key2 = timeLeft, key3 = showResumeDialog) { 
        if (!isReviewMode && timeLeft > 0 && !showResumeDialog) {
            kotlinx.coroutines.delay(1000L)
            timeLeft--
            
            if (timeLeft == 0) {
                 // Auto-submit logic
                val totalTime = if (test != null) test.time * 60 else 10 * 60
                val timeTaken = (totalTime - timeLeft).toLong()
                val answersMap = selectedAnswers.mapKeys { (index, _) -> questions[index].id }
                
                                viewModel.submitTest(testId, answersMap, timeTaken.toInt()) { score, total, _, coinsEarned ->
                                    com.rajasthanexams.data.OfflineManager.clearTestProgress(testId)
                                    onFinish(score, total, questions, selectedAnswers, timeTaken, timeSpentPerQuestion, coinsEarned)
                                }
            }
        }
    }

    // Helper to format time
    val formattedTime = remember(timeLeft) {
        val min = timeLeft / 60
        val sec = timeLeft % 60
        String.format("%02d:%02d", min, sec)
    }

    // Current Question
    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    // Calculate Progress
    // Calculate Progress
    val progress = if (totalQuestions > 0) (currentQuestionIndex + 1) / totalQuestions.toFloat() else 0f

    // 4. Question Palette (Bottom Sheet)
    var showQuestionPalette by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    
    if (showQuestionPalette) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showQuestionPalette = false },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text = if (isUiHindi) "प्रश्न पैलेट (Question Palette)" else "Question Palette",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 56.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false) 
                ) {
                    items(totalQuestions) { index ->
                        val isAttempted = selectedAnswers.containsKey(index)
                        val isMarked = markedQuestions.contains(index)
                        val isVisited = visitedQuestions.contains(index)
                        val isCurrent = index == currentQuestionIndex
                        
                        val isDraft = draftSelections.containsKey(index)
                        
                        // Color Logic:
                        // Saved + Marked -> Blue
                        // Saved -> Green
                        // Draft + Marked -> Light Blue
                        // Draft -> Light Green
                        // Marked -> Purple
                        // Visited -> Gray
                        
                        val startColor = when {
                            isAttempted && isMarked -> Color(0xFF2979FF) // Blue
                            isAttempted -> Color(0xFF4CAF50) // Green
                            isDraft && isMarked -> Color(0xFF64B5F6) // Light Blue (Draft+Marked)
                            isDraft -> Color(0xFF81C784) // Light Green (Draft)
                            isMarked -> Color(0xFF9C27B0) // Purple
                            isVisited -> Color.Gray 
                            else -> Color.White
                        }
                        
                        // Border logic
                        val borderC = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> Color.Gray
                        }
                        val borderWidth = if (isCurrent) 2.dp else 1.dp
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .background(startColor, androidx.compose.foundation.shape.CircleShape)
                                .border(
                                    width = borderWidth,
                                    color = borderC,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .clickable {
                                    scope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                    currentQuestionIndex = index
                                    // Update state for restoring answer
                                    // selectedOption = selectedAnswers[index] // Logic moved to rendering
                                    showSolution = (isReviewMode && selectedAnswers[index] != null && (test?.allowSolution != false)) || isAttemptedGlobal
                                    showQuestionPalette = false
                                }
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = if (isAttempted || isMarked || isVisited) Color.White else Color.Black,
                                fontWeight = if (isCurrent) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
                }
            } // Close LazyVerticalGrid
            Spacer(modifier = Modifier.height(24.dp))
                
                // Palette Legend
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Instructions:", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val legendItems = listOf(
                        Triple("Answered", Color(0xFF4CAF50), null),
                        Triple("Not Answered", Color.Gray, null), // Gray
                        Triple("Marked", Color(0xFF9C27B0), null),
                        Triple("Marked & Answered", Color(0xFF2979FF), null),
                        Triple("Not Visited", Color.White, Color.Gray)
                    )
                    
                    // Simple Grid for Legend
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                         columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                         horizontalArrangement = Arrangement.spacedBy(16.dp),
                         verticalArrangement = Arrangement.spacedBy(8.dp),
                         modifier = Modifier.height(150.dp) 
                    ) {
                        items(legendItems.size) { i ->
                            val (text, color, borderColor) = legendItems[i]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(color, CircleShape)
                                        .border(
                                            width = if (borderColor != null) 2.dp else 0.dp, 
                                            color = borderColor ?: Color.Transparent, 
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
        }
    }


    val context = androidx.compose.ui.platform.LocalContext.current
    if (showResumeDialog) {
        val attemptedCount = selectedAnswers.size
        val markedCount = markedQuestions.size
        val unattemptedCount = totalQuestions - attemptedCount
        
        com.rajasthanexams.ui.components.ResumeDialog(
            timeLeft = formattedTime,
            attempted = attemptedCount,
            unattempted = unattemptedCount,
            marked = markedCount,
            onResume = { 
                showResumeDialog = false 
            },
            onBack = {
                onPause()
            }
        )
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { type, desc ->
                showReportDialog = false
                // Call ViewModel
                // Current Question ID
                val currentQ = questions.getOrNull(pagerState.currentPage)
                if (currentQ != null) {
                    viewModel.reportQuestion(testId, currentQ.id, type, desc) { success ->
                        // Ideally show Toast
                        if (success) {
                            android.widget.Toast.makeText(context, if(isUiHindi) "Report Submitted" else "Report Submitted", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Failed to submit report. Please check connection.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }

    // 4. Question Persistence & Resume Logic
    // Load Saved State on Entry (if not AttemptedGlobal and not Live)
    var isResumed by remember(testId) { mutableStateOf(false) }
    
    LaunchedEffect(test) {
        if (test != null && !test.isLive && !isAttemptedGlobal && !isResumed) {
             val savedProgress = com.rajasthanexams.data.OfflineManager.getTestProgress(testId)
             if (savedProgress != null) {
                 timeLeft = savedProgress.timeLeft
                 
                 // Restore Answers
                 val savedAnswers = com.rajasthanexams.data.OfflineManager.getUserAnswers(testId)
                 savedAnswers.forEach { (qId, optionIndex) ->
                     val index = questions.indexOfFirst { it.id == qId }
                     if (index != -1) {
                         selectedAnswers[index] = optionIndex
                     }
                 }
                 
                 // Restore Marked
                 val savedMarkedIds = com.rajasthanexams.data.OfflineManager.getMarkedQuestions(testId)
                 savedMarkedIds.forEach { id ->
                     val idx = questions.indexOfFirst { it.id == id }
                     if (idx != -1 && !markedQuestions.contains(idx)) markedQuestions.add(idx)
                 }

                 // Restore Times
                 val savedTimes = com.rajasthanexams.data.OfflineManager.getTimeSpent(testId)
                 savedTimes.forEach { (id, time) ->
                     val idx = questions.indexOfFirst { it.id == id }
                     if (idx != -1) timeSpentPerQuestion[idx] = time
                 }

                 // Set Index AFTER restoring everything
                 currentQuestionIndex = savedProgress.currentQuestionIndex.coerceAtMost(totalQuestions - 1)
                 
                 isResumed = true
                 showResumeDialog = true // Trigger Dialog
             }
        }
    }

    // Reset Question Timer when dialog closes
    LaunchedEffect(showResumeDialog) {
        if (!showResumeDialog) {
             questionStartTime = System.currentTimeMillis()
        }
    }

    // Back Handler & Pause/Submit Dialog
    var showPauseDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    
    // Intercept back for both Live (Submit Confirm) and Practice (Pause)
    if (!isAttemptedGlobal) {
        androidx.activity.compose.BackHandler {
            if (test?.isLive == true || test?.type == TestType.LIVE) {
                showSubmitDialog = true
            } else {
                showPauseDialog = true
            }
        }
    }
    
    if (showSubmitDialog) {
         androidx.compose.ui.window.Dialog(onDismissRequest = { showSubmitDialog = false }) {
           Surface(
               shape = RoundedCornerShape(8.dp),
               color = Color.White,
               modifier = Modifier.fillMaxWidth().padding(16.dp)
           ) {
               Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                   Text(
                       text = if(isUiHindi) "क्या आप वाकई टेस्ट सबमिट करना चाहते हैं?" else "Are you sure you want to submit the test?",
                       style = MaterialTheme.typography.titleMedium,
                       fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                       color = Color.Black,
                       textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                       modifier = Modifier.padding(bottom = 24.dp)
                   )
                   
                   Row(
                       modifier = Modifier.fillMaxWidth(),
                       horizontalArrangement = Arrangement.spacedBy(16.dp)
                   ) {
                       // Yes Button (Blue)
                       androidx.compose.material3.Button(
                           onClick = {
                                // Submit Logic
                                val totalTimeSeconds = if (test != null) test.time * 60 else 10 * 60
                                val timeTaken = (totalTimeSeconds - timeLeft).toLong()
                                
                                // Merge Drafts -> Selected
                                draftSelections.forEach { (key, value) ->
                                    if (!selectedAnswers.containsKey(key)) {
                                        selectedAnswers[key] = value
                                    }
                                }
                                
                                val answersMap = selectedAnswers.mapKeys { (index, _) -> questions[index].id }
                                
                                viewModel.submitTest(testId, answersMap, timeTaken.toInt()) { score, total, _, coinsEarned ->
                                    com.rajasthanexams.data.OfflineManager.clearTestProgress(testId)
                                    onFinish(score, total, questions, selectedAnswers, timeTaken, timeSpentPerQuestion, coinsEarned)
                                }
                                showSubmitDialog = false
                           },
                           colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                               containerColor = Color(0xFF4285F4),
                               contentColor = Color.White
                           ),
                           shape = RoundedCornerShape(4.dp),
                           modifier = Modifier.weight(1f).height(45.dp)
                       ) {
                           Text(if(isUiHindi) "हाँ" else "Yes", fontSize = 16.sp)
                       }

                       // No Button (Grey)
                       androidx.compose.material3.Button(
                           onClick = { showSubmitDialog = false },
                           colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                               containerColor = Color(0xFF909090),
                               contentColor = Color.White
                           ),
                           shape = RoundedCornerShape(4.dp),
                           modifier = Modifier.weight(1f).height(45.dp)
                       ) {
                           Text(if(isUiHindi) "नहीं" else "No", fontSize = 16.sp)
                       }
                   }
               }
           }
        }
    }
    
    if (showPauseDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPauseDialog = false }) {
           Surface(
               shape = RoundedCornerShape(8.dp),
               color = Color.White,
               modifier = Modifier.fillMaxWidth().padding(16.dp)
           ) {
               Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                   Text(
                       text = if(isUiHindi) "क्या आप वाकई परीक्षा रोकना चाहते हैं?" else "Are you sure you want to pause the test?",
                       style = MaterialTheme.typography.titleMedium,
                       fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                       color = Color.Black,
                       textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                       modifier = Modifier.padding(bottom = 24.dp)
                   )
                   
                   Row(
                       modifier = Modifier.fillMaxWidth(),
                       horizontalArrangement = Arrangement.spacedBy(16.dp)
                   ) {
                       // Yes Button (Blue)
                       androidx.compose.material3.Button(
                           onClick = {
                                // Accumulate time
                                val now = System.currentTimeMillis()
                                val diff = now - questionStartTime
                                val currentTotal = timeSpentPerQuestion.getOrDefault(currentQuestionIndex, 0L)
                                timeSpentPerQuestion[currentQuestionIndex] = currentTotal + diff

                                // Fix: Merge Drafts -> Selected before saving
                                draftSelections.forEach { (key, value) ->
                                    selectedAnswers[key] = value
                                }

                                val answersMap = selectedAnswers.mapKeys { (index, _) -> questions[index].id }
                                com.rajasthanexams.data.OfflineManager.saveUserAnswers(testId, answersMap)
                                com.rajasthanexams.data.OfflineManager.saveTestProgress(testId, timeLeft, currentQuestionIndex)

                                // Save Marked
                                val markedIds = markedQuestions.map { questions[it].id }
                                com.rajasthanexams.data.OfflineManager.saveMarkedQuestions(testId, markedIds)

                                // Save Times
                                val timeMap = timeSpentPerQuestion.mapKeys { (idx, _) -> questions[idx].id }
                                com.rajasthanexams.data.OfflineManager.saveTimeSpent(testId, timeMap)

                                showPauseDialog = false
                                onPause()
                           },
                           colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                               containerColor = Color(0xFF4285F4),
                               contentColor = Color.White
                           ),
                           shape = RoundedCornerShape(4.dp),
                           modifier = Modifier.weight(1f).height(45.dp)
                       ) {
                           Text(if(isUiHindi) "हाँ" else "Yes", fontSize = 16.sp)
                       }

                       // No Button (Grey)
                       androidx.compose.material3.Button(
                           onClick = { showPauseDialog = false },
                           colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                               containerColor = Color(0xFF909090),
                               contentColor = Color.White
                           ),
                           shape = RoundedCornerShape(4.dp),
                           modifier = Modifier.weight(1f).height(45.dp)
                       ) {
                           Text(if(isUiHindi) "नहीं" else "No", fontSize = 16.sp)
                       }
                   }
               }
           }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // Title follows UI Language
                        Text(if(isUiHindi) "अभ्यास परीक्षा" else "Practice Test", style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        // X Button Logic
                         if (!isAttemptedGlobal) {
                             if (test?.isLive == true || test?.type == TestType.LIVE) {
                                 showSubmitDialog = true
                             } else {
                                 showPauseDialog = true
                             }
                         } else {
                            // Already Attempted (Review Mode) -> Just close
                             onPause() 
                         }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },

                actions = {
                    // Pause Button for Non-Live Tests
                    if (test?.isLive != true && test?.type != TestType.LIVE && !isAttemptedGlobal) {
                         IconButton(onClick = { showPauseDialog = true }) {
                             Icon(
                                 imageVector = androidx.compose.material.icons.Icons.Default.Pause, // Assuming Pause is available, else using specific
                                 contentDescription = "Pause",
                                 tint = MaterialTheme.colorScheme.primary
                             )
                         }
                    }

                    // Question Palette Icon
                    IconButton(onClick = { showQuestionPalette = true }) {
                         // Using a simple grid-like icon. Apps is standard for "Grid" view.
                         Icon(Icons.Default.Menu, contentDescription = "Question Palette") 
                    }
                
                    // Content Language Toggle
                    IconButton(onClick = { isContentHindi = !isContentHindi }) {
                        // Custom Icon: Mixed English (E) and Hindi (अ)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                             Row(
                                 verticalAlignment = Alignment.CenterVertically, 
                                 horizontalArrangement = Arrangement.Center,
                                 modifier = Modifier.padding(2.dp)
                             ) {
                                 Text(
                                     text = "E", 
                                     style = MaterialTheme.typography.labelSmall, 
                                     fontSize = 10.sp, 
                                     fontWeight = androidx.compose.ui.text.font.FontWeight.Black, 
                                     color = MaterialTheme.colorScheme.primary
                                 )
                                 Text(
                                     text = "/", 
                                     style = MaterialTheme.typography.labelSmall, 
                                     fontSize = 10.sp, 
                                     color = MaterialTheme.colorScheme.primary
                                 )
                                 Text(
                                     text = "अ", 
                                     style = MaterialTheme.typography.labelSmall, 
                                     fontSize = 10.sp, 
                                     fontWeight = androidx.compose.ui.text.font.FontWeight.Black, 
                                     color = MaterialTheme.colorScheme.primary
                                 )
                             }
                        }
                    }
                    
                    if (!isPracticeMode) {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                            Text(formattedTime, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }


                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center // Center the single button
                ) {
                    // Mark for Review Button (Visible if NOT Practice Mode, NOT Review Mode, NOT Topic Test)
                    if (!isPracticeMode && !isReviewMode && (test?.type != TestType.TOPIC)) {
                        val isMarked = markedQuestions.contains(currentQuestionIndex)
                        androidx.compose.material3.OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF9C27B0)), // Purple border
                            onClick = {
                                if (isMarked) {
                                    markedQuestions.remove(currentQuestionIndex)
                                } else {
                                    markedQuestions.add(currentQuestionIndex)
                                }
                            }
                        ) {
                            Text(
                                text = if (isMarked) "Unmark" else "Mark",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color(0xFF9C27B0)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Clear Response Button (Visible if something is selected/drafted)
                    val hasSelection = draftSelections[currentQuestionIndex] != null || selectedAnswers[currentQuestionIndex] != null
                    if (hasSelection && !isReviewMode && !isAttemptedGlobal) {
                         androidx.compose.material3.OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                draftSelections.remove(currentQuestionIndex)
                                selectedAnswers.remove(currentQuestionIndex)
                                // selectedOption = null // Removed variable
                            }
                        ) {
                            Text(
                                text = if(isUiHindi) "साफ़ करें" else "Clear",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Save & Next Button
                    AppButton(
                        modifier = Modifier.weight(1f), // Share width if Mark button exists, else fill
                        text = if (currentQuestionIndex < totalQuestions - 1) {
                             if(isUiHindi) "सहेजें और अगला (Save & Next)" else "Save & Next" 
                        } else {
                             if(isUiHindi) "जमा करें (Submit)" else "Submit"
                        },
                        onClick = { 
                            // Save current selection (Draft -> Saved)
                            val currentDraft = draftSelections[currentQuestionIndex]
                            if (currentDraft != null) {
                                selectedAnswers[currentQuestionIndex] = currentDraft
                            }
    
                            if (currentQuestionIndex < totalQuestions - 1) {
                                currentQuestionIndex++
                                // showSolution updated dynamically in UI
                            } else {
                                // Backend Submission
                                val totalTimeSeconds = if (test != null) test.time * 60 else 10 * 60
                                val timeTaken = (totalTimeSeconds - timeLeft).toLong()
                                
                                // Fix: Include Drafts in Submission
                                draftSelections.forEach { (key, value) ->
                                    if (!selectedAnswers.containsKey(key)) {
                                        selectedAnswers[key] = value
                                    }
                                }

                                val answersMap = selectedAnswers.mapKeys { (index, _) -> questions[index].id }
    
                                viewModel.submitTest(testId, answersMap, timeTaken.toInt()) { score, total, _, coinsEarned ->
                                    com.rajasthanexams.data.OfflineManager.clearTestProgress(testId)
                                    onFinish(score, total, questions, selectedAnswers, timeTaken, timeSpentPerQuestion, coinsEarned)
                                }
                            }
                        },
                        enabled = true 
                    )
                }
            }
        }
    ) { paddingValues ->
        HeritagePatternBackground(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Subject Tabs
                if (subjects.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 16.dp,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        subjects.forEachIndexed { index, subject ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    scope.launch {
                                        val qIndex = questions.indexOfFirst { 
                                            (it.subject ?: if(isUiHindi) "सामान्य" else "General") == subject 
                                        }
                                        if (qIndex != -1) {
                                            pagerState.scrollToPage(qIndex)
                                        }
                                    }
                                },
                                text = { Text(subject, style = MaterialTheme.typography.titleSmall) }
                            )
                        }
                    }
                }
            if (isAttemptedGlobal) {
                 Row(
                     modifier = Modifier.fillMaxWidth().background(Color(0xFFFFE0B2)).padding(8.dp),
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.SpaceBetween
                 ) {
                     Text("Review Mode: You have already attempted this test.", color = Color(0xFFE65100), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                     
                     if (test?.isLive != true) {
                         androidx.compose.material3.TextButton(onClick = {
                             // Retake Logic
                             com.rajasthanexams.data.OfflineManager.clearTestUserData(testId)
                             selectedAnswers.clear() // Clear local answers
                             markedQuestions.clear()
                             visitedQuestions.clear() // Clear visited
                             currentQuestionIndex = 0
                             // selectedOption = null // Removed
                             draftSelections.clear() // Clear drafts
                             showSolution = false
                             // Refresh VM to update isAttemptedGlobal
                             viewModel.fetchQuestions(testId)
                         }) {
                             Text("RETAKE", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFFE65100))
                         }
                     }
                 }
            }
            // Swipeable Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                
                val currentQuestion = questions.getOrNull(pageIndex)
                
                if (currentQuestion != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Live Timer Logic
                            var liveTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
                            val isCurrentPage = (pageIndex == currentQuestionIndex)
                            
                            androidx.compose.runtime.LaunchedEffect(isCurrentPage, questionStartTime, showResumeDialog) {
                                if (isCurrentPage && !showResumeDialog) {
                                    while(true) {
                                        val existing = timeSpentPerQuestion[pageIndex] ?: 0L
                                        val currentSession = System.currentTimeMillis() - questionStartTime
                                        liveTime = existing + currentSession
                                        kotlinx.coroutines.delay(1000L)
                                    }
                                } else {
                                    liveTime = timeSpentPerQuestion[pageIndex] ?: 0L
                                }
                            }

                            Text(
                                text = "Q. ${pageIndex + 1} / $totalQuestions",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            
                            // Timer Display
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val timeSec = liveTime / 1000
                                val timeMin = timeSec / 60
                                val timeSecRem = timeSec % 60
                                Text(
                                    text = String.format("%02d:%02d", timeMin, timeSecRem),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Gray
                                )
                            }
                            
                            // Report/Marking Icon
                            IconButton(onClick = { showReportDialog = true }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.Warning,
                                    contentDescription = "Report",
                                    tint = Color(0xFFFF9800)
                                )
                            }
                            
                            // Marks Display
                            val marks = currentQuestion.marksPerQuestion ?: test?.marksPerQuestion ?: 1.0
                            val negMarks = currentQuestion.negativeMarks ?: test?.negativeMarks ?: 0.0
                            
                            val marksStr = marks.toString()
                            val negStr = negMarks.toString()

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Positive
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "+ $marksStr",
                                        color = Color(0xFF2E7D32),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                // Negative
                                Surface(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "- $negStr",
                                        color = Color(0xFFC62828),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                                        // Bookmark Toggle
                                        var isBookmarked by remember(currentQuestion.id) { 
                                            mutableStateOf(com.rajasthanexams.data.OfflineManager.isBookmarked(currentQuestion.id)) 
                                        }
    
                                        Row(
                                            modifier = Modifier.fillMaxWidth(), 
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                // CONTENT Language
                                                text = if (isContentHindi) currentQuestion.questionHi else currentQuestion.questionEn,
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Always show Mark Icon if marked
                                                if (markedQuestions.contains(pageIndex)) {
                                                    Icon(
                                                         imageVector = Icons.Filled.Flag,
                                                         contentDescription = "Marked",
                                                         tint = Color(0xFF9C27B0), // Purple
                                                         modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                }

                                                if (isAttemptedGlobal || (test?.isLive != true)) {
                                                    IconButton(onClick = {
                                                        if (isBookmarked) {
                                                            com.rajasthanexams.data.OfflineManager.removeBookmark(currentQuestion.id)
                                                        } else {
                                                            com.rajasthanexams.data.OfflineManager.saveBookmark(currentQuestion)
                                                        }
                                                        isBookmarked = !isBookmarked
                                                    }) {
                                                        Icon(
                                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                            contentDescription = "Bookmark",
                                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        // Resolution of Selection: Check Draft -> Then Saved
                                        val mySelection = draftSelections[pageIndex] ?: selectedAnswers[pageIndex]
    
                                        // Options
                                        val currentOptions = if (isContentHindi) currentQuestion.optionsHi else currentQuestion.optionsEn
                                        currentOptions.forEachIndexed { index, option ->
                                            val isCorrect = index == currentQuestion.correctOptionIndex
                                            val isSelected = mySelection == index
                                            
                                            // Show Result if: Attempted (Review) OR (Practice Mode AND Answered)
                                            val shouldShowResult = isAttemptedGlobal || (isPracticeMode && mySelection != null)
                                            
                                            val backgroundColor = if (shouldShowResult) {
                                                when {
                                                    isCorrect -> Color(0xFFE8F5E9) 
                                                    isSelected -> Color(0xFFFFEBEE) 
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            } else {
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                            }
                                            
                                            val borderColor = if (shouldShowResult) {
                                                when {
                                                    isCorrect -> Color(0xFF43A047) 
                                                    isSelected -> Color(0xFFE53935)
                                                    else -> Color.Gray.copy(alpha = 0.2f)
                                                }
                                            } else {
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                                            }
    
                                            OptionItem(
                                                text = "${(index + 65).toChar()}. $option",
                                                isSelected = isSelected,
                                                backgroundColor = backgroundColor,
                                                borderColor = borderColor,
                                                onClick = { 
                                                    if (isAttemptedGlobal) {
                                                         // Review Logic
                                                    } else if (isPracticeMode) {
                                                        // Practice Mode (Instant)
                                                        if (mySelection == null) {
                                                            draftSelections[pageIndex] = index
                                                            selectedAnswers[pageIndex] = index // Auto save in Practice Mode
                                                        }
                                                    } else {
                                                        // Exam Mode: Update Draft
                                                        draftSelections[pageIndex] = index
                                                    }
                                                }
                                            )
                                        }
                                        
                                        // Solution Section
                                        val shouldShowSolution = (isAttemptedGlobal || (isPracticeMode && mySelection != null)) && (test?.allowSolution != false)
                                        
                                        if (shouldShowSolution) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            androidx.compose.material3.Card(
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                                    containerColor = Color(0xFFFFF3E0)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(
                                                        text = if(isUiHindi) "व्याख्या (Solution):" else "Solution:",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = Color(0xFFE65100),
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = if(isContentHindi) currentQuestion.solutionHi else currentQuestion.solutionEn,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFFBF360C)
                                                    )
                                                }
                                            }
                                        }
                    }
                } else {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("Loading Questions...")
                     }
                }
                }
            }
        }
    }
}

@Composable
fun OptionItem(
    text: String,
    isSelected: Boolean,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.Gray,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
    }
}
