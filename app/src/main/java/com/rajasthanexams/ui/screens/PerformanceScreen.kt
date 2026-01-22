package com.rajasthanexams.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    isHindi: Boolean,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(isHindi) "प्रदर्शन विश्लेषण" else "Performance Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        HeritagePatternBackground(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Expanded Analytics Content
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if(isHindi) "समग्र प्रदर्शन" else "Overall Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Accuracy Circle & Stats Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Circular Accuracy Chart
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                                CircularProgress(percentage = 0.78f, color = Color(0xFF2ECC71))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("78%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text(if(isHindi) "सटीकता" else "Accuracy", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(24.dp))
                            
                            // Key Stats
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                StatRow(if(isHindi) "टेस्ट दिए" else "Tests Taken", "42", Icons.Default.Assignment, Color(0xFF3498DB))
                                StatRow(if(isHindi) "रैंक" else "Rank", "Top 5%", Icons.Default.ShowChart, Color(0xFF9B59B6))
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Divider(color = Color.LightGray.copy(alpha=0.3f))
                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Comparison Graph
                        Text(
                            if(isHindi) "साप्ताहिक प्रगति (बनाम औसत)" else "Weekly Progress (vs Avg)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ComparisonGraph(modifier = Modifier.fillMaxWidth().height(200.dp))
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // 3. Weak Topics
                        Text(
                            if(isHindi) "कमजोर विषय (सुधार करें)" else "Weak Topics (Improve Now)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE74C3C)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeakTopicChip(if(isHindi) "इतिहास" else "History")
                            WeakTopicChip(if(isHindi) "कला और संस्कृति" else "Art & Culture")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CircularProgress(percentage: Float, color: Color) {
    Canvas(modifier = Modifier.size(120.dp)) {
        val strokeWidth = 10.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth / 2
        
        // Background Circle
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.2f),
            style = Stroke(width = strokeWidth),
            radius = radius
        )
        
        // Progress Ark
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360 * percentage,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth/2, strokeWidth/2),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
    }
}

@Composable
fun ComparisonGraph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val points = listOf(0.4f, 0.5f, 0.45f, 0.7f, 0.6f, 0.8f, 0.78f) // User Data (0.0 - 1.0)
        val avgPoints = listOf(0.3f, 0.35f, 0.4f, 0.45f, 0.5f, 0.5f, 0.55f) // Average Data

        val stepX = width / (points.size - 1)
        
        // Draw Avg Line
        val avgPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * (1 - avgPoints[0]))
            avgPoints.forEachIndexed { index, p ->
                lineTo(index * stepX, height * (1 - p))
            }
        }
        drawPath(avgPath, Color.Gray.copy(alpha = 0.5f), style = Stroke(width = 4f))

        // Draw User Line
        val userPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * (1 - points[0]))
            points.forEachIndexed { index, p ->
                val x = index * stepX
                val y = height * (1 - p)
                lineTo(x, y)
            }
        }
        
        // Gradient Fill for User Path
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            addPath(userPath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(RoyalBlue.copy(alpha=0.3f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(userPath, RoyalBlue, style = Stroke(width = 6f, cap = StrokeCap.Round))
        
        // Draw Dots
        points.forEachIndexed { index, p ->
             drawCircle(RoyalBlue, radius = 8f, center = androidx.compose.ui.geometry.Offset(index * stepX, height * (1 - p)))
        }
    }
}

@Composable
fun StatRow(title: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color.copy(alpha=0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun WeakTopicChip(label: String) {
    Surface(
        color = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.Bold
        )
    }
}
