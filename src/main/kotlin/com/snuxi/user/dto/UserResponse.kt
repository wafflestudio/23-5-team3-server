package com.snuxi.user.dto

import com.snuxi.user.model.Role
import com.snuxi.user.model.User

data class UserResponse(
    val email: String,
    val username: String,
    val profileImageUrl: String?,
    val role: Role,
    val notificationEnabled: Boolean
) {
    constructor(user: User) : this(
        email = user.email,
        username = user.username,
        profileImageUrl = user.profileImageUrl,
        role = user.role,
        notificationEnabled = user.notificationEnabled
    )
}
