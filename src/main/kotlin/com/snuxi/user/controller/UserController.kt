package com.snuxi.user.controller

import com.snuxi.notification.service.DeviceService
import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.dto.UserResponse
import com.snuxi.user.dto.UserUpdateRequest
import com.snuxi.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val deviceService: DeviceService
) {
    @GetMapping("/profile")
    fun getMyProfile(
        @AuthenticationPrincipal
        customOAuth2User: CustomOAuth2User
    ): ResponseEntity<UserResponse> {
        val email = customOAuth2User.attributes["email"] as String
        val profile = userService.getProfile(email)
        return ResponseEntity.ok(profile)
    }
    data class DeviceRegisterRequest(
        val token: String,
        val deviceId: String,
        val browserType: String
    )

    @PostMapping("/device")
    fun registerDevice(
        @AuthenticationPrincipal customOAuth2User: CustomOAuth2User,
        @RequestBody request: DeviceRegisterRequest
    ): ResponseEntity<Void> {
     deviceService.registerDevice(customOAuth2User.userId, request.token, request.deviceId, request.browserType
     )
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/notification")
    fun toggleNotification(
        @AuthenticationPrincipal customOAuth2User: CustomOAuth2User,
        @RequestParam enabled: Boolean
    ): ResponseEntity<Void> {
        userService.updateNotificationEnabled(customOAuth2User.userId, enabled)
        return ResponseEntity.ok().build()
    }
    @PatchMapping("/profile/name")
    fun updateUsername(
        @AuthenticationPrincipal customOAuth2User: CustomOAuth2User,
        @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val email = customOAuth2User.attributes["email"] as String
        val updatedProfile = userService.updateUsername(email, request.username)
        return ResponseEntity.ok(updatedProfile)
    }
}