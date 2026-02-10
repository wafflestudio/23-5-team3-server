package com.snuxi.pot

import com.snuxi.exception.DomainException
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
            errorCode = 1010000400,
            httpStatusCode = HttpStatus.BAD_REQUEST,
            msg = "최소 인원수는 최대 인원수보다 반드시 작거나 같아야 합니다."
        )

class InvalidCountException :
        PotException(
            errorCode = 1010001400,
            httpStatusCode = HttpStatus.BAD_REQUEST,
            msg = "최소 인원수는 2명 이상, 최대 인원수는 4명 이하로 설정해주세요."
        )

class DuplicateParticipationException :
        PotException(
            errorCode = 1010010400,
            httpStatusCode = HttpStatus.BAD_REQUEST,
            msg = "한 명이 2개 이상의 택시팟 참여에 관여할 수 없습니다."
        )

class PotNotFoundException :
    PotException(
        errorCode = 1010011404,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "해당 택시팟을 찾을 수 없습니다."
    )

class PotFullException :
    PotException(
        errorCode = 1010100400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "해당 팟은 이미 정원이 초과되었습니다."
    )

class NotParticipatingException :
    PotException(
        errorCode = 1010101400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "참여 중인 팟이 아닙니다."
    )

class NotPotOwnerException :
    PotException(
        errorCode = 1010110400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "해당 팟의 방장이 아닙니다."
    )

class CannotKickSelfException :
    PotException(
        errorCode = 1010111400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "방장은 스스로를 강퇴할 수 없습니다."
    )

class TemporarilyNotLeavePotException :
    PotException(
        errorCode = 1011000400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "현재 해당 팟에서 나갈 수 없습니다. 잠시 후 다시 시도해주세요"
    )

class TemporarilyNotJoinPotException :
    PotException(
        errorCode = 1011001400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "현재 해당 팟에 참여할 수 없습니다. 잠시 후 다시 시도해주세요"
    )

class KakaoDeepLinkNotOwnerException :
    PotException(
        errorCode = 1011010400,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "카카오 택시 딥링크는 방장만 생성할 수 있습니다."
    )

class RegionNotFoundException :
    PotException(
        errorCode = 1011011404,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "해당하는 출발지 또는 목적지를 찾을 수 없어 딥링크를 만들 수 없습니다."
    )

class SuspendedUserException(message: String) :
    PotException(
        errorCode = 400,
        httpStatusCode = HttpStatus.FORBIDDEN,
        msg = "정지된 유저는 팟을 생성/참여할 수 없습니다."
    )

class AlreadyJoinedThisPotException :
    PotException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "이미 해당 택시팟에 참여하고 있습니다."
    )