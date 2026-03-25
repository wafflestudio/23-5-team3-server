package com.snuxi.admin.dto

import java.time.Instant

data class AdminUserListResponse(
    val content: List<AdminUserListItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

data class AdminUserListItem(
    val id: Long,
    val email: String,
    val username: String,
    val role: String,
    val createdAt: Instant?,
    val suspended: Boolean
)
