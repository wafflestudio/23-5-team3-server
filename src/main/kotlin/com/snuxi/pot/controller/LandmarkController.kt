package com.snuxi.pot.controller

import com.snuxi.pot.dto.response.LandmarkListResponse
import com.snuxi.pot.dto.core.LandmarkDto
import com.snuxi.pot.repository.LandmarkRepository
import com.snuxi.pot.service.LandmarkService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/maps")
class LandmarkController(
    private val landmarkService: LandmarkService // 주입 변경
) {
    @GetMapping("/landmarks")
    fun getLandmarks(): LandmarkListResponse {
        return LandmarkListResponse(landmarkService.getAllLandmarks())
    }
}