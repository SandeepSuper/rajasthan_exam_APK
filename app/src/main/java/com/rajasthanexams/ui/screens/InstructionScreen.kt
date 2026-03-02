package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.ui.components.AppButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionScreen(
    testId: String,
    testTitle: String,
    totalQuestions: Int = 100, // Default or passed
    totalMarks: Double = 200.0, // Default
    durationMinutes: Int = 120, // Default or passed
    markingScheme: String = "1/3", // Penalty
    isUiHindi: Boolean,
    onBackClick: () -> Unit,
    onStartTest: () -> Unit
) {
    // Check for saved progress
    val context = androidx.compose.ui.platform.LocalContext.current
    val savedProgress = androidx.compose.runtime.remember(testId) {
        com.rajasthanexams.data.OfflineManager.getTestProgress(testId)
    }
    val isResumeAvailable = savedProgress != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isUiHindi) "निर्देश (Instructions)" else "Instructions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AppButton(
                    text = if(isResumeAvailable) {
                         if (isUiHindi) "परीक्षा जारी रखें" else "Resume Test"
                    } else {
                         if (isUiHindi) "परीक्षा शुरू करें" else "Start Test"
                    },
                    onClick = onStartTest,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Test Title
            Text(
                text = testTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Info Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    value = "$totalQuestions",
                    label = if (isUiHindi) "प्रश्न" else "Questions"
                )
                val formattedMarks = if (totalMarks % 1.0 == 0.0) {
                    totalMarks.toInt().toString()
                } else {
                    totalMarks.toString()
                }
                
                InfoChip(
                    value = formattedMarks, 
                    label = if (isUiHindi) "अंक" else "Marks"
                )
                InfoChip(
                    value = "$durationMinutes",
                    label = if (isUiHindi) "मिनट" else "Mins"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(24.dp))

            // Instructions Header
            Text(
                text = if (isUiHindi) "कृपया निम्नलिखित निर्देशों को ध्यान से पढ़ें" else "Please read the following instructions very carefully",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Instructions List
            val instructions = if (isUiHindi) listOf(
                "1. आपके पास परीक्षा पूरी करने के लिए $durationMinutes मिनट हैं।",
                "2. परीक्षा में कुल $totalQuestions प्रश्न हैं।",
                "3. प्रत्येक प्रश्न का केवल एक सही उत्तर है।",
                "4. प्रत्येक गलत उत्तर के लिए $markingScheme अंक का दंड है।",
                "5. आप किसी अन्य विकल्प पर क्लिक करके अपना उत्तर बदल सकते हैं।",
                "6. आप 'Clear Response' बटन पर क्लिक करके अपना उत्तर हटा सकते हैं।",
                "7. स्क्रीन के दाईं ओर प्रश्न सूची दिखाई देती है। आप किसी भी क्रम में प्रश्न हल कर सकते हैं।",
                "8. परीक्षा के दौरान कैलकुलेटर या अन्य सामग्री का उपयोग न करें।",
                "9. परीक्षा पूरी करने से पहले 'Submit Answer' बटन पर क्लिक न करें।"
            ) else listOf(
                "1. You have $durationMinutes Minutes to complete the test.",
                "2. The test contains a total of $totalQuestions Questions.",
                "3. There is only one correct answer to each question. Click on the most appropriate option to mark it as your answer.",
                "4. There is $markingScheme penalty for each wrong answer.",
                "5. You can change your answer by clicking on some other option.",
                "6. You can unmark your answer by clicking on the 'Clear Response' button.",
                "7. A Number list of all questions appears at the right-hand side of the screen. You can access questions in any order.",
                "8. You can use rough sheets while taking the test. Do not use calculators, log tables, dictionaries, etc.",
                "9. Do not click the button 'Submit test' before completing the test. A test once submitted cannot be resumed."
            )

            instructions.forEach { instruction ->
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Spacing for bottom bar
        }
    }
}

@Composable
fun InfoChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
