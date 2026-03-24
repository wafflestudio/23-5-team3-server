package com.snuxi.pot.dto

import com.snuxi.pot.entity.Pots
import com.snuxi.pot.PotStatus
import java.time.LocalDateTime

data class PotDto(
    val id: Long,
    val ownerId: Long,
    val ownerName: String,
    val departureId: Long,
    val departureName: String,
    val destinationId: Long,
    val destinationName: String,
    val departureTime: LocalDateTime,
    val minCapacity: Int,
    val maxCapacity: Int,
    val currentCount: Int,
    val estimatedFee: Int,
    val status: PotStatus,
    val unreadCount: Long = 0,
    val totalUnreadCount: Long = 0,
    val isLocked: Boolean = false
) {
    companion object {
        fun from(
            entity: Pots,
            ownerName: String,
            departureName: String = "",
            destinationName: String = "",
            unreadCount: Long = 0,
            totalUnreadCount: Long = 0
        ) = PotDto(
            id = entity.id!!,
            ownerId = entity.ownerId,
            ownerName = ownerName,
            departureId = entity.departureId,
            departureName = departureName,
            destinationId = entity.destinationId,
            destinationName = destinationName,
            departureTime = entity.departureTime,
            minCapacity = entity.minCapacity,
            maxCapacity = entity.maxCapacity,
            currentCount = entity.currentCount,
            estimatedFee = entity.estimatedFee,
            status = entity.status,
            unreadCount = unreadCount,
            totalUnreadCount = totalUnreadCount,
            isLocked = entity.isLocked
        )
    }
}