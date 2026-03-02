package com.rajasthanexams.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.rajasthanexams.data.Test
import com.rajasthanexams.data.TestType
import com.rajasthanexams.ui.components.HeritagePatternBackground
import com.rajasthanexams.ui.components.LiveTestCard
import com.rajasthanexams.ui.viewmodels.TestsByTypeUiState
import com.rajasthanexams.ui.viewmodels.TestsByTypeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTestsScreen(
    onBackClick: () -> Unit,
    onStartPractice: (Test) -> Unit,
    viewModel: TestsByTypeViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ongoing", "Attempted")
    
    // Load Data specifically for Live Tests
    LaunchedEffect(Unit) {
        viewModel.loadData(TestType.LIVE)
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initRegistration(context)
    }

    val uiState by viewModel.uiState.collectAsState()
    val registeredTests by viewModel.registeredState.collectAsState()

    HeritagePatternBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Live Panel", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Gray
                                ) 
                            }
                        )
                    }
                }
                
                // Content
                when (uiState) {
                    is TestsByTypeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is TestsByTypeUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error loading live tests", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is TestsByTypeUiState.Success -> {
                        val state = uiState as TestsByTypeUiState.Success
                        val allTests = state.tests.values.flatten().distinctBy { it.id }
                        
                        val filteredTests = if (selectedTab == 0) {
                            // Ongoing: Not attempted AND Not Ended
                            allTests.filter { test -> 
                                val isEnded = try {
                                    if (test.endsAt != null) java.time.LocalDateTime.now().isAfter(java.time.LocalDateTime.parse(test.endsAt)) else false
                                } catch (e: Exception) { false }
                                !test.isAttempted && !isEnded
                            }
                        } else {
                            // Attempted - Sorted by EndsAt Descending (Most Recent first)
                            allTests.filter { it.isAttempted }
                                .sortedByDescending { test ->
                                    try {
                                        if (test.endsAt != null) java.time.LocalDateTime.parse(test.endsAt) else java.time.LocalDateTime.MIN
                                    } catch (e: Exception) { java.time.LocalDateTime.MIN }
                                }
                        }
                        
                        if (filteredTests.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No ${tabs[selectedTab].lowercase()} live tests found.", 
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredTests) { test ->
                                    LiveTestCard(
                                        test = test,
                                        isRegistered = registeredTests.contains(test.id),
                                        onClick = { onStartPractice(test) },
                                        onRegister = { viewModel.registerForTest(test.id) }
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
