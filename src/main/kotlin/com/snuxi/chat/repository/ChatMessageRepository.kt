package com.snuxi.chat.repository

import com.snuxi.chat.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

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

    // 안 읽은 개수 세기
    fun countByPotIdAndIdGreaterThan(
        potId: Long,
        lastReadMessageId: Long
    ): Long

    // for debug
    fun countByPotId(potId: Long): Long
    
    // 이전 50개 + 본 채팅 1개 = 51개 가져오기
    fun findByPotIdAndIdLessThanEqualOrderByIdDesc(
        potId: Long,
        id: Long,
        pageable: Pageable
    ): Page<ChatMessage>

    @Query("SELECT HOUR(m.datetimeSendAt) as hr, COUNT(m) FROM ChatMessage m GROUP BY hr")
    fun countMessagesGroupedByHour(): List<Array<Any>>

    @Query("SELECT COUNT(DISTINCT m.senderId) FROM ChatMessage m WHERE m.datetimeSendAt BETWEEN :start AND :end")
    fun countActiveUsersBetween(start: LocalDateTime, end: LocalDateTime): Long

    fun countByDatetimeSendAtBetween(start: LocalDateTime, end: LocalDateTime): Long
    // 본 채팅 이후 50개 가져오기
    fun findByPotIdAndIdGreaterThanOrderByIdAsc(
        potId: Long,
        id: Long,
        pageable: Pageable
    ): Page<ChatMessage>
}