package com.snuxi.user.controller

import com.snuxi.user.dto.UserResponse
import com.snuxi.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/profile")
    fun getMyProfile(
        @AuthenticationPrincipal
        oAuth2User: OAuth2User
    ): ResponseEntity<UserResponse> {
        val email = oAuth2User.attributes["email"] as String
        val profile = userService.getProfile(email)
        return ResponseEntity.ok(profile)
    }
}