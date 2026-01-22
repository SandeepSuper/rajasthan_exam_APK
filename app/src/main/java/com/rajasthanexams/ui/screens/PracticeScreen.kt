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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    testId: String,
    isUiHindi: Boolean, // Global UI Language (Default English)
    // onToggleLanguage removed, managed locally for content
    isPracticeMode: Boolean = false,
    viewModel: TestViewModel = viewModel(),
    onFinish: (Int, Int, List<Question>, Map<Int, Int>) -> Unit 
) {
    // 1. Live Data
    val uiState by viewModel.uiState.collectAsState()
    
    // Fetch on Launch
    LaunchedEffect(testId) {
        viewModel.fetchQuestions(testId)
    }

    // 2. Load Questions from State
    val questions = remember(uiState) {
        if (uiState is TestUiState.Success) (uiState as TestUiState.Success).questions else emptyList()
    }
    val totalQuestions = questions.size

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
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, Int>()) } 
    var timeLeft by remember { mutableStateOf(10 * 60) } 
    var showSolution by remember { mutableStateOf(false) }
    
    // Content Language State (Default: Hindi)
    var isContentHindi by remember { mutableStateOf(true) }

    // 3. Timer Effect
    androidx.compose.runtime.LaunchedEffect(key1 = isPracticeMode) {
        if (!isPracticeMode) {
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000L)
                timeLeft--
            }
            // Auto-submit logic
            val totalTime = 10 * 60
            val timeTaken = totalTime
            val answersMap = selectedAnswers.mapKeys { (index, _) -> questions[index].id }
            
            viewModel.submitTest(testId, answersMap, timeTaken) { score, total, _ ->
                onFinish(score, total, questions, selectedAnswers)
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
    val progress = if (totalQuestions > 0) (currentQuestionIndex + 1) / totalQuestions.toFloat() else 0f

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
                        // Calculate Score on Close
                        var score = 0
                        questions.forEachIndexed { index, question ->
                            val userAnswerIndex = selectedAnswers[index]
                            if (userAnswerIndex != null) {
                                if (userAnswerIndex == question.correctOptionIndex) {
                                    score++
                                }
                            }
                        }
                        onFinish(score, totalQuestions, questions, selectedAnswers)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    // Content Language Toggle
                    IconButton(onClick = { isContentHindi = !isContentHindi }) {
                        Text(
                            text = if (isContentHindi) "HI" else "EN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                AppButton(
                    text = if (currentQuestionIndex < totalQuestions - 1) {
                         if(isUiHindi) "अगला प्रश्न" else "Next Question" 
                    } else {
                         if(isUiHindi) "जमा करें" else "Submit Test"
                    },
                    onClick = { 
                        // Save answer
                        if (selectedOption != null) {
                            selectedAnswers[currentQuestionIndex] = selectedOption!!
                        }

                        if (currentQuestionIndex < totalQuestions - 1) {
                            currentQuestionIndex++
                            selectedOption = selectedAnswers[currentQuestionIndex] // Restore
                            showSolution = false
                        } else {
                            // Backend Submission
                            val totalTime = 10 * 60 // hardcoded for now, matches init
                            val timeTaken = totalTime - timeLeft
                            
                            // Convert index-based answers to ID-based answers if needed by Backend
                            // But for this simple integration, assume Backend accepts QuestionID -> OptionIndex
                            val answersMap = selectedAnswers.mapKeys { (index, _) -> 
                                questions[index].id 
                            }

                            viewModel.submitTest(testId, answersMap, timeTaken) { score, total, _ ->
                                // For now, passing local questions/answers to ResultScreen for detailed view
                                // In future, Backend should return full report
                                onFinish(score, total, questions, selectedAnswers)
                            }
                        }
                    },
                    enabled = selectedOption != null 
                )
            }
        }
    ) { paddingValues ->
        HeritagePatternBackground(modifier = Modifier.padding(paddingValues)) {
            if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                ) {
                    Text(
                        text = "Q. ${currentQuestionIndex + 1} / $totalQuestions",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                                    // Bookmark Toggle
                                    var isBookmarked by remember(currentQuestion.id) { mutableStateOf(MockData.bookmarkedQuestionIds.contains(currentQuestion.id)) }

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
                                        
                                        IconButton(onClick = {
                                            if (isBookmarked) {
                                                MockData.bookmarkedQuestionIds.remove(currentQuestion.id)
                                            } else {
                                                MockData.bookmarkedQuestionIds.add(currentQuestion.id)
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
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Options - CONTENT Language
                                    val currentOptions = if (isContentHindi) currentQuestion.optionsHi else currentQuestion.optionsEn
                                    currentOptions.forEachIndexed { index, option ->
                                        // Color Logic for Practice Mode
                                        val isCorrect = index == currentQuestion.correctOptionIndex
                                        val isSelected = selectedOption == index
                                        
                                        val backgroundColor = if (isPracticeMode && selectedOption != null) {
                                            when {
                                                isCorrect -> Color(0xFFE8F5E9) // Light Green
                                                isSelected -> Color(0xFFFFEBEE) // Light Red
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        } else {
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                        }
                                        
                                        val borderColor = if (isPracticeMode && selectedOption != null) {
                                            when {
                                                isCorrect -> Color(0xFF43A047) // Green
                                                isSelected -> Color(0xFFE53935) // Red
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
                                                if (selectedOption == null) { // defined selection 
                                                     selectedOption = index 
                                                     if (isPracticeMode) {
                                                         showSolution = true
                                                     }
                                                }
                                            }
                                        )
                                    }
                                    
                                    // Solution Section
                                    if (selectedOption != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        if (showSolution) {
                                            androidx.compose.material3.Card(
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                                    containerColor = Color(0xFFFFF3E0)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(
                                                        // UI Language (Label) vs Content (Solution text)
                                                        // Actually, "Solution:" label should probably match Content language for consistency, or UI?
                                                        // Let's stick to UI for label, Content for body.
                                                        text = if(isUiHindi) "व्याख्या (Solution):" else "Solution:",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = Color(0xFFE65100),
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        // CONTENT Language
                                                        text = if(isContentHindi) currentQuestion.solutionHi else currentQuestion.solutionEn,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFFBF360C)
                                                    )
                                                }
                                            }
                                        }
                                    }
                }
            } else {
                 // Fallback or Loading
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Text("Loading Questions...")
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
