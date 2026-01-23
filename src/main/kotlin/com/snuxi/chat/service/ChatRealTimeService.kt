package com.snuxi.chat.service

import com.snuxi.chat.AuthenticationErrorException
import com.snuxi.chat.EmptyMessageException
import com.snuxi.chat.TooLongMessageException
import com.snuxi.chat.dto.ChatMessageItemDto
import com.snuxi.chat.entity.ChatMessage
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.security.CustomOAuth2User
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.Principal
import java.time.LocalDateTime

@Service
class ChatRealTimeService (
    private val chatMessageRepository: ChatMessageRepository,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    @Transactional
    fun send(
        roomId: Long,
        text: String,
        principal: Principal
    ): ChatMessageItemDto {
        val trimText = text.trim()
        if(trimText.isBlank()) throw EmptyMessageException()
        if(trimText.length > 200) throw TooLongMessageException()

        val userId = resolveUserIdFromPrincipal(principal)

        // 이미 인증된 유저이기 때문에, principal 로 인증 실패하면 그건 진짜 실패한 것
        val saved = chatMessageRepository.save(
            ChatMessage(
                potId = roomId,
                senderId = userId,
                text = trimText,
                datetimeSendAt = LocalDateTime.now()
            )
        )

        val itemDto = ChatMessageItemDto(
            id = saved.id!!,
            potId = saved.potId,
            senderId = saved.senderId,
            text = saved.text,
            datetimeSendAt = saved.datetimeSendAt
        )

        // broadcast
        simpMessagingTemplate.convertAndSend("/sub/rooms/$roomId", itemDto)

        return itemDto
    }

    private fun resolveUserIdFromPrincipal(
        principal: Principal
    ): Long {
        val authentication = principal as? Authentication ?: throw AuthenticationErrorException()

        val pr = authentication.principal
        return when(pr) {
            is CustomOAuth2User -> pr.userId
            else -> throw AuthenticationErrorException()
        }
    }
}