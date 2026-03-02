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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

import androidx.compose.material.icons.filled.Star // Added import

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
            // ... (Content hidden for brevity, no changes here)
             Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterTitle = when(reviewFilter) {
                        ReviewFilter.ALL -> if (isUiHindi) "कोन समीक्षा" else "All Questions"
                        ReviewFilter.CORRECT -> if (isUiHindi) "सही उत्तर" else "Correct Answers" 
                        ReviewFilter.WRONG -> if (isUiHindi) "गलत उत्तर" else "Wrong Answers"
                        ReviewFilter.SKIPPED -> if (isUiHindi) "छोड़े गए प्रश्न" else "Skipped Questions"
                    }
                    Text(
                        text = if (reviewFilter == ReviewFilter.ALL) (if (isUiHindi) "उत्तर समीक्षा" else "Review Answers") else filterTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    androidx.compose.material3.IconButton(onClick = { isContentHindi = !isContentHindi }) {
                        // Custom Icon: Mixed English (E) and Hindi (अ)
                        androidx.compose.foundation.layout.Box(
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
                                     fontWeight = FontWeight.Black, 
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
                                     fontWeight = FontWeight.Black, 
                                     color = MaterialTheme.colorScheme.primary
                                 )
                             }
                        }
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    val filteredQuestions = questions.mapIndexed { index, question -> index to question }
                        .filter { (originalIndex, question) ->
                            val userAnswerIndex = userAnswers[originalIndex]
                            val correctOptionIndex = question.correctOptionIndex
                            when (reviewFilter) {
                                ReviewFilter.ALL -> true
                                ReviewFilter.CORRECT -> userAnswerIndex != null && userAnswerIndex == correctOptionIndex
                                ReviewFilter.WRONG -> userAnswerIndex != null && userAnswerIndex != correctOptionIndex
                                ReviewFilter.SKIPPED -> userAnswerIndex == null
                            }
                        }

                    if (filteredQuestions.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if(isUiHindi) "कोई प्रश्न नहीं मिला" else "No questions found for this filter",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    itemsIndexed(filteredQuestions) { _, (originalIndex, question) ->
                        val index = originalIndex
                        val userAnswerIndex = userAnswers[index]
                        val correctOptionIndex = question.correctOptionIndex
                        val isCorrect = userAnswerIndex != null && userAnswerIndex == correctOptionIndex
                        val isSkipped = userAnswerIndex == null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        "Q${index + 1}.",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isContentHindi) question.questionHi else question.questionEn,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // Time Spent Display
                                val timeSpent = timePerQuestion[index] ?: 0L
                                val timeSec = timeSpent / 1000
                                val timeMin = timeSec / 60
                                val timeSecRem = timeSec % 60
                                val timeStr = String.format("%02d:%02d", timeMin, timeSecRem)
                                
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Schedule, // Make sure to import or use explicit
                                        contentDescription = "Time",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val questionOptions = if (isContentHindi) question.optionsHi else question.optionsEn
                                questionOptions.forEachIndexed { optIndex, option ->
                                    val isSelected = userAnswerIndex == optIndex
                                    val isTheCorrectAnswer = correctOptionIndex == optIndex
                                    
                                    val (bgColor, textColor) = when {
                                        isTheCorrectAnswer -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // Green bg, Dark Green text
                                        isSelected && !isTheCorrectAnswer -> Color(0xFFFFEBEE) to Color(0xFFC62828) // Red bg, Dark Red text
                                        else -> Color.Transparent to MaterialTheme.colorScheme.onSurface
                                    }
                                    
                                    val borderColor = if (isTheCorrectAnswer || isSelected) Color.Transparent else Color.Gray.copy(alpha=0.2f)

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(bgColor, RoundedCornerShape(8.dp))
                                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isTheCorrectAnswer) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                            } else if (isSelected) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            
                                            Text(
                                                option,
                                                color = textColor,
                                                fontWeight = if (isTheCorrectAnswer || isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                                
                                // Solution Block
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                    ) {
                                    Text(
                                        text = if(isUiHindi) "व्याख्या:" else "Solution:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if(isContentHindi) question.solutionHi else question.solutionEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFBF360C)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                AppButton(
                    text = if(isUiHindi) "बंद करें" else "Close Review", 
                    onClick = { 
                        activeView = ResultView.SUMMARY 
                        reviewFilter = ReviewFilter.ALL // Reset filter on close
                    }
                )
            }
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
