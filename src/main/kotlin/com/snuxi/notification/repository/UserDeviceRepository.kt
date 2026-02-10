package com.snuxi.notification.repository

import com.snuxi.notification.model.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, Long> {
    // 특정 유저의 특정 브라우저(기기)가 이미 등록되어 있는지 확인용
    fun findByUserIdAndDeviceId(userId: Long, deviceId: String): UserDevice?

    // 알림을 보낼 유저의 모든 기기 토큰을 가져오기
    fun findAllByUserId(userId: Long): List<UserDevice>

    // 방 참여자 여러 명에게 한꺼번에 보낼 때 사용
    fun findAllByUserIdIn(userIds: List<Long>): List<UserDevice>
    fun deleteAllByUserId(userId: Long)
}