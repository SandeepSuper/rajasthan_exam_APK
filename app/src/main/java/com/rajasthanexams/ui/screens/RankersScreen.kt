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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.rajasthanexams.data.local.SessionManager
import com.rajasthanexams.ui.components.CoinIcon
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.components.AvatarHelper
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
data class Ranker(val userId: String, val name: String, val score: Int, val totalMarks: Int = 0, val exam: String, val coins: Int, val avatarUrl: String = "", val rank: Int = 0)

@Composable
fun RankersScreen(
    testId: String,
    userTestCoins: Int? = null,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: com.rajasthanexams.ui.viewmodels.LeaderboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    androidx.compose.runtime.LaunchedEffect(testId) {
        viewModel.fetchLeaderboard(testId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        HeritagePatternBackground {
            when (val state = uiState) {
                is com.rajasthanexams.ui.viewmodels.LeaderboardUiState.Loading -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                     }
                }
                is com.rajasthanexams.ui.viewmodels.LeaderboardUiState.Error -> {
                    com.rajasthanexams.ui.components.NetworkErrorComponent(
                        onRetry = { viewModel.fetchLeaderboard(testId) }
                    )
                }
                is com.rajasthanexams.ui.viewmodels.LeaderboardUiState.Success -> {
                    val rankers = state.rankers
                    
                    if (rankers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    if (testId.isNotEmpty()) "No one has attempted this test yet.\nBe the first!"
                                    else "No leaderboard data yet.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { viewModel.fetchLeaderboard(testId) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    } else {
                        val topThree = rankers.take(3)
                        val others = rankers.drop(3)
                        
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
                                // Back Button
                                androidx.compose.material3.IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.align(Alignment.TopStart).offset(x = (-12).dp, y = (-12).dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack, 
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("Hall of Fame", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (testId.isNotEmpty()) "Test Leaderboard" else "Global Leaderboard",
                                        style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f)
                                    )
                                    
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
                                                rank = topThree[1].rank,
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
                                                rank = topThree[0].rank,
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
                                                rank = topThree[2].rank,
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
                                items(others) { ranker ->
                                    RankerRow(rank = ranker.rank, ranker = ranker)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Sticky Footer (Only show in Success state)
        if (uiState is com.rajasthanexams.ui.viewmodels.LeaderboardUiState.Success) {
             val rankers = (uiState as com.rajasthanexams.ui.viewmodels.LeaderboardUiState.Success).rankers
             if (rankers.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 16.dp, // Higher elevation
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val sessionManager = com.rajasthanexams.data.local.SessionManager(context)
                    val currentUserId = sessionManager.getUserId()
                    val currentUserName = sessionManager.getUserName() ?: "You"
                    
                    // Find user in the list (which is already sorted by rank 1..N)
                    // The API returns ordered list.
                    val userRankIndex = rankers.indexOfFirst { it.userId == currentUserId }
                    val userRanker = if (userRankIndex != -1) rankers[userRankIndex] else null
                    val displayRank = if (userRanker != null) "#${userRanker.rank}" else "Not Ranked"
                    val displayScore = if (userRanker != null) "${userRanker.score}" else "-"
                    val displayCoins = if (userRanker != null) "${userRanker.coins}" else "-"

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                         // 1. Rank (Left)
                         Text(
                             displayRank, 
                             style = MaterialTheme.typography.titleLarge, 
                             fontWeight = FontWeight.Bold, 
                             color = Color.White,
                             modifier = Modifier.padding(end = 16.dp)
                         )

                         // 2. Avatar & Name (Middle)
                         Row(
                             verticalAlignment = Alignment.CenterVertically,
                             modifier = Modifier.weight(1f)
                         ) {
                             val userProfilePic = sessionManager.getProfilePicture()
                             OfflineAvatar(name = currentUserName, size = 48.dp, border = true, avatarUrl = userProfilePic)
                             Spacer(modifier = Modifier.width(12.dp))
                             
                             Column {
                                 Text("$currentUserName (You)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                 // Optional: Show Score or Exam info here if ranker exists
                                 if (userRanker != null) {
                                     Text("${userRanker.exam}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha=0.7f))
                                 }
                             }
                         }
                         
                         // 3. Coins (Right)
                         Column(horizontalAlignment = Alignment.End) {
                             // Logic for coin display
                             val isTestSpecific = testId.isNotEmpty()
                             var totalUserCoins by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(sessionManager.getCoins()) }
                             
                             val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
                             androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                                 val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                                     if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                         totalUserCoins = sessionManager.getCoins()
                                     }
                                 }
                                 lifecycleOwner.lifecycle.addObserver(observer)
                                 onDispose {
                                     lifecycleOwner.lifecycle.removeObserver(observer)
                                 }
                             }

                             val coinsText = if (isTestSpecific) {
                                 userRanker?.coins?.toString() ?: userTestCoins?.toString() ?: "-"
                             } else {
                                 // Use server coins if user is found in leaderboard, else fallback to local
                                 "${userRanker?.coins ?: totalUserCoins}"
                             }
                             
                             val label = if (isTestSpecific) "Earned" else "Total Balance"

                             // Coin Row
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 Text(
                                     coinsText, 
                                     style = MaterialTheme.typography.titleLarge, 
                                     color = Color(0xFFFFD700), // Gold
                                     fontWeight = FontWeight.Bold
                                 )
                                 Spacer(modifier = Modifier.width(4.dp))
                                 CoinIcon(size = 20.dp)
                             }
                             Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                         }
                    }
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
                 OfflineAvatar(name = ranker.name, size = 60.dp, border = true, borderColor = borderColor, avatarUrl = ranker.avatarUrl)
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
                        if (ranker.totalMarks > 0) "${ranker.score}/${ranker.totalMarks}" else "${ranker.score}",
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
            
            OfflineAvatar(name = ranker.name, size = 40.dp, avatarUrl = ranker.avatarUrl)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(ranker.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(ranker.exam, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                 Text(
                     if (ranker.totalMarks > 0) "${ranker.score}/${ranker.totalMarks}" else "${ranker.score}",
                     style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4169E1)
                 )
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
fun OfflineAvatar(name: String, size: Dp, border: Boolean = false, borderColor: Color = Color.White, avatarUrl: String? = null) {
    val colorIndex = kotlin.math.abs(name.hashCode()) % 5
    val avatarColor = when (colorIndex) {
        0 -> Color(0xFFE74C3C)
        1 -> Color(0xFF3498DB)
        2 -> Color(0xFF2ECC71)
        3 -> Color(0xFF9B59B6)
        else -> Color(0xFFF1C40F)
    }

    val avatarRes = AvatarHelper.getDrawableRes(avatarUrl)

    Surface(
        shape = CircleShape,
        color = if (avatarRes != null) Color.White else avatarColor,
        modifier = Modifier.size(size),
        border = if (border) BorderStroke(2.dp, borderColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                // Predefined avatar drawable
                avatarRes != null -> Image(
                    painter = androidx.compose.ui.res.painterResource(id = avatarRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                // Real image URL (legacy)
                !avatarUrl.isNullOrEmpty() -> coil.compose.AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Default: initial letter or person icon
                else -> Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}
