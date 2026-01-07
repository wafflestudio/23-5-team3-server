package com.snuxi.user

import com.snuxi.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

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

