package com.rajasthanexams.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajasthanexams.data.local.SessionManager
import com.rajasthanexams.data.remote.RetrofitClient
import com.rajasthanexams.data.remote.dto.CommunityPostResponse
import com.rajasthanexams.data.remote.dto.CreatePostRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CommunityUiState {
    object Loading : CommunityUiState()
    data class Success(val posts: List<CommunityPostResponse>) : CommunityUiState()
    data class Error(val message: String) : CommunityUiState()
}

class CommunityViewModel(application: Application) : AndroidViewModel(application) {
    private val api = RetrofitClient.getService()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState

    private val _createPostState = MutableStateFlow<Boolean?>(null) // null=idle, true=success, false=fail
    val createPostState: StateFlow<Boolean?> = _createPostState

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.value = CommunityUiState.Loading
            val userId = sessionManager.getUserId()
            try {
                val response = api.getCommunityPosts(userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = CommunityUiState.Success(response.body()!!)
                } else {
                    _uiState.value = CommunityUiState.Error("Failed to fetch posts: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = CommunityUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun createPost(content: String, subject: String, category: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            val userName = sessionManager.getUserName() ?: "Unknown User"
            val userProfilePic = sessionManager.getProfilePicture()

            if (userId == null) {
                onResult(false, "User not logged in")
                return@launch
            }

            try {
                val request = CreatePostRequest(
                    userId = userId,
                    userName = userName,
                    userProfilePicture = userProfilePic,
                    content = content,
                    subject = subject,
                    category = category
                )
                val response = api.createPost(request)
                if (response.isSuccessful) {
                    fetchPosts() // Refresh list
                    onResult(true, null)
                } else {
                    onResult(false, "Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    // Comment Logic
    private val _selectedPost = MutableStateFlow<CommunityPostResponse?>(null)
    val selectedPost: StateFlow<CommunityPostResponse?> = _selectedPost

    private val _comments = MutableStateFlow<List<com.rajasthanexams.data.remote.dto.CommunityCommentResponse>>(emptyList())
    val comments: StateFlow<List<com.rajasthanexams.data.remote.dto.CommunityCommentResponse>> = _comments

    fun selectPost(post: CommunityPostResponse) {
        _selectedPost.value = post
        incrementView(post.id) // Increment view when opening details
        fetchComments(post.id)
    }

    fun fetchComments(postId: String) {
        viewModelScope.launch {
            try {
                val response = api.getComments(postId)
                if (response.isSuccessful && response.body() != null) {
                    _comments.value = response.body()!!
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addComment(postId: String, content: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            val userName = sessionManager.getUserName() ?: "Unknown"
            val userProfile = sessionManager.getProfilePicture()

            if (userId == null) return@launch

             val request = com.rajasthanexams.data.remote.dto.CreateCommentRequest(
                userId = userId,
                userName = userName,
                userProfilePicture = userProfile,
                content = content
            )
            try {
                val response = api.addComment(postId, request)
                if (response.isSuccessful) {
                    fetchComments(postId) // Refresh comments
                    fetchPosts() // Refresh post list (for comment count)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun incrementView(postId: String) {
        viewModelScope.launch {
            try {
                api.incrementView(postId)
                // Optionally update local state if needed, but view count update might not be critical to show immediately
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId() ?: return@launch
            try {
                val response = api.toggleLike(postId, userId)
                if (response.isSuccessful) {
                    // Update local state to reflect change immediately for better UX
                    val currentPosts = (_uiState.value as? CommunityUiState.Success)?.posts ?: return@launch
                    val updatedPosts = currentPosts.map { post ->
                        if (post.id == postId) {
                            val isLiked = response.body() ?: false
                            // If liked (true), increment upvotes, else decrement
                            // But backend returns isLiked state.
                            // We need to adjust count based on previous state or just response?
                            // Response is boolean isLiked.
                            // If isLiked is true, count = count + 1 (if it was false)
                            // Ideally backend returns new count, but we can approximate
                            val oldIsLiked = post.isLiked
                            val newIsLiked = isLiked
                            var newCount = post.upvotes
                            if (!oldIsLiked && newIsLiked) newCount++
                            if (oldIsLiked && !newIsLiked) newCount = (newCount - 1).coerceAtLeast(0)
                            
                            post.copy(isLiked = newIsLiked, upvotes = newCount)
                        } else {
                            post
                        }
                    }
                    _uiState.value = CommunityUiState.Success(updatedPosts)
                    
                    // Also update selected post if it matches
                    if (_selectedPost.value?.id == postId) {
                       _selectedPost.value = updatedPosts.find { it.id == postId }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

