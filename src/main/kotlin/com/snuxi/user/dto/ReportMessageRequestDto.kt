package com.snuxi.user.dto

import com.snuxi.user.model.ReportReason

data class ReportMessageRequestDto (
    val reason: ReportReason,
    val targetMessageId: Long,
    val reportedUserId: Long
)