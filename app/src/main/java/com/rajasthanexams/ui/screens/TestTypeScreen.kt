package com.rajasthanexams.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajasthanexams.data.TestType
import com.rajasthanexams.ui.components.HeritagePatternBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestTypeScreen(
    categoryName: String,
    onBackClick: () -> Unit,
    onTypeSelect: (TestType) -> Unit
) {
    HeritagePatternBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    "Select Test Type",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                TestTypeCard(
                    title = "Mock Tests",
                    subtitle = "Practice with full-length simulations",
                    icon = Icons.Default.Assignment,
                    color = Color(0xFFE67E22), // Orange
                    onClick = { onTypeSelect(TestType.MOCK) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                TestTypeCard(
                    title = "Topic Tests",
                    subtitle = "Master specific subjects",
                    icon = Icons.Default.Book,
                    color = Color(0xFF27AE60), // Green
                    onClick = { onTypeSelect(TestType.TOPIC) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TestTypeCard(
                    title = "Full Tests",
                    subtitle = "Complete syllabus coverage",
                    icon = Icons.Default.ListAlt,
                    color = Color(0xFF2980B9), // Blue
                    onClick = { onTypeSelect(TestType.FULL) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TestTypeCard(
                    title = "Previous Year Papers (PYQ)",
                    subtitle = "Patwari, RAS, REET Old Papers",
                    icon = Icons.Default.History,
                    color = Color(0xFF8E44AD), // Purple
                    onClick = { onTypeSelect(TestType.PYQ) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TestTypeCard(
                    title = "Daily Quiz",
                    subtitle = "Short daily practice tests",
                    icon = Icons.Default.Event,
                    color = Color(0xFF009688), // Teal
                    onClick = { onTypeSelect(TestType.DAILY_QUIZ) }
                )
            }
        }
    }
}

@Composable
fun TestTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
