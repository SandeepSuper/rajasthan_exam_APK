package com.rajasthanexams.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.data.Question
import com.rajasthanexams.ui.components.AppButton
import com.rajasthanexams.ui.components.HeritagePatternBackground
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells

import androidx.compose.material.icons.filled.Star

@Composable
fun ResultScreen(
    score: Double,
    totalQuestions: Int,
    questions: List<Question>,
    userAnswers: Map<Int, Int>, // qIndex -> optionIndex
    timeTaken: Long = 0,
    timePerQuestion: Map<Int, Long> = emptyMap(),
    coinsEarned: Int = 0, // Added coinsEarned
    isUiHindi: Boolean,
    onHomeClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onLeaderboardClick: () -> Unit = {},
    testId: String = ""
) {
    // ... no changes until title
    // Determine which view to show
    var activeView by remember { mutableStateOf(ResultView.SUMMARY) }
    var reviewFilter by remember { mutableStateOf(ReviewFilter.ALL) }
    var isContentHindi by remember { mutableStateOf(true) }

    // Intercept back gesture when in sub-views (Leaderboard, Review)
    androidx.activity.compose.BackHandler(enabled = activeView != ResultView.SUMMARY) {
        activeView = ResultView.SUMMARY
    }

    // If Leaderboard view is active
    if (activeView == ResultView.LEADERBOARD) {
        RankersScreen(
            testId = testId,
            userTestCoins = coinsEarned,
            onBack = { activeView = ResultView.SUMMARY }
        )
        return // Early return to show full screen leaderboard
    }

    HeritagePatternBackground {
        if (activeView == ResultView.REVIEW) {
            ReviewAnswerView(
                questions = questions,
                userAnswers = userAnswers,
                timePerQuestion = timePerQuestion,
                isUiHindi = isUiHindi,
                initialFilter = reviewFilter,
                onClose = {
                    activeView = ResultView.SUMMARY
                    reviewFilter = ReviewFilter.ALL
                }
            )
        } else {
            // SCORE UI (activeView == ResultView.SUMMARY)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if(isUiHindi) "परीक्षा समाप्त!" else "Test Completed!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Add Coins Display here
                if (coinsEarned > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                         Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF57F17)) 
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(
                             text = "+$coinsEarned Coins",
                             style = MaterialTheme.typography.titleMedium,
                             color = Color(0xFFF57F17),
                             fontWeight = FontWeight.Bold
                         )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Score Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if(isUiHindi) "आपका स्कोर" else "Your Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = String.format("%.2f/%d", score, totalQuestions),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Correct / Wrong Counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val correctCount = userAnswers.count { (qIndex, ansIndex) -> 
                            questions.getOrNull(qIndex)?.correctOptionIndex == ansIndex 
                        }
                        val incorrectCount = userAnswers.count { (qIndex, ansIndex) -> 
                            questions.getOrNull(qIndex)?.correctOptionIndex != ansIndex 
                        }
                        
                        // Correct
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { 
                                    reviewFilter = ReviewFilter.CORRECT
                                    activeView = ResultView.REVIEW
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "$correctCount",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF2E7D32), // Green
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if(isUiHindi) "सही" else "Correct",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(32.dp))
                        
                        // Wrong
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { 
                                    reviewFilter = ReviewFilter.WRONG
                                    activeView = ResultView.REVIEW
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "$incorrectCount",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFC62828), // Red
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if(isUiHindi) "गलत" else "Wrong",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC62828)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // Not Attempted
                        // Not Attempted
                        val notAttemptedCount = totalQuestions - userAnswers.size
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { 
                                    reviewFilter = ReviewFilter.SKIPPED
                                    activeView = ResultView.REVIEW
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "$notAttemptedCount",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if(isUiHindi) "छोड़े गए" else "Skipped", // or Not Attempted
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Sync Button (Simulated Offline Sync)
                val context = androidx.compose.ui.platform.LocalContext.current
                androidx.compose.material3.TextButton(
                    onClick = {
                         android.widget.Toast.makeText(context, if(isUiHindi) "परिणाम सिंक किया गया!" else "Result Synced Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if(isUiHindi) "क्लाउड पर सिंक करें" else "Sync to Cloud")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val accuracy = if (totalQuestions > 0) (score * 100 / totalQuestions) else 0.0
                    
                    val formattedTime = remember(timeTaken) {
                         val safeTime = if (timeTaken < 0) 0L else timeTaken
                         val minutes = safeTime / 60
                         val seconds = safeTime % 60
                         String.format("%02dm %02ds", minutes, seconds)
                    }
                    
                    StatItem(if(isUiHindi) "सटीकता" else "Accuracy", "%.2f%%".format(accuracy))
                    StatItem(if(isUiHindi) "प्रयास किए" else "Attempted", "${userAnswers.size}/$totalQuestions")
                    StatItem(if(isUiHindi) "समय" else "Time", formattedTime)
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                AppButton(
                    text = if(isUiHindi) "उत्तर समीक्षा" else "Review Answers",
                    onClick = { 
                        reviewFilter = ReviewFilter.ALL
                        activeView = ResultView.REVIEW 
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { activeView = ResultView.LEADERBOARD },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    // Trophy Icon
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if(isUiHindi) "लीडरबोर्ड देखें" else "View Leaderboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onRetakeClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(if(isUiHindi) "पुनः प्रयास करें" else "Retake Test", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onHomeClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                     Text(if(isUiHindi) "होम पर जाएं" else "Back to Home", fontSize = 16.sp)
                }
            }
        }
    }
}

enum class ResultView {
    SUMMARY, REVIEW, LEADERBOARD
}

enum class ReviewFilter {
    ALL, CORRECT, WRONG, SKIPPED
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReviewAnswerView(
    questions: List<Question>,
    userAnswers: Map<Int, Int>,
    timePerQuestion: Map<Int, Long>,
    isUiHindi: Boolean,
    initialFilter: ReviewFilter,
    onClose: () -> Unit
) {
    var isContentHindi by remember { mutableStateOf(true) }
    var activeFilter by remember { mutableStateOf(initialFilter) }
    var showPalette by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Build filtered list (pairs of originalIndex -> question)
    val filteredList = remember(activeFilter, questions, userAnswers) {
        questions.mapIndexed { index, q -> index to q }.filter { (idx, q) ->
            val ans = userAnswers[idx]
            when (activeFilter) {
                ReviewFilter.ALL     -> true
                ReviewFilter.CORRECT -> ans != null && ans == q.correctOptionIndex
                ReviewFilter.WRONG   -> ans != null && ans != q.correctOptionIndex
                ReviewFilter.SKIPPED -> ans == null
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { filteredList.size.coerceAtLeast(1) })

    // Question Palette bottom sheet
    if (showPalette) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showPalette = false },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isUiHindi) "प्रश्न पैलेट" else "Question Palette",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(14.dp).background(Color(0xFF4CAF50), CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isUiHindi) "सही" else "Correct", style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(14.dp).background(Color(0xFFC62828), CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isUiHindi) "गलत" else "Wrong", style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(14.dp).background(Color.Gray, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isUiHindi) "छोड़े" else "Skipped", style = MaterialTheme.typography.labelSmall)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 52.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) {
                    // Show all questions in palette (always, regardless of filter)
                    items(questions.size) { origIdx ->
                        val ans = userAnswers[origIdx]
                        val correct = questions[origIdx].correctOptionIndex
                        val isCorrect = ans != null && ans == correct
                        val isWrong   = ans != null && ans != correct
                        val isSkipped = ans == null
                        val bgColor = when {
                            isCorrect -> Color(0xFF4CAF50)
                            isWrong   -> Color(0xFFC62828)
                            else      -> Color.Gray
                        }
                        // Find position in filteredList to jump pager
                        val filteredPos = filteredList.indexOfFirst { it.first == origIdx }
                        val isCurrent = filteredList.getOrNull(pagerState.currentPage)?.first == origIdx

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(bgColor, CircleShape)
                                .border(
                                    width = if (isCurrent) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (filteredPos != -1) {
                                        scope.launch { pagerState.scrollToPage(filteredPos) }
                                    } else {
                                        // Switch filter to show this question
                                        activeFilter = when {
                                            isCorrect -> ReviewFilter.CORRECT
                                            isWrong   -> ReviewFilter.WRONG
                                            else      -> ReviewFilter.SKIPPED
                                        }
                                    }
                                    showPalette = false
                                }
                        ) {
                            Text(
                                text = "${origIdx + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            Text(
                text = if (isUiHindi) "उत्तर समीक्षा" else "Review Answers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                // Language toggle
                IconButton(onClick = { isContentHindi = !isContentHindi }) {
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
                            Text("E", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text("/", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                            Text("अ", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                // Palette button
                IconButton(onClick = { showPalette = true }) {
                    Icon(Icons.Default.GridView, contentDescription = "Palette", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                ReviewFilter.ALL     to (if (isUiHindi) "सभी" else "All"),
                ReviewFilter.WRONG   to (if (isUiHindi) "गलत" else "Wrong"),
                ReviewFilter.SKIPPED to (if (isUiHindi) "छोड़े" else "Skipped"),
                ReviewFilter.CORRECT to (if (isUiHindi) "सही" else "Correct"),
            )
            filters.forEach { (filter, label) ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = {
                        activeFilter = filter
                        scope.launch { pagerState.scrollToPage(0) }
                    },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Progress indicator
        if (filteredList.isNotEmpty()) {
            val currOrig = filteredList.getOrNull(pagerState.currentPage)?.first ?: 0
            val label = when {
                userAnswers[currOrig] == null -> if (isUiHindi) "छोड़ा गया" else "Skipped"
                userAnswers[currOrig] == questions[currOrig].correctOptionIndex -> if (isUiHindi) "सही" else "Correct"
                else -> if (isUiHindi) "गलत" else "Wrong"
            }
            val labelColor = when {
                userAnswers[currOrig] == null -> Color.Gray
                userAnswers[currOrig] == questions[currOrig].correctOptionIndex -> Color(0xFF2E7D32)
                else -> Color(0xFFC62828)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Q${currOrig + 1} / ${questions.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )
                Text(
                    "${pagerState.currentPage + 1} / ${filteredList.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Empty state
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isUiHindi) "कोई प्रश्न नहीं मिला" else "No questions for this filter",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            // Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) { page ->
                val (origIdx, question) = filteredList[page]
                val userAnswerIndex = userAnswers[origIdx]
                val correctOptionIndex = question.correctOptionIndex

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Question text
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    "Q${origIdx + 1}.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isContentHindi) question.questionHi else question.questionEn,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            // Time spent
                            val timeMs = timePerQuestion[origIdx] ?: 0L
                            val tSec = timeMs / 1000
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = String.format("%02d:%02d", tSec / 60, tSec % 60),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Options
                    val options = if (isContentHindi) question.optionsHi else question.optionsEn
                    options.forEachIndexed { optIdx, opt ->
                        val isSelected = userAnswerIndex == optIdx
                        val isCorrectOpt = correctOptionIndex == optIdx
                        val (bgColor, textColor) = when {
                            isCorrectOpt -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                            isSelected && !isCorrectOpt -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                            else -> Color.Transparent to MaterialTheme.colorScheme.onSurface
                        }
                        val borderColor = if (isCorrectOpt || isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.2f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(bgColor, RoundedCornerShape(8.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCorrectOpt) {
                                Icon(Icons.Default.Check, null, tint = textColor, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                            } else if (isSelected) {
                                Icon(Icons.Default.Close, null, tint = textColor, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(opt, color = textColor, fontWeight = if (isCorrectOpt || isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Solution
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isUiHindi) "व्याख्या:" else "Solution:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isContentHindi) question.solutionHi else question.solutionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBF360C)
                        )
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }

        // Bottom Nav Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } },
                enabled = pagerState.currentPage > 0,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.NavigateBefore, null)
                Text(if (isUiHindi) "पिछला" else "Prev")
            }

            OutlinedButton(
                onClick = { showPalette = true },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.GridView, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isUiHindi) "पैलेट" else "Palette", color = MaterialTheme.colorScheme.primary)
            }

            OutlinedButton(
                onClick = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(filteredList.size - 1)) } },
                enabled = pagerState.currentPage < filteredList.size - 1,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isUiHindi) "अगला" else "Next")
                Icon(Icons.Default.NavigateNext, null)
            }
        }
    }
}

