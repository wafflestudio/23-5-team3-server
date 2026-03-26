package com.snuxi.pot.controller

import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/go")
class QrRedirectController {

    private val log = LoggerFactory.getLogger(javaClass)

    private val redirectMap = mapOf(
        "a" to "https://snuxi.com"
    )

    private val fallbackUrl = "https://snuxi.com"

    @GetMapping("/{code}")
    fun redirect(
        @PathVariable code: String,
        response: HttpServletResponse
    ) {
        val target = redirectMap[code] ?: fallbackUrl
        log.info("QR scan: code={}, redirect={}", code, target)
        response.sendRedirect(target)
    }
}
