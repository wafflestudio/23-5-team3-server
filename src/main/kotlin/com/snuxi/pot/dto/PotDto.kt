package com.snuxi.pot.dto

import com.snuxi.pot.entity.Pots
import com.snuxi.pot.PotStatus
import java.time.LocalDateTime

data class PotDto(
    val id: Long,
    val ownerId: Long,
    val ownerName: String,
    val departureId: Long,
    val destinationId: Long,
    val departureTime: LocalDateTime,
    val minCapacity: Int,
    val maxCapacity: Int,
    val currentCount: Int,
    val estimatedFee: Int,
    val status: PotStatus,
    val unreadCount: Long = 0,
    val isLocked: Boolean = false
) {
    companion object {
        fun from(entity: Pots, ownerName: String, unreadCount: Long = 0) = PotDto(
            id = entity.id!!,
            ownerId = entity.ownerId,
            ownerName = ownerName,
            departureId = entity.departureId,
            destinationId = entity.destinationId,
            departureTime = entity.departureTime,
            minCapacity = entity.minCapacity,
            maxCapacity = entity.maxCapacity,
            currentCount = entity.currentCount,
            estimatedFee = entity.estimatedFee,
            status = entity.status,
            unreadCount = unreadCount,
            isLocked = entity.isLocked
        )
    }
}