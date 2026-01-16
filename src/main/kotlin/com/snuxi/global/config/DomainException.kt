package com.snuxi.global.config

import org.springframework.http.HttpStatusCode

abstract class DomainException(
    val errorCode: Int,
    val httpStatusCode: HttpStatusCode,
    val msg: String,
    override val cause: Throwable? = null
) : RuntimeException(msg, cause)