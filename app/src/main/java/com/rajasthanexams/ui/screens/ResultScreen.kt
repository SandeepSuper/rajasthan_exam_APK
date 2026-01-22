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

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    questions: List<Question>,
    userAnswers: Map<Int, Int>, // qIndex -> optionIndex
    isUiHindi: Boolean,
    onHomeClick: () -> Unit,
    onRetakeClick: () -> Unit
) {
    var showReview by remember { mutableStateOf(false) }
    var isContentHindi by remember { mutableStateOf(true) }

    HeritagePatternBackground {
        if (showReview) {
            // REVIEW UI
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
                    Text(
                        if (isUiHindi) "उत्तर समीक्षा" else "Review Answers",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    androidx.compose.material3.IconButton(onClick = { isContentHindi = !isContentHindi }) {
                        Text(
                            text = if (isContentHindi) "HI" else "EN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(questions) { index, question ->
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
                AppButton(text = if(isUiHindi) "बंद करें" else "Close Review", onClick = { showReview = false })
            }
        } else {
            // SCORE UI
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
                        text = "$score/$totalQuestions",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
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
                    val accuracy = if (totalQuestions > 0) (score * 100 / totalQuestions) else 0
                    StatItem(if(isUiHindi) "सटीकता" else "Accuracy", "$accuracy%")
                    StatItem(if(isUiHindi) "प्रयास किए" else "Attempted", "${userAnswers.size}/$totalQuestions")
                    StatItem(if(isUiHindi) "समय" else "Time", "10m") // Mocked for now, strictly could be passed too
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                AppButton(
                    text = if(isUiHindi) "उत्तर समीक्षा" else "Review Answers",
                    onClick = { showReview = true }
                )
                
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

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
