package com.snuxi.chat.dto

data class ReadStatusUpdateDto(
    val type: String = "READ",
    val userId: Long,
    val lastReadMessageId: Long
)
