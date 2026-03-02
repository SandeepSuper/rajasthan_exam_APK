package com.rajasthanexams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.rajasthanexams.ui.components.CategoryItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.rajasthanexams.data.Test
import com.rajasthanexams.data.TestType
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.viewmodels.TestsByTypeUiState
import com.rajasthanexams.ui.viewmodels.TestsByTypeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsByTypeScreen(
    testType: TestType,
    onBackClick: () -> Unit,
    onStartPractice: (Test) -> Unit,
    viewModel: TestsByTypeViewModel = viewModel()
) {
    if (testType == TestType.LIVE) {
        LiveTestsScreen(
            onBackClick = onBackClick,
            onStartPractice = onStartPractice,
            viewModel = viewModel
        )
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(testType) {
        viewModel.loadData(testType)
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initRegistration(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getTestTypeTitle(testType), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        HeritagePatternBackground(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                if (uiState is TestsByTypeUiState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState is TestsByTypeUiState.Success) {
                    val state = uiState as TestsByTypeUiState.Success
                    val categories = state.categories
                    
                    if (categories.isNotEmpty()) {
                        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
                            initialPage = state.selectedCategoryIndex
                        ) { categories.size }

                        // Sync pager state with ViewModel
                        LaunchedEffect(pagerState.currentPage) {
                            if (pagerState.currentPage != state.selectedCategoryIndex) {
                                viewModel.onCategorySelected(pagerState.currentPage)
                            }
                            viewModel.loadTestsForCategory(categories[pagerState.currentPage], testType)
                        }
                        if (categories.size <= 3) {
                            TabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary,
                                indicator = { tabPositions ->
                                    if (pagerState.currentPage < tabPositions.size) {
                                        TabRowDefaults.Indicator(
                                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            ) {
                                categories.forEachIndexed { index, category ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = { 
                                            scope.launch { 
                                                pagerState.animateScrollToPage(index) 
                                            }
                                        },
                                        text = { 
                                            Text(
                                                category.title, 
                                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            ) 
                                        }
                                    )
                                }
                            }
                        } else {
                            ScrollableTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary,
                                edgePadding = 16.dp,
                                indicator = { tabPositions ->
                                    if (pagerState.currentPage < tabPositions.size) {
                                        TabRowDefaults.Indicator(
                                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            ) {
                                categories.forEachIndexed { index, category ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = { 
                                            scope.launch { 
                                                pagerState.animateScrollToPage(index) 
                                            }
                                        },
                                        text = { 
                                            Text(
                                                category.title, 
                                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                            ) 
                                        }
                                    )
                                }
                            }
                        }

                        // Load data for current page if needed
                        LaunchedEffect(pagerState.currentPage) {
                            viewModel.loadTestsForCategory(categories[pagerState.currentPage], testType)
                        }
                    
                        // 2. Tests List via Pager
                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val currentCategory = categories[page]
                            val categoryTests = state.tests[currentCategory.id]

                            if (categoryTests == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (categoryTests.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().height(200.dp), 
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No ${getTestTypeTitle(testType)} available for this exam yet.", color = Color.Gray)
                                            }
                                        }
                                    } else {
                                        items(categoryTests) { test ->
                                            com.rajasthanexams.ui.screens.TestCard(
                                                test = test, 
                                                onClick = { onStartPractice(test) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No categories found", color = Color.Gray)
                        }
                    }
                } else if (uiState is TestsByTypeUiState.Error) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading data", color = Color.Red)
                    }
                }
            }
        }
    }
}

fun getTestTypeTitle(type: TestType): String {
    return when(type) {
        TestType.MOCK -> "Mock Tests"
        TestType.TOPIC -> "Topic Tests"
        TestType.FULL -> "Full Length Tests"
        TestType.PYQ -> "Previous Year Papers"
        TestType.LIVE -> "Live Tests"
        TestType.DAILY_QUIZ -> "Daily Quizzes"
    }
}
