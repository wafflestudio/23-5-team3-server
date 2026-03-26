package com.snuxi.qr.controller

import com.snuxi.qr.entity.QrScan
import com.snuxi.qr.repository.QrScanRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/go")
class QrRedirectController(
    private val qrScanRepository: QrScanRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val redirectMap = mapOf(
        "a" to "https://snuxi.com",
        "b" to "https://snuxi.com",
        "c" to "https://snuxi.com",
    )

    private val fallbackUrl = "https://snuxi.com"

    @GetMapping("/{code}")
    fun redirect(
        @PathVariable code: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val target = redirectMap[code] ?: fallbackUrl

        qrScanRepository.save(
            QrScan(
                code = code,
                userAgent = request.getHeader("User-Agent")?.take(512),
                ip = request.getHeader("X-Real-IP") ?: request.remoteAddr,
                referer = request.getHeader("Referer")?.take(512),
            )
        )

        log.info("QR scan: code={}, ip={}, redirect={}", code, request.remoteAddr, target)
        response.sendRedirect(target)
    }
}
