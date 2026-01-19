package com.snuxi.chat

import com.snuxi.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class ChatException (
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null
): DomainException(errorCode, httpStatusCode, msg, cause)

class NonParticipatingThisPotException :
    ChatException(
        errorCode = 400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "현재 이 팟에 참여하고 있지 않습니다."
    )