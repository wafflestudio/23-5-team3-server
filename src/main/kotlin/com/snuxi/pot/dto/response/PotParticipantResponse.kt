package com.snuxi.pot.dto.response

import com.snuxi.user.model.User

data class PotParticipantResponse(
    val userId: Long,
    val username: String,
    val profileImageUrl: String?,
    val role: String // "OWNER" or "MEMBER"
) {
    companion object {
        fun from(user: User, isOwner: Boolean) = PotParticipantResponse(
            userId = user.id!!,
            username = user.username,
            profileImageUrl = user.profileImageUrl,
            role = if (isOwner) "OWNER" else "MEMBER"
        )
    }
}