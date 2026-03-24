package com.snuxi.pot.service

import com.snuxi.chat.service.ChatBotService
import com.snuxi.infra.fcm.FcmPushClient
import com.snuxi.notification.service.PushService
import com.snuxi.pot.AlreadyJoinedThisPotException
import com.snuxi.pot.MaxPotLimitException
import com.snuxi.pot.model.Landmark
import com.snuxi.pot.repository.LandmarkRepository
import com.snuxi.user.model.User
import com.snuxi.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PotServiceParticipationTest {

    @Autowired
    private lateinit var potService: PotService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var landmarkRepository: LandmarkRepository

    @MockitoBean
    private lateinit var chatBotService: ChatBotService

    @MockitoBean
    private lateinit var pushService: PushService

    @MockitoBean
    private lateinit var fcmPushClient: FcmPushClient

    @MockitoBean
    private lateinit var simpMessagingTemplate: SimpMessagingTemplate

    private lateinit var testUser: User
    private lateinit var otherUser: User
    private lateinit var departure: Landmark
    private lateinit var destination: Landmark
    private val futureTime: LocalDateTime = LocalDateTime.now().plusHours(1)

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(
            User(
                email = "test@snu.ac.kr",
                username = "TestUser"
            )
        )
        otherUser = userRepository.save(
            User(
                email = "other@snu.ac.kr",
                username = "OtherUser"
            )
        )
        departure = landmarkRepository.save(
            Landmark(
                landmarkName = "서울대입구역",
                latitude = BigDecimal("37.48190"),
                longitude = BigDecimal("126.95270")
            )
        )
        destination = landmarkRepository.save(
            Landmark(
                landmarkName = "관악캠퍼스",
                latitude = BigDecimal("37.45960"),
                longitude = BigDecimal("126.95200")
            )
        )
    }

    private fun createPot(userId: Long): Long {
        val response = potService.createPot(
            userId = userId,
            departureId = departure.id!!,
            destinationId = destination.id!!,
            departureTime = futureTime,
            minCapacity = 2,
            maxCapacity = 4
        )
        return response.createdPotId
    }

    @Test
    fun `유저가 팟 1개 생성 - 성공`() {
        val potId = createPot(testUser.id!!)
        assertNotNull(potId)
    }

    @Test
    fun `유저가 팟 3개 생성 - 전부 성공`() {
        assertDoesNotThrow { createPot(testUser.id!!) }
        assertDoesNotThrow { createPot(testUser.id!!) }
        assertDoesNotThrow { createPot(testUser.id!!) }
    }

    @Test
    fun `유저가 팟 3개 생성 후 4번째 생성 시도 - MaxPotLimitException`() {
        createPot(testUser.id!!)
        createPot(testUser.id!!)
        createPot(testUser.id!!)

        assertThrows<MaxPotLimitException> {
            createPot(testUser.id!!)
        }
    }

    @Test
    fun `유저가 팟 2개 참여 중 + 다른 팟 join - 성공 (3개째)`() {
        createPot(testUser.id!!)
        createPot(testUser.id!!)

        val otherPot = createPot(otherUser.id!!)

        assertDoesNotThrow {
            potService.joinPot(testUser.id!!, otherPot)
        }
    }

    @Test
    fun `유저가 팟 3개 참여 중 + 다른 팟 join - MaxPotLimitException`() {
        createPot(testUser.id!!)
        createPot(testUser.id!!)
        createPot(testUser.id!!)

        val otherPot = createPot(otherUser.id!!)

        assertThrows<MaxPotLimitException> {
            potService.joinPot(testUser.id!!, otherPot)
        }
    }

    @Test
    fun `같은 팟에 중복 join - AlreadyJoinedThisPotException`() {
        val potId = createPot(testUser.id!!)

        assertThrows<AlreadyJoinedThisPotException> {
            potService.joinPot(testUser.id!!, potId)
        }
    }

    @Test
    fun `유저가 팟 3개 참여 후 1개 leave 후 새 팟 생성 - 성공`() {
        val pot1 = createPot(testUser.id!!)
        createPot(testUser.id!!)
        createPot(testUser.id!!)

        potService.leavePot(testUser.id!!, pot1)

        assertDoesNotThrow {
            createPot(testUser.id!!)
        }
    }
}
