package com.snuxi.chat.controller

import com.snuxi.chat.dto.ChatMessageItemDto
import com.snuxi.chat.dto.ChatSendRequestDto
import com.snuxi.chat.service.ChatRealTimeService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatRealTimeController (
    private val chatRealTimeService: ChatRealTimeService
) {
    @MessageMapping("/rooms/{roomId}/messages")
    fun send(
        @DestinationVariable("roomId") roomId: Long,
        chatSendRequestDto: ChatSendRequestDto,
        principal: Principal
    ): ChatMessageItemDto = chatRealTimeService.send(roomId, chatSendRequestDto.text, principal)
}