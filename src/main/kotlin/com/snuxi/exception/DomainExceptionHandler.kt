package com.snuxi.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DomainExceptionHandler {
    // 로깅 기능 추가. - 초기화
    private val log = LoggerFactory.getLogger(DomainExceptionHandler::class.java)

    @ExceptionHandler(DomainException::class)
    fun handle(
        e: DomainException
    ): ResponseEntity<ErrorResponse> {
        log.error("Domain Exception occurred: [Code: ${e.errorCode}, Status: ${e.httpStatusCode}] - ${e.msg}", e)

        return ResponseEntity.status(e.httpStatusCode).body(
            ErrorResponse(
                errMsg = e.msg,
                errInternalCode = e.errorCode
            )
        )
    }
}