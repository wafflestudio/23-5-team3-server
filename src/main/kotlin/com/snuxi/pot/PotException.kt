package com.snuxi.pot

import com.snuxi.global.config.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class PotException (
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null
): DomainException(errorCode, httpStatusCode, msg, cause)

class MinMaxReversedException :
        PotException(
            errorCode = 0,
            httpStatusCode = HttpStatus.BAD_REQUEST,
            msg = "최소 인원수는 최대 인원수보다 반드시 작거나 같아야 합니다."
        )

class InvalidCountException :
        PotException(
            errorCode = 0,
            httpStatusCode = HttpStatus.BAD_REQUEST,
            msg = "최소 인원수는 2명 이상, 최대 인원수는 4명 이하로 설정해주세요."
        )

class DuplicateParticipationException :
        PotException(
            errorCode = 0,
            httpStatusCode = HttpStatus.BAD_REQUEST,
            msg = "한 명이 2개 이상의 택시팟 참여에 관여할 수 없습니다."
        )

class PotNotFoundException :
    PotException(
        errorCode = 404,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "해당 택시팟을 찾을 수 없습니다."
    )

class PotFullException :
    PotException(
        errorCode = 400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "해당 팟은 이미 정원이 초과되었습니다."
    )

class NotParticipatingException :
    PotException(
        errorCode = 400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "참여 중인 팟이 아닙니다."
    )

class NotPotOwnerException :
    PotException(
        errorCode = 400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "해당 팟의 방장이 아닙니다."
    )