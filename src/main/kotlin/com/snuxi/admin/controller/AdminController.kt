package com.snuxi.admin.controller

import com.snuxi.admin.dto.AdminStatsResponse
import com.snuxi.admin.dto.SuspendRequest
import com.snuxi.admin.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService
) {
    // POST /admin/users/{userId}/suspend
    @PostMapping("/users/{userId}/suspend")
    fun suspendUser(
        @PathVariable userId: Long,
        @RequestBody request: SuspendRequest
    ): ResponseEntity<String> {
        adminService.suspendUser(userId, request.days)

        return ResponseEntity.ok("유저($userId)를 ${request.days}일간 정지시켰습니다.")
    }

    @GetMapping("/stats")
    fun getAdminStats(): ResponseEntity<AdminStatsResponse> {
        return ResponseEntity.ok(adminService.getFullStatistics())
    }
}