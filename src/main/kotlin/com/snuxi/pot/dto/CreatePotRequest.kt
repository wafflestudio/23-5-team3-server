package com.snuxi.pot.dto

import java.time.LocalDateTime

data class CreatePotRequest (
    val ownerId: Long,
    val departureId: Long,
    val destinationId: Long,
    val departureTime: LocalDateTime,
    val minCapacity: Int,
    val maxCapacity: Int
)
