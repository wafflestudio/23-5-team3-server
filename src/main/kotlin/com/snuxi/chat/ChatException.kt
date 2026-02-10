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
        errorCode = 1100000400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "현재 이 팟에 참여하고 있지 않습니다."
    )

class EmptyMessageException :
    ChatException(
        errorCode = 1100001400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "메시지는 최소 1글자 이상이어야 합니다."
    )

class TooLongMessageException :
    ChatException(
        errorCode = 1100010400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "메시지는 한번에 최대 200자만 전송할 수 있습니다."
    )

class AuthenticationErrorException :
    ChatException(
        errorCode = 1100011500,
        httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        msg = "일시적 인증 실패 현상이 발생하였습니다. 로그아웃 후 재시도 해주세요."
    )