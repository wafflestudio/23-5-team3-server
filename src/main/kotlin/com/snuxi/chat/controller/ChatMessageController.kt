package com.snuxi.chat.controller

import com.snuxi.chat.dto.ChatMessagePageDto
import com.snuxi.chat.service.ChatMessageService
import com.snuxi.security.CurrentUserIdResolver
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rooms/{roomId}")
class ChatMessageController (
    private val currentUserIdResolver: CurrentUserIdResolver,
    private val chatMessageService: ChatMessageService
) {
    // 커서 기반 채팅 내역 조회(해당 room only)
    @GetMapping("/messages")
    fun getMessages(
        @PathVariable("roomId") potId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") size: Int
    ): ChatMessagePageDto {
        val userId = currentUserIdResolver.getCurrentUserId()
        return chatMessageService.getMessages(potId, userId, cursor, size)
    }
}