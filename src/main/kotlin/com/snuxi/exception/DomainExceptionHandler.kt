package com.snuxi.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DomainExceptionHandler {
    @ExceptionHandler(DomainException::class)
    fun handle(
        e: DomainException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.httpStatusCode).body(
            ErrorResponse(
                errMsg = e.msg,
                errInternalCode = e.errorCode
            )
        )
}