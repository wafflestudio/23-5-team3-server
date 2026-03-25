package com.snuxi.admin.dto

import java.time.Instant
import java.time.LocalDateTime

data class AdminPotListResponse(
    val content: List<AdminPotListItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

data class AdminPotListItem(
    val potId: Long,
    val departureName: String,
    val destinationName: String,
    val departureTime: LocalDateTime,
    val participantCount: Int,
    val kakaoCallStatus: String,
    val kakaoCallAt: LocalDateTime?,
    val kakaoCallError: String?,
    val createdAt: Instant?
)
