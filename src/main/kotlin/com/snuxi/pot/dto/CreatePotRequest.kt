package com.snuxi.pot.dto

import java.time.LocalDateTime

data class CreatePotRequest (
    val departureId: Long,
    val destinationId: Long,
    val departureTime: LocalDateTime,
    val minCapacity: Int,
    val maxCapacity: Int,
    val estimatedFee: Int
)
