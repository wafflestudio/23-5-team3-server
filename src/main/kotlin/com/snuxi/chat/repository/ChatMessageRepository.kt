package com.snuxi.chat.repository

import com.snuxi.chat.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    // cursor base(cursor = id)
    fun findByPotIdAndIdLessThanOrderByIdDesc(
        potId: Long,
        id: Long,
        pageable: Pageable
    ): Page<ChatMessage>

    // 최신 N개 조회(no cursor)
    fun findByPotIdOrderByIdDesc(
        potId: Long,
        pageable: Pageable
    ): Page<ChatMessage>
}