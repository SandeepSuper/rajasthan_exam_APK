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
import com.rajasthanexams.data.Category
import com.rajasthanexams.ui.components.HeritagePatternBackground
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajasthanexams.ui.viewmodels.CommunityViewModel
import com.rajasthanexams.ui.viewmodels.CommunityUiState
import com.rajasthanexams.data.remote.dto.CommunityPostResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(),
    exams: List<Category> = emptyList(),   // available/purchased exams for exam tabs
    onPostClick: (CommunityPostResponse) -> Unit,
    onLikeClick: (String) -> Unit = { viewModel.toggleLike(it) },
    onPurchaseRequired: ((examId: String, examTitle: String) -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val subjectFilters = listOf("All", "History", "Geography", "Art & Culture", "Polity")
    val selectedExamId by viewModel.selectedExamId.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current  // capture here for lambdas
    var showCreatePostDialog by remember { mutableStateOf(false) }
    // Purchase prompt state
    var showPurchasePrompt by remember { mutableStateOf(false) }
    var purchasePromptExamId by remember { mutableStateOf("") }
    var purchasePromptTitle by remember { mutableStateOf("") }

    // Purchase prompt dialog
    if (showPurchasePrompt) {
        AlertDialog(
            onDismissRequest = { showPurchasePrompt = false },
            title = {
                Text("🔒 Purchase Required", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "\"${purchasePromptTitle}\" ke doubts poochne ke liye pehle exam purchase karein.\n\nPurchase karne ke baad aap is exam mein unlimited doubts pooch sakte hain!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPurchasePrompt = false
                        onPurchaseRequired?.invoke(purchasePromptExamId, purchasePromptTitle)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("🛒 Purchase Karo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchasePrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { content, subject, category ->
                viewModel.createPost(content, subject, category) { success, message ->
                    if (success) {
                        showCreatePostDialog = false
                    } else if (message == "PURCHASE_REQUIRED") {
                        // User hasn't purchased this exam — show prompt
                        showCreatePostDialog = false
                        purchasePromptExamId = selectedExamId ?: ""
                        purchasePromptTitle = exams.find { it.id == selectedExamId }?.title ?: "Exam"
                        showPurchasePrompt = true
                    } else {
                        // Rate limit, network error, etc. — close dialog + show toast
                        showCreatePostDialog = false
                        val displayMsg = message
                            ?.removePrefix("RATE_LIMITED: ")
                            ?.removePrefix("ERROR: ")
                            ?: "Post nahi ho saka, dobara try karo"
                        android.widget.Toast.makeText(
                            context,
                            displayMsg,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostDialog = true },
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

                // ── Exam Tabs (All + each purchased exam) ──────────────────────
                if (exams.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = if (selectedExamId == null) 0
                            else (exams.indexOfFirst { it.id == selectedExamId } + 1).coerceAtLeast(0),
                        edgePadding = 16.dp,
                        containerColor = Color.Transparent,
                        indicator = {},
                        divider = {}
                    ) {
                        // "All" tab
                        SuggestionChip(
                            onClick = { viewModel.selectExam(null) },
                            label = { Text("🌐 All") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedExamId == null) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface,
                                labelColor = if (selectedExamId == null) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        // One tab per exam
                        exams.forEach { exam ->
                            SuggestionChip(
                                onClick = { viewModel.selectExam(exam.id) },
                                label = { Text(exam.title) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedExamId == exam.id) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                                    labelColor = if (selectedExamId == exam.id) Color.White
                                        else MaterialTheme.colorScheme.onSurface
                                ),
                                border = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }

                // ── Subject filter chips ────────────────────────────────────────
                ScrollableTabRow(
                    selectedTabIndex = subjectFilters.indexOf(selectedFilter),
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    indicator = {},
                    divider = {}
                ) {
                    subjectFilters.forEach { filter ->
                        SuggestionChip(
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedFilter == filter) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surface,
                                labelColor = if (selectedFilter == filter) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                            ),
                            border = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feed
                when (val state = uiState) {
                    is CommunityUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is CommunityUiState.Error -> {
                        com.rajasthanexams.ui.components.NetworkErrorComponent(
                            onRetry = { viewModel.fetchPosts() }
                        )
                    }
                    is CommunityUiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.posts.filter { selectedFilter == "All" || it.subject == selectedFilter }) { post ->
                                DoubtPostCard(post, onPostClick, onLikeClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("History") }
    // Using a simple list for subjects for now
    val subjects = listOf("History", "Geography", "Art & Culture", "Polity", "Economics", "Other")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ask a Doubt") },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Your Question") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedSubject,
                        onValueChange = { },
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        subjects.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedSubject = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (content.isNotBlank()) {
                        onSubmit(content, selectedSubject, "General") // Default category for now
                    }
                }
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtPostCard(
    post: CommunityPostResponse, 
    onClick: (CommunityPostResponse) -> Unit,
    onLikeClick: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { onClick(post) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info (Same as before)
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
                    Text("Just now", style = MaterialTheme.typography.labelSmall, color = Color.Gray) // You can format post.createdAt
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
                // Like Button
                IconButton(onClick = { onLikeClick(post.id) }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.ThumbUp, 
                        contentDescription = "Like",
                        tint = if (post.isLiked) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.upvotes}", color = if (post.isLiked) MaterialTheme.colorScheme.primary else Color.Gray)
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Comment Count
                Icon(Icons.Default.Comment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.commentCount}", color = Color.Gray)

                Spacer(modifier = Modifier.width(24.dp))

                // View Count
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.viewCount}", color = Color.Gray)
            }
        }
    }
}
