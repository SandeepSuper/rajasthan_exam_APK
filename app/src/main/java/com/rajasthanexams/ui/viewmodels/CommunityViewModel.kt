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

    // Currently selected exam filter (null = show all posts)
    private val _selectedExamId = MutableStateFlow<String?>(null)
    val selectedExamId: StateFlow<String?> = _selectedExamId

    init {
        fetchPosts()
    }

    fun selectExam(examId: String?) {
        _selectedExamId.value = examId
        fetchPosts(examId)
    }

    fun fetchPosts(examId: String? = _selectedExamId.value) {
        viewModelScope.launch {
            _uiState.value = CommunityUiState.Loading
            val userId = sessionManager.getUserId()
            try {
                val response = api.getCommunityPosts(userId, examId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = CommunityUiState.Success(response.body()!!)
                } else {
                    _uiState.value = CommunityUiState.Error("Failed to fetch posts: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = CommunityUiState.Error(friendlyNetworkError(e))
            }
        }
    }

    fun createPost(content: String, subject: String, category: String, examId: String? = _selectedExamId.value, onResult: (Boolean, String?) -> Unit) {
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
                    category = category,
                    examId = examId
                )
                val response = api.createPost(request)
                if (response.isSuccessful) {
                    fetchPosts()
                    onResult(true, null)
                } else {
                    // Read actual backend error message from errorBody
                    val backendMsg = try {
                        val body = response.errorBody()?.string() ?: ""
                        // Spring's ResponseStatusException wraps message in JSON: {"message":"..."}
                        // Try to extract it, fallback to raw body
                        if (body.contains("\"message\"")) {
                            org.json.JSONObject(body).optString("message", body)
                        } else {
                            body.ifBlank { response.message() }
                        }
                    } catch (e: Exception) {
                        response.message()
                    }

                    val errorMsg = when (response.code()) {
                        403  -> "PURCHASE_REQUIRED"
                        429  -> "RATE_LIMITED: $backendMsg"
                        else -> "ERROR: $backendMsg"
                    }
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                onResult(false, friendlyNetworkError(e))
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
                    val currentPosts = (_uiState.value as? CommunityUiState.Success)?.posts ?: return@launch
                    val updatedPosts = currentPosts.map { post ->
                        if (post.id == postId) {
                            val isLiked = response.body() ?: false
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
                    if (_selectedPost.value?.id == postId) {
                       _selectedPost.value = updatedPosts.find { it.id == postId }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun friendlyNetworkError(e: Exception): String {
        val msg = e.message ?: e.localizedMessage ?: ""
        if (e is java.net.UnknownHostException || msg.contains("UnknownHost", ignoreCase = true) || msg.contains("Unable to resolve host", ignoreCase = true)) {
            return "Internet connection check karein. Server se connect nahi ho pa raha."
        }
        if (e is java.net.SocketTimeoutException || msg.contains("timeout", ignoreCase = true)) {
            return "Server response time out ho gaya. Thodi der baad try karein."
        }
        if (e is java.net.ConnectException || msg.contains("failed to connect", ignoreCase = true)) {
            return "Internet connection check karein. Server available nahi hai."
        }
        return "Network issue: ${e.localizedMessage ?: "Unknown error"}"
    }
}

