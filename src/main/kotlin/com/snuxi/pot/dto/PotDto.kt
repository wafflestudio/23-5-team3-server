package com.snuxi.pot.dto

import com.snuxi.pot.entity.Pots
import com.snuxi.pot.PotStatus
import java.time.LocalDateTime

data class PotDto(
    val id: Long,
    val ownerId: Long,
    val departureId: Long,
    val destinationId: Long,
    val departureTime: LocalDateTime,
    val minCapacity: Int,
    val maxCapacity: Int,
    val currentCount: Int,
    val estimatedFee: Int,
    val status: PotStatus
) {
    companion object {
        fun from(entity: Pots) = PotDto(
            id = entity.id!!,
            ownerId = entity.ownerId,
            departureId = entity.departureId,
            destinationId = entity.destinationId,
            departureTime = entity.departureTime,
            minCapacity = entity.minCapacity,
            maxCapacity = entity.maxCapacity,
            currentCount = entity.currentCount,
            estimatedFee = entity.estimatedFee,
            status = entity.status
        )
    }
}