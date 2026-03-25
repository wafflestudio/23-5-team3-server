package com.snuxi.admin.dto

data class UnsuspendResponse(
    val userId: Long,
    val status: String = "UNSUSPENDED"
)
