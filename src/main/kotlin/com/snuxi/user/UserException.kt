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
    errorCode = 1000000400,
    httpStatusCode = HttpStatus.BAD_REQUEST,
    msg = "파일이 비어있습니다."
)

class InvalidImageFormatException : UserException(
    errorCode = 1000001400,
    httpStatusCode = HttpStatus.BAD_REQUEST,
    msg = "지원하지 않는 이미지 형식이거나 파일명이 잘못되었습니다."
)

class UserNotFoundException : UserException(
    errorCode = 1000010404,
    httpStatusCode = HttpStatus.NOT_FOUND,
    msg = "해당 유저를 찾을 수 없습니다."
)
class ReportNotFoundException : UserException(
    errorCode = 1000011404,
    httpStatusCode = HttpStatus.NOT_FOUND,
    msg = "해당 신고 내역을 찾을 수 없습니다."
)
class CannotFindChatException : UserException(
    errorCode = 1000100404,
    httpStatusCode = HttpStatus.NOT_FOUND,
    msg = "해당 채팅 내역을 찾을 수 없습니다."
)

class SuspendedUserException(
    msg: String
) : OAuth2AuthenticationException(
    OAuth2Error("suspended_user"),
    msg
)

class NotSnuMailException(
    msg: String = "서울대학교(@snu.ac.kr) 계정만 로그인 가능합니다."
) : OAuth2AuthenticationException(
    OAuth2Error("not_snu_mail"),
    msg
)

