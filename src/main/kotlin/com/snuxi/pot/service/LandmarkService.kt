package com.snuxi.pot.service

import com.snuxi.pot.dto.core.LandmarkDto
import com.snuxi.pot.repository.LandmarkRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LandmarkService(
    private val landmarkRepository: LandmarkRepository
) {
    @Transactional(readOnly = true)
    fun getAllLandmarks(): List<LandmarkDto> {
        return landmarkRepository.findAll().map { LandmarkDto.from(it) }
    }
}