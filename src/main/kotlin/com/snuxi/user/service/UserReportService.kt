package com.snuxi.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.snuxi.chat.repository.ChatMessageRepository
import com.snuxi.participant.repository.ParticipantRepository
import com.snuxi.user.CannotFindChatException
import com.snuxi.user.UserNotFoundException
import com.snuxi.user.dto.ChatLogDto
import com.snuxi.user.model.ReportReason
import com.snuxi.user.model.Reported
import com.snuxi.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@Service
class UserReportService (
    private val participantRepository: ParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository
) {
    @Transactional
    fun reportMessage(
        roomId: Long,
        reportUserId: Long,
        reason: ReportReason,
        targetMessageId: Long,
        reportedUserId: Long
    ): Long {
        // 신고자와 피신고자 모두 같은 방 안에 있어야 하며,
        val reporterIsInPot = participantRepository.existsByUserIdAndPotId(reportUserId, roomId)
        if(!reporterIsInPot) throw UserNotFoundException()

        val reportedIsInPot = participantRepository.existsByUserIdAndPotId(reportedUserId, roomId)
        if(!reportedIsInPot) throw UserNotFoundException()

        // 신고된 메시지가 해당 팟에 존재해야 한다.
        val targetMessage = chatMessageRepository.findById(targetMessageId).orElseThrow {
            CannotFindChatException()
        }

        if(targetMessage.potId != roomId) throw CannotFindChatException()

        // 조회: 앞 50개, 뒤 50개
        val beforeTarget = chatMessageRepository.findByPotIdAndIdLessThanEqualOrderByIdDesc(roomId, targetMessage.id!!, PageRequest.of(0, 51)).content
        val afterTarget = chatMessageRepository.findByPotIdAndIdGreaterThanOrderByIdAsc(roomId, targetMessage.id!!, PageRequest.of(0, 50)).content
        val totalMessages = (beforeTarget + afterTarget).sortedBy{ it.id }

        // ChatMessage -> ChatLogDto 변환
        // username 불러오기
        val allUsersId = totalMessages.map {
            it.senderId
        }.distinct()
        val allUsers = userRepository.findAllById(allUsersId)
        val userIdToName = allUsers.associateBy(
            { it.id!! }, {it.username}
        )

        val chatLogDto = totalMessages.map {
            ChatLogDto(
                id = it.id!!,
                senderId = it.senderId,
                username = userIdToName[it.senderId] ?: "unknown user",
                text = it.text,
                datetimeSendAt = it.datetimeSendAt
            )
        }

        // reported email / messages 만들기
        val messagesJson = objectMapper.writeValueAsString(chatLogDto)

        // reportedRepository save
        val reporterEmail = userRepository.findById(reportUserId).orElseThrow {
            UserNotFoundException()
        }.email
        val reportedEmail = userRepository.findById(reportedUserId).orElseThrow {
            UserNotFoundException()
        }.email

        val saved = Reported(
            isProcessed = false,
            reportedAt = LocalDateTime.now(),
            reporterUserId = reportUserId,
            reporterEmail = reporterEmail,
            reportedUserId = reportedUserId,
            reportedEmail = reportedEmail,
            reason = reason,
            messages = messagesJson
        )

        return saved.id!!
    }
}