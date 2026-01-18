package com.snuxi.chat.repository

import com.snuxi.chat.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByPotIdAndIdLessThanOrderByIdDesc(
        potId: Long,
        id: Long,
        pageable: Pageable
    ): Page<ChatMessage>
}