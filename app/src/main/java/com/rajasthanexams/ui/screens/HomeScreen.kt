package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.rajasthanexams.data.Category
import com.rajasthanexams.data.MockData
import com.rajasthanexams.data.Test
import com.rajasthanexams.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isDarkTheme: Boolean = false,
    profileImageUrl: String? = null,
    onExamClick: (Test) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onTestTypeClick: (com.rajasthanexams.data.TestType) -> Unit,
    onNotificationClick: () -> Unit,
    onReferralClick: () -> Unit,
    onCurrentAffairsClick: () -> Unit,
    onPromotionClick: (Category) -> Unit = {},
    showPurchasedExams: Boolean = false,
    viewModel: com.rajasthanexams.ui.viewmodels.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    notificationsViewModel: com.rajasthanexams.ui.viewmodels.NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Live Data Observation
    val uiState by viewModel.uiState.collectAsState()
    val registeredTests by viewModel.registeredState.collectAsState()
    val notificationState by notificationsViewModel.uiState.collectAsState()
    
    val unreadNotifications = remember(notificationState) {
        if (notificationState is com.rajasthanexams.ui.viewmodels.NotificationsUiState.Success) {
            (notificationState as com.rajasthanexams.ui.viewmodels.NotificationsUiState.Success).notifications.count { !it.isRead }
        } else 0
    }
    
    // Filter logic works on the *State* list now
    val filteredTests = remember(searchQuery, uiState, showPurchasedExams) {
        val allTests = if (uiState is com.rajasthanexams.ui.viewmodels.HomeUiState.Success) {
            (uiState as com.rajasthanexams.ui.viewmodels.HomeUiState.Success).tests
        } else {
            emptyList()
        }

        // On the Tests tab, exclude purchased tests from "Recommended" — they show in "My Purchases"
        val tests = if (showPurchasedExams) allTests.filter { !it.isPurchased } else allTests

        if (searchQuery.isBlank()) {
            tests
        } else {
            tests.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Header Section
            item {
                // Header Section with Gradient
                val headerBrush = if (isDarkTheme) {
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF))
                    )
                } else {
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(RoyalBlue, Color(0xFF1E3799))
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = headerBrush,
                            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                        )
                        .padding(24.dp)
                        .padding(bottom = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Rajasthan Test Hub",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Practice Smart • Rank Fast",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onNotificationClick) {
                                    Box(modifier = Modifier.size(32.dp)) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = Color.White,
                                            modifier = Modifier.align(Alignment.Center).size(28.dp)
                                        )
                                        
                                        if (unreadNotifications > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Red,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(16.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = unreadNotifications.toString(),
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    val avatarRes = com.rajasthanexams.ui.components.AvatarHelper.getDrawableRes(profileImageUrl)
                                    when {
                                        avatarRes != null -> {
                                            // Local avatar drawable
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = avatarRes),
                                                contentDescription = "Profile",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }
                                        !profileImageUrl.isNullOrBlank() -> {
                                            // Remote URL
                                            AsyncImage(
                                                model = profileImageUrl,
                                                contentDescription = "Profile",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }
                                        else -> {
                                            // Fallback
                                            Icon(
                                                Icons.Default.AccountCircle,
                                                contentDescription = "Profile",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Search Bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text("Search exams, tests...", color = Color.Gray)
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Promotional Carousel
            item {
                val promoCategories = if (uiState is com.rajasthanexams.ui.viewmodels.HomeUiState.Success) {
                    (uiState as com.rajasthanexams.ui.viewmodels.HomeUiState.Success)
                        .categories
                        .filter { it.isPremium && !it.isPurchased && it.price > 0 }
                } else emptyList()
                PromotionCarousel(
                    categories = promoCategories,
                    onItemClick = onPromotionClick
                )
            }

            // Daily Current Affairs (News)
            item {
               CurrentAffairsBanner(onClick = onCurrentAffairsClick)
            }

            // Refer & Earn Banner
            item {
               ReferAndEarnCard(onClick = onReferralClick)
            }

            // Categories Section
            item {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Text(
                        "Select Category",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Replaced LazyRow with Row + Scroll for stability
                    // Get dynamic categories from state if available
                    val categories = if (uiState is com.rajasthanexams.ui.viewmodels.HomeUiState.Success) {
                        (uiState as com.rajasthanexams.ui.viewmodels.HomeUiState.Success).categories
                    } else {
                        MockData.categories
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        categories.forEach { category ->
                            CategoryItem(category = category, onClick = { onCategoryClick(category) })
                        }
                    }
                }
            }

            // Test Type Quick Navigation Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Select Test Type",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        data class TestTypeInfo(
                            val label: String,
                            val type: com.rajasthanexams.data.TestType,
                            val color: Color,
                            val icon: androidx.compose.ui.graphics.vector.ImageVector
                        )
                        val types = listOf(
                            TestTypeInfo("Mock\nTests", com.rajasthanexams.data.TestType.MOCK, Color(0xFFE67E22), Icons.Default.Assignment),
                            TestTypeInfo("Topic\nTests", com.rajasthanexams.data.TestType.TOPIC, Color(0xFF27AE60), Icons.Default.MenuBook),
                            TestTypeInfo("Full\nTests", com.rajasthanexams.data.TestType.FULL, Color(0xFF2980B9), Icons.Default.LibraryBooks),
                            TestTypeInfo("PYQ\nPapers", com.rajasthanexams.data.TestType.PYQ, Color(0xFF8E44AD), Icons.Default.History),
                            TestTypeInfo("Daily\nQuiz", com.rajasthanexams.data.TestType.DAILY_QUIZ, Color(0xFF009688), Icons.Default.Bolt),
                            TestTypeInfo("Live\nTests", com.rajasthanexams.data.TestType.LIVE, Color(0xFFEB5757), Icons.Default.PlayArrow)
                        )
                        types.forEach { info ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .clickable { onTestTypeClick(info.type) }
                            ) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = info.color.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, info.color.copy(alpha = 0.35f)),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = info.icon,
                                            contentDescription = info.label,
                                            tint = info.color,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    info.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // My Purchases Section (only shown on Tests tab)
            if (showPurchasedExams) {
                val purchasedTests = if (uiState is com.rajasthanexams.ui.viewmodels.HomeUiState.Success) {
                    (uiState as com.rajasthanexams.ui.viewmodels.HomeUiState.Success).tests.filter { it.isPurchased }
                } else emptyList()

                if (purchasedTests.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "My Purchases",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.material3.Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    "${purchasedTests.size}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    items(purchasedTests) { test ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            TestCard(test = test, onClick = { onExamClick(test) })
                        }
                    }
                } else if (uiState is com.rajasthanexams.ui.viewmodels.HomeUiState.Success) {
                    item {
                        androidx.compose.material3.Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = androidx.compose.material3.CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎓", style = MaterialTheme.typography.displaySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No Purchased Exams Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Browse categories below to unlock premium tests",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Popular Tests Section Header
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Recommended Tests",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
                
            // Tests List or Loading
            if (uiState is com.rajasthanexams.ui.viewmodels.HomeUiState.Loading) {
                 item {
                     Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator()
                     }
                 }
            } else {
                 items(filteredTests) { test ->
                     Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                         TestCard(test = test, onClick = { onExamClick(test) })
                     }
                 }
                 
                 if (filteredTests.isEmpty() && uiState is com.rajasthanexams.ui.viewmodels.HomeUiState.Success) {
                     item {
                         Text(
                             "No tests available.",
                             style = MaterialTheme.typography.bodyMedium,
                             modifier = Modifier.padding(16.dp),
                             color = Color.Gray
                         )
                     }
                 }
            }
        }
    }
}
@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                if (!category.iconUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = category.iconUrl,
                        contentDescription = category.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            category.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            "${category.testsAvailable} Tests",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun TestCard(test: Test, onClick: () -> Unit) {
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
                val (buttonLabel, buttonColor) = when {
                    test.isPremium && !test.isPurchased -> "Unlock Premium" to MaterialTheme.colorScheme.error
                    test.isAttempted -> "View Result" to MaterialTheme.colorScheme.tertiary
                    else -> "Start Now" to MaterialTheme.colorScheme.secondary
                }
                Text(
                    buttonLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = buttonColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromotionCarousel(
    categories: List<Category> = emptyList(),
    onItemClick: (Category) -> Unit = {}
) {
    // Gradient palette — cycles through if more exams than colors
    val gradients = listOf(
        Pair(Color(0xFF6A11CB), Color(0xFF2575FC)),
        Pair(Color(0xFFFF512F), Color(0xFFDD2476)),
        Pair(Color(0xFF11998e), Color(0xFF38ef7d)),
        Pair(Color(0xFFf7971e), Color(0xFFffd200)),
        Pair(Color(0xFF1A1A2E), Color(0xFF16213E))
    )

    // Use live data if available, otherwise fall back to mock
    val items: List<Pair<String, Any>> = if (categories.isNotEmpty()) {
        categories.map { it.title to (it as Any) }
    } else {
        MockData.promotions.map { it.title to (it as Any) }
    }

    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    // Auto-scroll
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = Modifier.padding(top = 24.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) { page ->
            val gradient = gradients[page % gradients.size]
            val item = items[page].second
            if (item is Category) {
                val discountedAmt = if (item.discountPercent > 0)
                    kotlin.math.round(item.price * (1.0 - item.discountPercent / 100.0)).toDouble()
                else null
                PromotionCard(
                    title = item.title,
                    subtitle = if (item.discountPercent == 0) "₹${item.price.toInt()} · Unlock Now" else "",
                    badge = if (item.discountPercent > 0) "${item.discountPercent}% OFF" else "PREMIUM",
                    mrp = if (item.discountPercent > 0) item.price else null,
                    discountedPrice = discountedAmt,
                    colorStart = gradient.first,
                    colorEnd = gradient.second,
                    onClick = { onItemClick(item) }
                )
            } else {
                val promo = item as com.rajasthanexams.data.Promotion
                PromotionCard(
                    title = promo.title,
                    subtitle = promo.subtitle,
                    badge = promo.discount,
                    colorStart = promo.colorStart,
                    colorEnd = promo.colorEnd
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicators
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
        }
    }
}

@Composable
fun PromotionCard(
    title: String,
    subtitle: String,
    badge: String,
    colorStart: Color,
    colorEnd: Color,
    mrp: Double? = null,
    discountedPrice: Double? = null,
    onClick: () -> Unit = {}
) {
    val hasDiscount = mrp != null && discountedPrice != null

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(colorStart, colorEnd),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(
                            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY
                        )
                    )
                )
        ) {
            // Decorative blurred circles
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-40).dp, y = (-40).dp)
                    .background(Color.White.copy(alpha = 0.07f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 25.dp, y = 25.dp)
                    .background(Color.White.copy(alpha = 0.09f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // ── TOP: Title + Badge ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            badge,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // ── MIDDLE: Price Chip (the focal point) ──
                if (hasDiscount) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // MRP crossed out
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "MRP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.55f)
                                )
                                Text(
                                    "\u20b9${mrp!!.toInt()}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ),
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Divider
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(34.dp)
                                    .background(Color.White.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            // Discounted price (large, bold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "You Pay",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    "\u20b9${discountedPrice!!.toInt()}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Savings green pill
                            Surface(
                                color = Color(0xFF00C853),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Save \u20b9${(mrp!! - discountedPrice!!).toInt()}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                } else if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // ── BOTTOM: Label + CTA ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "\u2726 Full Course Included",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            "Unlock Now \u2192",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferAndEarnCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Refer & Earn Free Mock Tests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    "Invite friends to unlock premium!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1B5E20)
                )
            }
        }
    }
}


@Composable
fun CurrentAffairsBanner(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Light Blue
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                     Icons.Default.MenuBook,
                     contentDescription = null,
                     tint = Color(0xFF1565C0),
                     modifier = Modifier.size(32.dp)
                 )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Daily Rajasthan Current Affairs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Text(
                    "Stay updated with exam-focused news!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0D47A1)
                )
            }
        }
    }
}
