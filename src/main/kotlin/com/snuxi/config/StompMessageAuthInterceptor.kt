package com.snuxi.config

import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.security.CurrentUserIdResolver
import com.snuxi.security.CustomOAuth2User
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import java.security.Principal
import org.springframework.security.core.Authentication

@Component
class StompMessageAuthInterceptor (
    private val participantRepository: ParticipantRepository,
    private val currentUserIdResolver: CurrentUserIdResolver
): ChannelInterceptor {
    // 메시지가 전송되기 전 이 함수가 가로채서 보안 검사를 한다.
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel
    ): Message<*>? {
        // message 객체를 command 등 구성요소로 wrap
        val accessor = StompHeaderAccessor.wrap(message)
        val command = accessor.command ?: return message

        // SUBSCRIBE(구독) & SEND(전송) 만 보안 검사 실행
        if(command == StompCommand.SUBSCRIBE || command == StompCommand.SEND){
            val dest = accessor.destination ?: return message // ex: /sub/rooms/10

            val roomId = roomIdFromDest(dest) ?: return message
            val userId = resolveUserId(accessor.user)

            val isExistingInPot = participantRepository.existsByUserIdAndPotId(userId, roomId)
            if(!isExistingInPot) return null
        }

        return message
    }

    private fun roomIdFromDest(
        dest: String
    ): Long? {
        val splitDest = dest.split("/")
        val roomsIdx = splitDest.indexOf("rooms")

        if(roomsIdx < 0 || roomsIdx + 1 >= splitDest.size) return null
        return splitDest[roomsIdx + 1].toLongOrNull()
    }

    private fun resolveUserId(
        principal: Principal?
    ): Long {
        return runCatching {
            // 먼저 채팅보낸 사람의 정보를 확인
            resolveUserIdFromPrincipal(principal)
        }. getOrElse {
            // 쿠키 미전송 등의 오류로 없으면 서버에서 찾음
            currentUserIdResolver.getCurrentUserId()
        }
    }

    private fun resolveUserIdFromPrincipal(
        principal: Principal?
    ): Long {
        val authentication = principal as? Authentication ?: throw IllegalStateException("Auth not found in websocket")

        val pr = authentication.principal
        return when(pr) {
            is CustomOAuth2User -> pr.userId
            else -> throw IllegalStateException("not expected auth type")
        }
    }
}