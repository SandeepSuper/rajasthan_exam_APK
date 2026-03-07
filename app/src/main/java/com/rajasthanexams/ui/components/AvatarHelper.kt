package com.rajasthanexams.ui.components

import com.rajasthanexams.R

/**
 * Maps avatar ID strings (e.g. "avatar_1") to drawable resource IDs.
 * Also provides the ordered list of all avatars for the picker UI.
 */
object AvatarHelper {

    val allAvatars = listOf(
        "avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5",
        "avatar_6", "avatar_7", "avatar_8", "avatar_9", "avatar_10"
    )

    fun getDrawableRes(avatarId: String?): Int? {
        return when (avatarId) {
            "avatar_1"  -> R.drawable.avatar_1
            "avatar_2"  -> R.drawable.avatar_2
            "avatar_3"  -> R.drawable.avatar_3
            "avatar_4"  -> R.drawable.avatar_4
            "avatar_5"  -> R.drawable.avatar_5
            "avatar_6"  -> R.drawable.avatar_6
            "avatar_7"  -> R.drawable.avatar_7
            "avatar_8"  -> R.drawable.avatar_8
            "avatar_9"  -> R.drawable.avatar_9
            "avatar_10" -> R.drawable.avatar_10
            else        -> null
        }
    }

    /** Returns true if the profile picture value is one of the predefined avatars. */
    fun isAvatar(profilePicture: String?): Boolean =
        profilePicture != null && profilePicture.startsWith("avatar_")
}
