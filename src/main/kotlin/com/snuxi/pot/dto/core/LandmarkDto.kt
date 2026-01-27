package com.snuxi.pot.dto.core

import com.snuxi.pot.model.Landmark
import java.math.BigDecimal

data class LandmarkDto(
    val id: Long,
    val name: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal
) {
    companion object {
        fun from(landmark: Landmark) = LandmarkDto(
            id = landmark.id!!,
            name = landmark.landmarkName,
            latitude = landmark.latitude,
            longitude = landmark.longitude
        )

        fun generateKakaoLink(origin: LandmarkDto, dest: LandmarkDto): String {
            return "https://t.kakao.com/launch?type=taxi" +
                    "&origin_lat=${origin.latitude}" +
                    "&origin_lng=${origin.longitude}" +
                    "&dest_lat=${dest.latitude}" +
                    "&dest_lng=${dest.longitude}"
        }
    }
}