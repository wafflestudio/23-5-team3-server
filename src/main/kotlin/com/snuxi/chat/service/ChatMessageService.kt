package com.snuxi.chat.service

import com.snuxi.chat.NonParticipatingThisPotException
import com.snuxi.chat.dto.ChatMessageItemDto
import com.snuxi.chat.dto.ChatMessagePageDto
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService (
    private val chatMessageRepository: ChatMessageRepository,
    private val participantRepository: ParticipantRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getMessages(
        potId: Long,
        userId: Long,
        cursor: Long?,
        size: Int
    ): ChatMessagePageDto {
        // 해당 사용자가 현재 팟에 참여해있지 않을 경우, 채팅 내역을 볼 수 없음
        val isParticipating = participantRepository.existsByUserIdAndPotId(userId, potId)
        if(!isParticipating) throw NonParticipatingThisPotException()

        // 사용자 인증 완료, 채팅 내역을 가져온다
        val pageable = PageRequest.of(0, size.coerceIn(1, 100))

        // 커서 기반 조회
        val page = if(cursor == null) {
            chatMessageRepository.findByPotIdOrderByIdDesc(potId, pageable)
        } else {
            chatMessageRepository.findByPotIdAndIdLessThanOrderByIdDesc(potId, cursor, pageable)
        }

        val senderIds = page.content.map { it.senderId }.distinct()
        val users = userRepository.findAllById(senderIds)
        val userMap = users.associateBy { it.id!! }

        // Page<ChatMessage> -> List<ChatMessageItemDto> 변환
        val items = page.content.map { msg ->
            val u = userMap[msg.senderId]

            ChatMessageItemDto(
                id = msg.id!!,
                potId = msg.potId,
                senderId = msg.senderId,
                text = msg.text,
                datetimeSendAt = msg.datetimeSendAt,
                senderUsername = u?.username ?: "unknown",
                senderProfileImageUrl = u?.profileImageUrl
            )
        }

        // 방 멤버들의 읽은 상태 조회
        val participants = participantRepository.findAllByPotId(potId)
        val readStatuses = participants.associate {
            it.userId to it.lastReadMessageId
        }

        // cursor 갱신
        val nextCursor = items.lastOrNull()?.id
        return ChatMessagePageDto(
            items = items,
            nextCursor = if(page.hasNext()) nextCursor else null,
            hasNext = page.hasNext(),
            readStatuses = readStatuses
        )
    }


    // for debug
    @Transactional(readOnly = true)
    fun countMessages(potId: Long): Long {
        return chatMessageRepository.countByPotId(potId)
    }

}