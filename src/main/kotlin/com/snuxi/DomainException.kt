package com.snuxi

import org.springframework.http.HttpStatusCode

open class DomainException(
    val errorCode: Int,
    val httpStatusCode: HttpStatusCode,
    val msg: String,
    cause: Throwable? = null
) : RuntimeException(msg, cause)