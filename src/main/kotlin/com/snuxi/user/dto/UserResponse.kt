package com.snuxi.user.dto

import com.snuxi.user.model.Role
import com.snuxi.user.model.User
import java.time.LocalDateTime

data class UserResponse(
    val email: String,
    val username: String,
    val profileImageUrl: String?,
    val role: Role,
    val notificationEnabled: Boolean,
    val isSuspended: Boolean,
    val suspendedUntil: LocalDateTime?
) {
    constructor(user: User) : this(
        email = user.email,
        username = user.username,
        profileImageUrl = user.profileImageUrl,
        role = user.role,
        notificationEnabled = user.notificationEnabled,
        isSuspended = user.isSuspended(),
        suspendedUntil = user.suspendedUntil
    )
}
