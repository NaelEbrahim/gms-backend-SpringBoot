package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("""
            SELECT m FROM Message m
            WHERE m.conversation.id = :conversationId
              AND (:deletedAt IS NULL OR m.date > :deletedAt)
            ORDER BY m.date DESC
            """)
    List<Message> findMessagesAfterDeletedAt(
            @Param("conversationId") Integer conversationId,
            @Param("deletedAt") LocalDateTime deletedAt,
            Pageable pageable
    );

    @Query("""
                SELECT COUNT(m)
                FROM Message m
                WHERE m.conversation.id = :conversationId
                  AND m.sender.id <> :userId
                  AND m.date > :afterDate
            """)
    Integer countUnreadForConversation(
            @Param("conversationId") Integer conversationId,
            @Param("userId") Integer userId,
            @Param("afterDate") LocalDateTime afterDate
    );


    @Query("""
            SELECT m FROM Message m
            WHERE m.conversation.id = :conversationId
            ORDER BY m.date DESC LIMIT 1
            """)
    Message findLastMessage(Integer conversationId);

}