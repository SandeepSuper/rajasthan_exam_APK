package com.rajasthanexams.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajasthanexams.data.remote.dto.PerformanceResponse
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.theme.RoyalBlue
import com.rajasthanexams.ui.viewmodels.PerformanceUiState
import com.rajasthanexams.ui.viewmodels.PerformanceViewModel

private val GreenOk    = Color(0xFF2ECC71)
private val PurpleAcc  = Color(0xFF9B59B6)
private val BlueInfo   = Color(0xFF3498DB)
private val RedWeak    = Color(0xFFE74C3C)
private val OrangeWarn = Color(0xFFF39C12)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    isHindi: Boolean,
    onBackClick: () -> Unit,
    vm: PerformanceViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isHindi) "प्रदर्शन विश्लेषण" else "Performance Analytics",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            when (uiState) {
                is PerformanceUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is PerformanceUiState.Error -> {
                    val msg = (uiState as PerformanceUiState.Error).message
                    Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning, contentDescription = null,
                            tint = RedWeak, modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (isHindi) "डेटा लोड नहीं हो सका" else "Could not load performance data",
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(msg, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { vm.load() }) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isHindi) "पुनः प्रयास करें" else "Retry")
                        }
                    }
                }

                is PerformanceUiState.Success -> {
                    val data = (uiState as PerformanceUiState.Success).data
                    PerformanceContent(isHindi = isHindi, data = data)
                }
            }
        }
    }
}

@Composable
private fun PerformanceContent(isHindi: Boolean, data: PerformanceResponse) {
    val surface    = MaterialTheme.colorScheme.surface
    val onSurface  = MaterialTheme.colorScheme.onSurface
    val primary    = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Overall Card ─────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    if (isHindi) "समग्र प्रदर्शन" else "Overall Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Accuracy donut
                    val accFraction = (data.avgAccuracy / 100.0).toFloat().coerceIn(0f, 1f)
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                        CircularProgress(percentage = accFraction, color = GreenOk)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${data.avgAccuracy.toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isHindi) "सटीकता" else "Accuracy",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.width(24.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatRow(
                            title = if (isHindi) "टेस्ट दिए" else "Tests Taken",
                            value = data.totalTests.toString(),
                            icon = Icons.Default.Assignment,
                            color = BlueInfo
                        )
                        StatRow(
                            title = if (isHindi) "सर्वश्रेष्ठ स्कोर" else "Best Score",
                            value = data.bestScore.toInt().toString(),
                            icon = Icons.Default.Star,
                            color = OrangeWarn
                        )
                        StatRow(
                            title = if (isHindi) "कुल समय" else "Time Spent",
                            value = formatTime(data.totalTimeSecs, isHindi),
                            icon = Icons.Default.Timer,
                            color = PurpleAcc
                        )
                    }
                }
            }
        }

        // ── Weekly Progress Chart ─────────────────────────────────────────────
        if (data.weeklyScores.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShowChart, contentDescription = null,
                            tint = primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isHindi) "हालिया प्रदर्शन (अंतिम ${data.weeklyScores.size} टेस्ट)"
                            else "Recent Progress (Last ${data.weeklyScores.size} Tests)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                    Spacer(Modifier.height(4.dp))

                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot(color = RoyalBlue, label = if (isHindi) "आपका स्कोर" else "Your Score")
                        LegendDot(color = Color.Gray.copy(alpha = 0.6f), label = if (isHindi) "औसत" else "Avg")
                    }
                    Spacer(Modifier.height(12.dp))

                    DynamicLineChart(
                        userValues = data.weeklyScores.map { it.toFloat() },
                        avgValues = data.weeklyAccuracies.map { it.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )

                    // Date labels
                    if (data.weeklyDates.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            data.weeklyDates.forEach { date ->
                                Text(
                                    date.takeLast(5), // MM-DD
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, contentDescription = null,
                            tint = Color.LightGray, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isHindi) "अभी तक कोई टेस्ट नहीं दिया"
                            else "No tests attempted yet",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Weak Topics ───────────────────────────────────────────────────────
        if (data.weakTopics.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null,
                            tint = RedWeak, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isHindi) "कमजोर विषय — सुधार करें" else "Weak Areas — Needs Improvement",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = RedWeak
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(data.weakTopics.size) { i ->
                            WeakTopicChip(label = data.weakTopics[i])
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
fun CircularProgress(percentage: Float, color: Color) {
    Canvas(modifier = Modifier.size(120.dp)) {
        val strokeWidth = 10.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth / 2
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.2f),
            style = Stroke(width = strokeWidth),
            radius = radius
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360 * percentage,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(radius * 2, radius * 2)
        )
    }
}

@Composable
fun DynamicLineChart(
    userValues: List<Float>,
    avgValues: List<Float>,
    modifier: Modifier = Modifier
) {
    if (userValues.isEmpty()) return

    Canvas(modifier = modifier) {
        val width  = size.width
        val height = size.height
        val maxVal = (userValues + avgValues).maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val stepX  = if (userValues.size > 1) width / (userValues.size - 1) else width

        fun normalize(v: Float) = height * (1f - v / maxVal)

        // Avg line (gray dashed-style via dots)
        if (avgValues.size >= 2) {
            val avgPath = Path().apply {
                moveTo(0f, normalize(avgValues[0]))
                avgValues.forEachIndexed { i, v -> lineTo(i * stepX, normalize(v)) }
            }
            drawPath(avgPath, Color.Gray.copy(alpha = 0.5f), style = Stroke(width = 3f))
        }

        // User fill
        val userPath = Path().apply {
            moveTo(0f, normalize(userValues[0]))
            userValues.forEachIndexed { i, v -> lineTo(i * stepX, normalize(v)) }
        }
        val fillPath = Path().apply {
            addPath(userPath)
            lineTo((userValues.size - 1) * stepX, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(RoyalBlue.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f, endY = height
            )
        )
        drawPath(userPath, RoyalBlue, style = Stroke(width = 5f, cap = StrokeCap.Round))

        // Dots
        userValues.forEachIndexed { i, v ->
            drawCircle(RoyalBlue, radius = 7f, center = Offset(i * stepX, normalize(v)))
            drawCircle(Color.White, radius = 3.5f, center = Offset(i * stepX, normalize(v)))
        }
    }
}

@Composable
fun StatRow(title: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
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
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(8.dp).background(color, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

private fun formatTime(secs: Int, isHindi: Boolean): String {
    val h = secs / 3600
    val m = (secs % 3600) / 60
    return when {
        h > 0  -> if (isHindi) "${h}घ ${m}मि" else "${h}h ${m}m"
        m > 0  -> if (isHindi) "${m} मिनट" else "${m} min"
        else   -> if (isHindi) "${secs} सेकंड" else "${secs}s"
    }
}
