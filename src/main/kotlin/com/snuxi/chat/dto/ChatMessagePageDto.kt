package com.snuxi.chat.dto

import java.time.LocalDateTime

data class ChatMessagePageDto (
    val items: List<ChatMessageItemDto>,
    val nextCursor: Long?,
    val hasNext: Boolean,
    val readStatuses: Map<Long, Long> = emptyMap()
)

data class ChatMessageItemDto (
    val id: Long,
    val potId: Long,
    val senderId: Long,
    val text: String,
    val datetimeSendAt: LocalDateTime
)
