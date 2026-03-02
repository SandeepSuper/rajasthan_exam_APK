package com.rajasthanexams.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.rajasthanexams.ui.viewmodels.CommunityViewModel
import com.rajasthanexams.data.remote.dto.CommunityPostResponse
import com.rajasthanexams.data.remote.dto.CommunityCommentResponse
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onBackClick: () -> Unit,
    viewModel: CommunityViewModel
) {
    val selectedPost by viewModel.selectedPost.collectAsState()
    val comments by viewModel.comments.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    if (selectedPost == null) {
        // Fallback if no post selected (shouldn't happen with proper nav)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No post selected")
            Button(onClick = onBackClick) {
                Text("Go Back")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            CommentInputBar(
                text = commentText,
                onTextChange = { commentText = it },
                onSend = {
                    if (commentText.isNotBlank()) {
                        isSending = true
                        viewModel.addComment(selectedPost!!.id, commentText) { success ->
                            isSending = false
                            if (success) {
                                commentText = ""
                            }
                        }
                    }
                },
                isSending = isSending
            )
        }
    ) { paddingAndInsets ->
        
        // Manual padding adjustment if needed, but Scaffold paddingValues usually sufficient
        // Applying padding to LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingAndInsets)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Post Header
            item {
                PostDetailHeader(
                    post = selectedPost!!,
                    onLikeClick = { viewModel.toggleLike(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Answers (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Comments List
            items(comments) { comment ->
                CommentItem(comment = comment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PostDetailHeader(
    post: CommunityPostResponse,
    onLikeClick: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info (same as before)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.userProfilePicture.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(post.userProfilePicture),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = post.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    // You might want to format date here
                     Text(text = post.subject, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = post.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge)

            if (!post.verifiedAnswer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp)) // Light Green
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Verified Answer: ${post.verifiedAnswer}",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
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

                // View Count
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.viewCount}", color = Color.Gray)
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommunityCommentResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!comment.userProfilePicture.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(comment.userProfilePicture),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                         Text(
                            text = comment.userName.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = comment.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Write your answer...") },
            modifier = Modifier
                .weight(1f),
            maxLines = 3,
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank() && !isSending,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
