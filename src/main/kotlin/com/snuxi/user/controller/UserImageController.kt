package com.snuxi.user.controller

import com.snuxi.security.CustomOAuth2User
import com.snuxi.user.service.UserImageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/user")
class UserImageController(
    private val userImageService: UserImageService
) {
    @PostMapping("/profile/picture", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadProfileImage(
        @AuthenticationPrincipal customOAuth2User: CustomOAuth2User,
        @RequestParam("image") imageFile: MultipartFile
    ): ResponseEntity<String> {
        val email = customOAuth2User.attributes["email"] as String
        val imageUrl = userImageService.uploadProfileImage(email, imageFile)

        return ResponseEntity.ok(imageUrl)
    }


}