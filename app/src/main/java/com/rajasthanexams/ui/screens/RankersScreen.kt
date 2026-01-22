package com.rajasthanexams.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rajasthanexams.ui.components.HeritagePatternBackground
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.material.icons.filled.MonetizationOn

data class Ranker(val name: String, val score: Int, val exam: String, val coins: Int, val avatarUrl: String = "")

@Composable
fun RankersScreen(
    modifier: Modifier = Modifier,
    viewModel: com.rajasthanexams.ui.viewmodels.LeaderboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchLeaderboard()
    }

    val uiState by viewModel.uiState.collectAsState()
    
    val rankers = when(uiState) {
        is com.rajasthanexams.ui.viewmodels.LeaderboardUiState.Success -> (uiState as com.rajasthanexams.ui.viewmodels.LeaderboardUiState.Success).rankers
        else -> emptyList() // Or keep mock list as fallback
    }
    
    // If empty (loading/error or no data), use mock for visuals if desired, or show empty
    // For this demo, let's fallback to mock if empty so UI looks good
    val effectiveRankers = if (rankers.isNotEmpty()) rankers else listOf(
        Ranker("Amit Sharma", 98, "Patwari", 1250, ""),
        Ranker("Priya Singh", 96, "RAS Prelims", 1100, ""),
        Ranker("Rajeev Verma", 95, "REET L2", 1050, ""),
        Ranker("Suman Gupta", 92, "SI Hindi", 980, ""),
        Ranker("Arjun Yadav", 90, "CET", 920, "")
    )

    val topThree = effectiveRankers.take(3)
    val others = effectiveRankers.drop(3)

    // Using Box to ensure the Footer overlays the list correctly at the bottom
    Box(modifier = modifier.fillMaxSize()) {
        HeritagePatternBackground {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF))
                            ),
                            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Hall of Fame", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Our Top Achievers", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Podium
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 2nd Place - Silver Gradient
                            if (topThree.size > 1) {
                                PodiumItem(
                                    ranker = topThree[1],
                                    rank = 2,
                                    height = 140.dp,
                                    brush = Brush.verticalGradient(listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E))), // Silver
                                    borderColor = Color(0xFFC0C0C0)
                                ) 
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            // 1st Place - Gold Gradient
                            if (topThree.isNotEmpty()) {
                                PodiumItem(
                                    ranker = topThree[0],
                                    rank = 1,
                                    height = 170.dp,
                                    brush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))), // Gold
                                    borderColor = Color(0xFFFFD700)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            // 3rd Place - Bronze Gradient
                            if (topThree.size > 2) {
                                PodiumItem(
                                    ranker = topThree[2],
                                    rank = 3,
                                    height = 120.dp,
                                    brush = Brush.verticalGradient(listOf(Color(0xFFFFAB91), Color(0xFF8D6E63))), // Bronze/Copper
                                    borderColor = Color(0xFFCD7F32)
                                )
                            }
                        }
                    }
                }
                
                Text(
                    "Leaderboard",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Fill remaining space but leave room if not using Box overlay
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // Add padding for the sticky footer
                ) {
                    itemsIndexed(others) { index, ranker ->
                        RankerRow(rank = index + 4, ranker = ranker)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Sticky user rank Footer (Overlay)
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 16.dp, // Higher elevation
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     // User Avatar
                     OfflineAvatar(name = "Rahul Kumar", size = 48.dp, border = true)
                     Spacer(modifier = Modifier.width(12.dp))
                     
                     Column {
                         Text("Rahul Kumar (You)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                         Text("12 Tests â€¢ 850 Coins", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                     }
                 }
                 
                 Column(horizontalAlignment = Alignment.End) {
                     Text("Your Rank", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                     Text("#42", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                 }
            }
        }
    }
}

@Composable
fun PodiumItem(ranker: Ranker, rank: Int, height: Dp, brush: Brush, borderColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Avatar
        Box(contentAlignment = Alignment.BottomCenter) {
             Box(modifier = Modifier.offset(y = 10.dp).zIndex(1f)) {
                 // Use the explicit borderColor passed for the medal effect
                 OfflineAvatar(name = ranker.name, size = 60.dp, border = true, borderColor = borderColor)
             }
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha=0.3f),
                modifier = Modifier.size(24.dp).align(Alignment.BottomCenter).zIndex(2f)
            ) {
                 Box(contentAlignment = Alignment.Center) {
                     Text("$rank", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                 }
            }
        }
        
        // Podium Pillar
        Card(
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.width(90.dp).height(height)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = brush)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp).fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(20.dp)) // Space for avatar overlap
                    Text(
                        ranker.name.split(" ").first(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${ranker.score}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    // Coins Display in Podium
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         CoinIcon(size = 14.dp)
                         Spacer(modifier = Modifier.width(2.dp))
                         Text(
                             "${ranker.coins}",
                             style = MaterialTheme.typography.bodySmall,
                             color = Color.White,
                             fontWeight = FontWeight.Bold
                         )
                    }
                }
            }
        }
    }
}

@Composable
fun RankerRow(rank: Int, ranker: Ranker) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Darker surface
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$rank",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f),
                modifier = Modifier.width(36.dp)
            )
            
            OfflineAvatar(name = ranker.name, size = 40.dp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(ranker.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("${ranker.exam} â€¢ 850 Qs Attempted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                 Text("${ranker.score}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4169E1)) // Royal Blue
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     CoinIcon(size = 16.dp)
                     Spacer(modifier = Modifier.width(4.dp))
                     Text("${ranker.coins}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFD700)) // Gold coins
                 }
            }

        }
    }
}

@Composable
fun CoinIcon(size: Dp = 16.dp) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFFFD700), // Gold
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "C",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = (size.value * 0.7).sp // Responsive font size
            )
        }
    }
}

@Composable
fun OfflineAvatar(name: String, size: Dp, border: Boolean = false, borderColor: Color = Color.White) {
    // Generate a consistent color based on the name hash
    val colorIndex = kotlin.math.abs(name.hashCode()) % 5
    val avatarColor = when(colorIndex) {
        0 -> Color(0xFFE74C3C) // Red
        1 -> Color(0xFF3498DB) // Blue
        2 -> Color(0xFF2ECC71) // Green
        3 -> Color(0xFF9B59B6) // Purple
        else -> Color(0xFFF1C40F) // Yellow
    }
    
    Surface(
        shape = CircleShape,
        color = avatarColor,
        modifier = Modifier.size(size),
        border = if (border) BorderStroke(2.dp, borderColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
             Icon(
                 imageVector = Icons.Default.Person,
                 contentDescription = null,
                 tint = Color.White,
                 modifier = Modifier.size(size * 0.6f)
             )
        }
    }
}
