package com.snuxi.notification.service




@Service
class DeviceService(private val userDeviceRepository: UserDeviceRepository) {
    @Transactional
    fun registerDevice(userId: Long, token: String, deviceId: String, browserType: String) {
        val existingDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)

        if (existingDevice != null) {
            // SNUTT의 updateIfChanged와 유사하게 정보 갱신
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