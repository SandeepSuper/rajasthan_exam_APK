package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajasthanexams.ui.components.HeritagePatternBackground

data class DoubtPost(
    val id: String,
    val userId: String,
    val userName: String,
    val timeAgo: String,
    val content: String,
    val subject: String,
    val category: String, // e.g., "History", "Math"
    val upvotes: Int,
    val commentCount: Int,
    val verifiedAnswer: String? = null // Null if no verified answer
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen() {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "History", "Geography", "Art & Culture", "Polity")

    val posts = remember {
        listOf(
            DoubtPost("1", "u1", "Rahul Sharma", "2h ago", "महाराणा प्रताप के घोड़े 'चेतक' की समाधि कहां स्थित है?", "History", "Culture", 45, 12, "बलीचा गांव, राजसमंद में स्थित है।"),
            DoubtPost("2", "u2", "Priya Verma", "5h ago", "What is the correct order of Aravalli peaks by height?", "Geography", "Geography", 32, 8, null),
            DoubtPost("3", "u3", "Amit Singh", "1d ago", "बनी-ठनी चित्रकला किस शैली से सम्बंधित है?", "Art & Culture", "Art", 120, 25, "किशनगढ़ शैली (नागरीदास के समय)।")
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Open Ask Dialog */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ask Doubt", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        HeritagePatternBackground(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    "Community & Doubts",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                // Filter Chips
                ScrollableTabRow(
                    selectedTabIndex = filters.indexOf(selectedFilter),
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    indicator = {},
                    divider = {}
                ) {
                    filters.forEach { filter ->
                        SuggestionChip(
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                labelColor = if (selectedFilter == filter) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feed
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts) { post ->
                        if (selectedFilter == "All" || post.subject == selectedFilter) {
                            DoubtPostCard(post)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoubtPostCard(post: DoubtPost) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.userName.take(1), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(post.userName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(post.timeAgo, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha=0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        post.subject,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Question
            Text(post.content, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Verified Answer (if any)
            if (post.verifiedAnswer != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Educator Answer", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(post.verifiedAnswer, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B5E20))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.upvotes}", color = Color.Gray)
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Icon(Icons.Default.Comment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.commentCount}", color = Color.Gray)
            }
        }
    }
}
