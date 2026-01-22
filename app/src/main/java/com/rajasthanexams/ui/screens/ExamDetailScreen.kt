package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    onStartPractice: (String) -> Unit,
    viewModel: com.rajasthanexams.ui.viewmodels.ExamDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    androidx.compose.runtime.LaunchedEffect(examId, testType) {
        viewModel.fetchTests(examId, testType)
    }

    val tests = if (uiState is com.rajasthanexams.ui.viewmodels.ExamDetailUiState.Success) {
        (uiState as com.rajasthanexams.ui.viewmodels.ExamDetailUiState.Success).tests
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
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                AppButton(
                    text = "Start Practice",
                    onClick = { 
                        if (tests.isNotEmpty()) onStartPractice(tests[0].id) 
                    }
                )
            }
        }
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
                          DetailTestCard(test = test, onClick = { onStartPractice(test.id) }) 
                          Spacer(modifier = Modifier.height(16.dp))
                     }
                }
            }
        }
    }
}

@Composable
fun DetailTestCard(test: Test, onClick: () -> Unit) {
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
                 Spacer(modifier = Modifier.width(16.dp))
                 Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                 Text(" ${test.rating}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                    
                    IconButton(
                        onClick = {
                            if (!isDownloaded) {
                                com.rajasthanexams.data.OfflineManager.downloadTest(test.id)
                                isDownloaded = true
                                android.widget.Toast.makeText(context, "${test.title} Downloaded!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = "Download",
                            tint = if (isDownloaded) Color(0xFF27AE60) else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        if (test.isLive) "Start Now" else "Unlock Premium",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (test.isLive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
