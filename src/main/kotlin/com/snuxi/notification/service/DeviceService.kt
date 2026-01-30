package com.snuxi.notification.service

import com.snuxi.notification.model.UserDevice
import com.snuxi.notification.repository.UserDeviceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
class DeviceService(private val userDeviceRepository: UserDeviceRepository) {
    @Transactional
    fun registerDevice(userId: Long, token: String, deviceId: String, browserType: String) {
        val existingDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)

        if (existingDevice != null) {
            // SNUTT의 updateIfChanged 참고
            existingDevice.fcmToken = token
            existingDevice.browserType = browserType
            existingDevice.updatedAt = LocalDateTime.now()
        } else {
            userDeviceRepository.save(
                UserDevice(userId = userId, fcmToken = token, deviceId = deviceId, browserType = browserType)
            )
        }
    }
}