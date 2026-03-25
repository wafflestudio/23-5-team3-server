package com.snuxi.admin

import com.snuxi.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class AdminException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null
) : DomainException(errorCode, httpStatusCode, msg, cause)

class InvalidAdminFilterException(msg: String) :
    AdminException(
        errorCode = 1020000400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = msg
    )
