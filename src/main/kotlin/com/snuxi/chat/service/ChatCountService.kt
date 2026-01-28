package com.snuxi.chat.service

import com.snuxi.chat.dto.ReadStatusUpdateDto
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.participant.repository.ParticipantRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class ChatCountService(
    private val participantRepository: ParticipantRepository,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    // 읽음 처리
    @Transactional
    fun markAsRead(userId: Long, roomId: Long, messageId: Long){
        participantRepository.updateLastReadMessageId(userId, roomId, messageId)

        val readEvent = ReadStatusUpdateDto(
            userId = userId,
            lastReadMessageId = messageId
        )

        simpMessagingTemplate.convertAndSend("/sub/rooms/$roomId/read", readEvent)
    }

}