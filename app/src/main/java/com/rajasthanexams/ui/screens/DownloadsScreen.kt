package com.rajasthanexams.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajasthanexams.data.OfflineManager
import com.rajasthanexams.ui.components.HeritagePatternBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    isHindi: Boolean,
    onBackClick: () -> Unit,
    onTestClick: (String) -> Unit
) {
    val downloads = remember {
        OfflineManager.getDownloadedTests().toMutableStateList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "डाउनलोड" else "Downloads", fontWeight = FontWeight.Bold) },
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
            if (downloads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (isHindi) "कोई डाउनलोड नहीं मिला" else "No downloads found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(downloads) { test ->
                        Surface(
                            shape = RoundedCornerShape(15.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth().clickable { onTestClick(test.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween, 
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        test.type,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            OfflineManager.removeDownload(test.id)
                                            downloads.remove(test)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                     // Qs
                                     androidx.compose.material3.Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                     Text(" ${test.questions} Qs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                     
                                     Spacer(modifier = Modifier.width(16.dp))
                                     
                                     // Time
                                     androidx.compose.material3.Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                     Text(" ${test.time} Mins", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                // Marking Scheme
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                     Text(
                                         text = "+${test.marksPerQuestion}", 
                                         style = MaterialTheme.typography.labelMedium,
                                         color = Color(0xFF2E7D32),
                                         fontWeight = FontWeight.Bold,
                                         modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                                     )
                                     
                                     if (test.negativeMarks > 0) {
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Text(
                                             text = "-${test.negativeMarks}", 
                                             style = MaterialTheme.typography.labelMedium,
                                             color = Color(0xFFC62828),
                                             fontWeight = FontWeight.Bold,
                                             modifier = Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                                         )
                                     }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
