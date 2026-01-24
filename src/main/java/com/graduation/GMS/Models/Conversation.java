package com.graduation.GMS.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@Table(
        name = "conversation",
        indexes = {
                @Index(name = "idx_conversation_key", columnList = "conversation_key")
        }
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 32, unique = true)
    private Long conversationKey;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Conversation> conversationUsers;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> conversationMessages;

}
