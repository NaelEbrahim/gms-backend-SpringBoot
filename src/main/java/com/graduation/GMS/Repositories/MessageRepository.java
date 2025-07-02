package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Message;
import com.graduation.GMS.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    // Get distinct partner IDs (both senders and receivers you've interacted with)
    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END " +
            "FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Integer> findDistinctConversationPartnerIds(@Param("userId") Integer userId);

    // Get full conversation between two users (both directions)
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.date ASC")
    List<Message> findConversationBetweenUsers(@Param("user1") User user1,
                                               @Param("user2") User user2);

    // Alternative method using Spring Data JPA naming convention
    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByDate(
            User sender, User receiver,
            User receiverAgain, User senderAgain);


}
