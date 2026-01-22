package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajasthanexams.data.MockData
import com.rajasthanexams.data.Question
import com.rajasthanexams.ui.components.HeritagePatternBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    isHindi: Boolean,
    onBackClick: () -> Unit
) {
    // Local language state, defaults to the passed global setting
    var localIsHindi by remember(isHindi) { mutableStateOf(isHindi) }

    // Filter bookmarked questions (unchanged)
    val bookmarkedQuestions = remember {
        MockData.sampleQuestions.filter { MockData.bookmarkedQuestionIds.contains(it.id) }
    }

    HeritagePatternBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(if(localIsHindi) "सहेजे गए प्रश्न" else "Saved Questions", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Language Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                if(localIsHindi) "हिन्दी" else "Eng",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = localIsHindi,
                                onCheckedChange = { localIsHindi = it },
                                modifier = Modifier.scale(0.8f) 
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (bookmarkedQuestions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if(localIsHindi) "कोई बुकमार्क प्रश्न नहीं" else "No bookmarked questions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    items(bookmarkedQuestions) { question ->
                        SavedQuestionItem(question, localIsHindi)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SavedQuestionItem(question: Question, isHindi: Boolean) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if(isHindi) question.questionHi else question.questionEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show Correct Answer
            val options = if(isHindi) question.optionsHi else question.optionsEn
            val correctOption = options[question.correctOptionIndex]
            Text(
                text = (if(isHindi) "उत्तर: " else "Answer: ") + correctOption,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF27AE60), // Green
                fontWeight = FontWeight.Bold
            )
            
            val solution = if(isHindi) question.solutionHi else question.solutionEn
            if (solution.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = (if(isHindi) "व्याख्या: " else "Exp: ") + solution,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
