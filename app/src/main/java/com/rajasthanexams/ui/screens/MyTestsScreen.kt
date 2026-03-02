package com.rajasthanexams.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rajasthanexams.data.Test
import com.rajasthanexams.ui.viewmodels.MyTestsUiState
import com.rajasthanexams.ui.viewmodels.MyTestsViewModel
import com.rajasthanexams.ui.viewmodels.PurchasedExam

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTestsScreen(
    onTestClick: (Test) -> Unit,
    onBrowseClick: () -> Unit,
    onExamClick: (PurchasedExam) -> Unit,
    viewModel: MyTestsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val exams = remember(uiState) {
        if (uiState is MyTestsUiState.Success)
            (uiState as MyTestsUiState.Success).exams
        else emptyList()
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Header ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text(
                            "My Tests",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            when (uiState) {
                                is MyTestsUiState.Loading -> "Loading your exams..."
                                is MyTestsUiState.Success ->
                                    if (exams.isEmpty()) "No purchased exams yet"
                                    else "${exams.size} purchased exam${if (exams.size > 1) "s" else ""}"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // ── Loading ─────────────────────────────────────────────
            if (uiState is MyTestsUiState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            }

            // ── Error ───────────────────────────────────────────────
            if (uiState is MyTestsUiState.Error) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            (uiState as MyTestsUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Retry") }
                    }
                }
            }

            // ── Empty State ─────────────────────────────────────────
            if (uiState is MyTestsUiState.Success && exams.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1A237E).copy(alpha = 0.08f),
                            modifier = Modifier.size(96.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF1A237E).copy(alpha = 0.5f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "No Purchased Exams Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Browse the Home tab and purchase an exam to unlock premium tests",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBrowseClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse Exams")
                        }
                    }
                }
            }

            // ── Purchased Exams — Category Style Row ────────────────
            if (uiState is MyTestsUiState.Success && exams.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 24.dp)) {
                        Text(
                            "My Purchased Exams",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap an exam to see its tests",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            exams.forEach { exam ->
                                PurchasedExamCategoryItem(
                                    exam = exam,
                                    isSelected = false,
                                    onClick = { onExamClick(exam) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchasedExamCategoryItem(
    exam: PurchasedExam,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Box {
            Surface(
                shape = CircleShape,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surface,
                shadowElevation = if (isSelected) 4.dp else 2.dp,
                border = if (isSelected)
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else null,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                    if (!exam.iconUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = exam.iconUrl,
                            contentDescription = exam.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Initials fallback
                        val initials = exam.title.split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .joinToString("")
                            .uppercase()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                initials,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Green ✓ purchased badge
            Surface(
                shape = CircleShape,
                color = Color(0xFF43A047),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .offset(x = 4.dp, y = (-4).dp),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Purchased",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            exam.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            "${exam.testCount} Tests",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}


