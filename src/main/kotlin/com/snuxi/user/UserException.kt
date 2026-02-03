package com.snuxi.user

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import com.snuxi.exception.DomainException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error

sealed class UserException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class EmptyFileException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.BAD_REQUEST,
    msg = "파일이 비어있습니다."
)

class InvalidImageFormatException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.BAD_REQUEST,
    msg = "지원하지 않는 이미지 형식이거나 파일명이 잘못되었습니다."
)

class UserNotFoundException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.NOT_FOUND,
    msg = "해당 유저를 찾을 수 없습니다."
)

class SuspendedUserException(
    msg: String
) : OAuth2AuthenticationException(
    OAuth2Error("suspended_user"),
    msg
)

