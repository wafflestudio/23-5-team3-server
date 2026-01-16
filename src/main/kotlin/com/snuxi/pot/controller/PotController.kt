package com.snuxi.pot.controller

import com.snuxi.pot.dto.CreatePotRequest
import com.snuxi.pot.dto.CreatePotResponse
import com.snuxi.pot.service.PotService
import com.snuxi.pot.entity.Pots
import com.snuxi.pot.dto.PotDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
class PotController (
    private val potService: PotService
) {
    @PostMapping("/room")
    fun create(
        @RequestHeader("CERTIFIED_USER_ID") userId: Long,
        @RequestBody createPotRequest: CreatePotRequest
    ): ResponseEntity<CreatePotResponse> {
        val ownerId = createPotRequest.ownerId
        val departureId = createPotRequest.departureId
        val destinationId = createPotRequest.destinationId
        val departureTime = createPotRequest.departureTime
        val minCapacity = createPotRequest.minCapacity
        val maxCapacity = createPotRequest.maxCapacity

        val response = potService.createPot(userId, ownerId, departureId, destinationId, departureTime, minCapacity, maxCapacity)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/rooms/{roomId}")
    fun delete(
        @RequestHeader("CERTIFIED_USER_ID") userId: Long,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        potService.deletePot(userId, roomId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/rooms/{roomId}/join")
    fun join(
        @RequestHeader("CERTIFIED_USER_ID") userId: Long,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        potService.joinPot(userId, roomId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/rooms/{roomId}/leave")
    fun leave(
        @RequestHeader("CERTIFIED_USER_ID") userId: Long,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        potService.leavePot(userId, roomId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/rooms/search")
    fun search(
        @RequestParam departureId: Long,
        @RequestParam destinationId: Long
    ): List<PotDto> {
        return potService.searchPots(departureId, destinationId)
    }

    @GetMapping("/users/me/pot")
    fun getMyPot(
        @RequestHeader("CERTIFIED_USER_ID") userId: Long
    ): PotDto? {
        return potService.getMyPot(userId)
    }




}