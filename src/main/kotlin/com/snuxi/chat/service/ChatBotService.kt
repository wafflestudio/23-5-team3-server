package com.snuxi.chat.service

import com.snuxi.chat.dto.ChatMessageItemDto
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.user.model.Role
import com.snuxi.user.repository.UserRepository
import com.snuxi.user.model.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.snuxi.chat.entity.ChatMessage
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime

@Service
class ChatBotService (
    private val userRepository: UserRepository,
    private val participantRepository: ParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    private val BOT_EMAIL_ADDR = "bot@snuxi.com"
    private val BOT_USERNAME = "Chatting Bot"

    // 챗봇 id 반환
    @Transactional
    fun getOrCreateChatBot(): Long {
        val existingChatBot = userRepository.findByEmail(BOT_EMAIL_ADDR)

        // 채팅봇 이미 존재하면 id 반환
        if(existingChatBot != null) return existingChatBot.id!!

        // 채팅봇 저장
        val saved = userRepository.save(
            User(
                email = BOT_EMAIL_ADDR,
                username = BOT_USERNAME,
                profileImageUrl = null,
                role = Role.CHATBOT,
                activePotId = null,
                notificationEnabled = false
            )
        )

        return saved.id!!
    }

    // 챗봇이 입장 메시지 전송
    @Transactional
    fun sendJoinMsg(
        roomId: Long,
        userId: Long,
        userName: String
    ): ChatMessageItemDto {
        val text = "${userName} 님이 입장했습니다."

        val participantsId = participantRepository.findAllByPotId(roomId).map {
                it -> it.userId
        }

        // 채팅방에 메시지 전송
        return sendMessage(roomId, text)
    }

    // 떠나는 메시지 전송(챗봇)
    @Transactional
    fun sendLeaveMsg(
        roomId: Long,
        userId: Long,
        userName: String
    ): ChatMessageItemDto {
        val text = "${userName} 님이 퇴장했습니다."

        val participantsId = participantRepository.findAllByPotId(roomId).map {
                it -> it.userId
        }

        // 채팅방에 메시지 전송
        return sendMessage(roomId, text)
    }

    @Transactional
    fun sendMessage(
        roomId: Long,
        text: String
    ): ChatMessageItemDto {
        val botId = getOrCreateChatBot()

        val saved = chatMessageRepository.save(
            ChatMessage(
                potId = roomId,
                senderId = botId,
                text = text,
                datetimeSendAt = LocalDateTime.now()
            )
        )

        val chatMessageItemDto = ChatMessageItemDto(
            id = saved.id!!,
            potId = saved.potId,
            senderId = saved.senderId,
            text = saved.text,
            datetimeSendAt = saved.datetimeSendAt,
            senderUsername = BOT_USERNAME,
            senderProfileImageUrl = null
        )

        // 메시지 브로드캐스트
        simpMessagingTemplate.convertAndSend("/sub/rooms/${roomId}", chatMessageItemDto)
        return chatMessageItemDto
    }

    @Transactional
    fun sendKickMsg(
        roomId: Long,
        userName: String
    ): ChatMessageItemDto {
        val text = "${userName} 님이 강퇴되었습니다." // 멘트 수정 가능
        return sendMessage(roomId, text)
    }

    @Transactional
    fun sendOwnerChangeMsg(potId: Long, nextOwnerName: String): ChatMessageItemDto {
        val text = "${nextOwnerName}님이 새로운 방장이 되었습니다."
        return sendMessage(potId, text)
    }
}