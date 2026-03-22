package com.snuxi.user.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI

@Controller
class AuthController {

    private val allowedRedirectHosts = setOf(
        "snuxi.com",
        "d2c0wdnl0iqvgb.cloudfront.net",
        "d2j21bk78krg0p.cloudfront.net",
        "localhost"
    )

    @GetMapping("/login")
    fun login(@RequestParam("redirect_uri") redirectUri: String?): String {
        if (redirectUri != null) {
            val host = try { URI(redirectUri).host } catch (_: Exception) { null }
            if (host != null && allowedRedirectHosts.contains(host)) {
                return "redirect:/oauth2/authorization/google?redirect_uri=$redirectUri"
            }
        }
        return "redirect:/oauth2/authorization/google"
    }
}
