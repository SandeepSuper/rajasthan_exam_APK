package com.rajasthanexams.data.remote.dto

data class CommunityPostResponse(
    val id: String,
    val userId: String,
    val userName: String,
    val userProfilePicture: String?,
    val content: String,
    val subject: String,
    val category: String,
    val upvotes: Int,
    val commentCount: Int,
    val viewCount: Int,
    val isLiked: Boolean,
    val verifiedAnswer: String?,
    val createdAt:String
)

data class CreatePostRequest(
    val userId: String,
    val userName: String,
    val userProfilePicture: String?,
    val content: String,
    val subject: String,
    val category: String
)

data class CommunityCommentResponse(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val userProfilePicture: String?,
    val content: String,
    val createdAt: String
)

data class CreateCommentRequest(
    val userId: String,
    val userName: String,
    val userProfilePicture: String?,
    val content: String
)
