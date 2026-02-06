package com.snuxi.pot.controller

import com.snuxi.pot.dto.CreatePotRequest
import com.snuxi.pot.dto.CreatePotResponse
import com.snuxi.pot.service.PotService
import com.snuxi.pot.dto.PotDto
import com.snuxi.security.CustomOAuth2User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.data.web.PageableDefault
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.snuxi.pot.dto.core.LandmarkDto

@RestController
class PotController (
    private val potService: PotService
) {
    @PostMapping("/room")
    fun create(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @RequestBody createPotRequest: CreatePotRequest
    ): ResponseEntity<CreatePotResponse> {
        val departureId = createPotRequest.departureId
        val destinationId = createPotRequest.destinationId
        val departureTime = createPotRequest.departureTime
        val minCapacity = createPotRequest.minCapacity
        val maxCapacity = createPotRequest.maxCapacity

        val response = potService.createPot(principal.userId, departureId, destinationId, departureTime, minCapacity, maxCapacity)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/rooms/{roomId}")
    fun delete(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        potService.deletePot(principal.userId, roomId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/rooms/{roomId}/join")
    fun join(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        // @RequestHeader("CERTIFIED_USER_ID") userId: Long,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        potService.joinPot(principal.userId, roomId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/rooms/{roomId}/leave")
    fun leave(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        potService.leavePot(principal.userId, roomId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/rooms/search")
    fun search(
        @RequestParam departureId: Long?,
        @RequestParam destinationId: Long?,
        @PageableDefault(size = 10) pageable: Pageable
    ): Page<PotDto> {
        return potService.searchPots(departureId, destinationId, pageable)
    }

    @DeleteMapping("/rooms/{roomId}/members/{targetUserId}")
    fun kickMember(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @PathVariable roomId: Long,
        @PathVariable targetUserId: Long
    ): ResponseEntity<Void> {
        potService.kickParticipant(
            requestUserId = principal.userId,
            potId = roomId,
            targetUserId = targetUserId
        )
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/users/me/pot")
    fun getMyPot(
        @AuthenticationPrincipal principal: CustomOAuth2User,
    ): PotDto? {
        return potService.getMyPot(principal.userId)
    }

    @GetMapping("/rooms/{roomId}/kakao-deep-link")
    fun getKakaoDeepLink(
        @PathVariable("roomId") roomId: Long,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): String = potService.generateKakaoDeepLink(roomId, principal.userId)

    @PatchMapping("/rooms/{roomId}/status")
    fun togglePotStatus(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @PathVariable roomId: Long
    ): ResponseEntity<PotDto> {
        val updatedPot = potService.togglePotStatus(principal.userId, roomId)
        return ResponseEntity.ok(updatedPot)
    }
}