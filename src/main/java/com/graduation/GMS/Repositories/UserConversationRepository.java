package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.User_Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserConversationRepository extends JpaRepository<User_Conversation, Integer> {

    List<User_Conversation> findAllByConversationId(Integer conversationId);

    Optional<User_Conversation> findByUserIdAndConversationId(Integer userId, Integer conversationId);

    @Query("""
            SELECT uc FROM User_Conversation uc
            WHERE uc.user.id = :userId AND uc.isDeleted = false
            """)
    List<User_Conversation> findVisibleForUser(@Param("userId") Integer userId);


    @Modifying
    @Transactional
    @Query("""
            UPDATE User_Conversation uc
            SET uc.deletedAt = :deletedAt,
            uc.isDeleted = true
            WHERE uc.user.id = :userId AND uc.conversation.id = :conversationId
            """)
    void markDeletedForUser(
            @Param("userId") Integer userId,
            @Param("conversationId") Integer conversationId,
            @Param("deletedAt") LocalDateTime deletedAt
    );

    @Transactional
    @Modifying
    @Query("""
                UPDATE User_Conversation uc
                SET uc.lastSeenAt = :lastSeenAt
                WHERE uc.user.id = :userId AND uc.conversation.id = :conversationId
            """)
    void updateLastSeenAt(
            @Param("userId") Integer userId,
            @Param("conversationId") Integer conversationId,
            @Param("lastSeenAt") LocalDateTime lastSeenAt
    );

}
