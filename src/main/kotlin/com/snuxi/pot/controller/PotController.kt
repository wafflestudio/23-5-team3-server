package com.snuxi.pot.controller

import com.snuxi.pot.dto.CreatePotRequest
import com.snuxi.pot.dto.CreatePotResponse
import com.snuxi.pot.service.PotService
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
}