package com.snuxi.user.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AuthController {
    @GetMapping("/login")
    fun login(@RequestParam("redirect_uri") redirectUri: String?): String {
        return if (redirectUri != null) {
            "redirect:/oauth2/authorization/google?redirect_uri=$redirectUri"
        } else {
            "redirect:/oauth2/authorization/google"
        }
    }
}