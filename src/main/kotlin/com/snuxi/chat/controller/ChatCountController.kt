package com.snuxi.chat.controller

import com.snuxi.chat.service.ChatCountService
import com.snuxi.security.CustomOAuth2User
import com.snuxi.chat.dto.ReadMessageRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatCountController(
    private val chatCountService: ChatCountService
) {
    @PatchMapping("/rooms/{roomId}/read")
    fun markAsRead(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @PathVariable roomId: Long,
        @RequestBody request: ReadMessageRequest
    ) {
        chatCountService.markAsRead(principal.userId, roomId, request.messageId)
    }
}