package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajasthanexams.data.MockData
import com.rajasthanexams.data.TestType
import com.rajasthanexams.data.Test
import com.rajasthanexams.ui.components.AppButton
import com.rajasthanexams.ui.components.HeritagePatternBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    examId: String,
    examName: String, 
    testType: TestType? = null,
    onBackClick: () -> Unit,
    onStartPractice: (Test) -> Unit,
    viewModel: com.rajasthanexams.ui.viewmodels.ExamDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    androidx.compose.runtime.LaunchedEffect(examId, testType) {
        viewModel.fetchTests(examId, testType)
    }

    val tests = if (uiState is com.rajasthanexams.ui.viewmodels.ExamDetailUiState.Success) {
        (uiState as com.rajasthanexams.ui.viewmodels.ExamDetailUiState.Success).tests.sortedBy { it.isPremium }
    } else {
        emptyList()
    }
    

    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(examName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },

    ) { paddingValues ->
        HeritagePatternBackground(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (testType != null) "${testType.name}s for $examName" else "Available Tests",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (tests.isEmpty()) {
                     Text("No tests available depending on your selection.", color = Color.Gray)
                } else {
                     tests.forEach { test ->
                          DetailTestCard(
                              test = test, 
                              onClick = { onStartPractice(test) },
                              onDownload = { id, title, cat, neg, marks, qs, mins, callback -> 
                                  viewModel.downloadTestContent(id, title, cat, neg, marks, qs, mins, callback) 
                              }
                          ) 
                          Spacer(modifier = Modifier.height(16.dp))
                     }
                }
            }
        }
    }
}

@Composable
fun DetailTestCard(
    test: Test, 
    onClick: () -> Unit,
    onDownload: (String, String, String, Double, Double, Int, Int, (Boolean) -> Unit) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(15.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    test.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                if (test.isLive) {
                    Text(
                        "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color(0xFFEB5757), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                 Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                 Text(" ${test.questions} Qs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                 Spacer(modifier = Modifier.width(16.dp))
                 Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                 Text(" ${test.time} Mins", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                 Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                 Text(" ${test.rating}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            // Marking Scheme Display
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                 // Positive Marks
                 Text(
                     text = "+${test.marksPerQuestion}", 
                     style = MaterialTheme.typography.labelMedium,
                     color = Color(0xFF2E7D32), // Green
                     fontWeight = FontWeight.Bold,
                     modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                 )
                 Spacer(modifier = Modifier.width(8.dp))
                 
                 // Negative Marks
                 if (test.negativeMarks > 0) {
                     Text(
                         text = "-${test.negativeMarks}", 
                         style = MaterialTheme.typography.labelMedium,
                         color = Color(0xFFC62828), // Red
                         fontWeight = FontWeight.Bold,
                         modifier = Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                 }
                 
                 Text(
                     text = "per question", 
                     style = MaterialTheme.typography.bodySmall, 
                     color = Color.Gray
                 )
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha=0.5f))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${test.attempts} attempted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Download Button
                    val context = androidx.compose.ui.platform.LocalContext.current
                    var isDownloaded by androidx.compose.runtime.remember { 
                        androidx.compose.runtime.mutableStateOf(com.rajasthanexams.data.OfflineManager.isTestDownloaded(test.id)) 
                    }
                    var isDownloading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    
                    if (isDownloading) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        // Hide download for Live Tests
                        if (!test.isLive) {
                            IconButton(
                                onClick = {
                                    if (!isDownloaded) {
                                        isDownloading = true
                                        onDownload(test.id, test.title, test.category, test.negativeMarks, test.marksPerQuestion, test.questions, test.time) { success ->
                                            isDownloading = false
                                            if (success) {
                                                isDownloaded = true
                                                android.widget.Toast.makeText(context, "${test.title} Downloaded!", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "Download failed", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = if (isDownloaded) Color(0xFF27AE60) else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Check for saved progress (Resume)
                    val savedProgress = remember(test.id) {
                        com.rajasthanexams.data.OfflineManager.getTestProgress(test.id)
                    }

                    val buttonText = when {
                        savedProgress != null -> "Resume"
                        test.isAttempted -> "View Result"
                        test.isLive || !test.isPremium || test.isPurchased -> "Start Now"
                        else -> "Unlock Premium"
                    }

                    val buttonColor = when {
                        savedProgress != null -> MaterialTheme.colorScheme.secondary
                        test.isAttempted -> MaterialTheme.colorScheme.primary
                        test.isLive || !test.isPremium || test.isPurchased -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }

                    Text(
                        buttonText,
                        style = MaterialTheme.typography.labelLarge,
                        color = buttonColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
