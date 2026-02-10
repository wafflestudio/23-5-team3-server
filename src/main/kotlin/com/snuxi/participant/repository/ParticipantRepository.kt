package com.snuxi.participant.repository

import com.snuxi.participant.entity.Participants
import com.snuxi.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ParticipantRepository : JpaRepository<Participants, Long>{
    fun existsByUserIdAndPotId(
        userId: Long,
        potId: Long
    ): Boolean
    fun existsByUserId(
        userId: Long
    ): Boolean
    fun findByUserId(userId: Long): Participants?
    fun findAllByPotId(potId: Long): List<Participants>
    fun findByUserIdAndPotId(userId: Long, potId: Long): Participants?
    fun deleteByUserIdAndPotId(userId: Long, potId: Long)
    fun deleteAllByPotId(potId: Long)

    @Query(
        value = "SELECT user_id FROM participants WHERE pot_id = :potId",
        nativeQuery = true
    )
    fun findUserIdsByPotId(
        potId: Long
    ): List<Long>

    fun findFirstByPotIdOrderByJoinedAtAsc(potId: Long): Participants?

    // 실제로 삭제된 row 수 반환
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
            DELETE from Participants p
            WHERE p.userId = :userId
            AND p.potId = :potId
        """
    )
    fun deleteByUserIdAndPotIdReturnCount(
        @Param("userId") userId: Long,
        @Param("potId") potId: Long
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Participants p SET p.lastReadMessageId = :messageId WHERE p.userId = :userId AND p.potId = :potId")
    fun updateLastReadMessageId(
        @Param("userId") userId: Long,
        @Param("potId") potId: Long,
        @Param("messageId") messageId: Long
    )

    @Query("SELECT p.userId FROM Participants p WHERE p.potId IN :potIds")
    fun findUserIdsByPotIds(@Param("potIds") potIds: List<Long>): List<Long>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Participants p WHERE p.potId IN :potIds")
    fun deleteAllByPotIdIn(@Param("potIds") potIds: List<Long>)
    fun deleteAllByUserId(userId: Long)

    @Query("SELECT u FROM User u, Participants p WHERE u.id = p.userId AND p.potId = :potId")
    fun findUsersByPotId(@Param("potId") potId: Long): List<User>

    fun findByPotIdOrderByJoinedAtAsc(potId: Long): List<Participants>
}
