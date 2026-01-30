package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EventResponse {

    private Integer id;

    private UserResponse admin;

    private String title;

    private String description;

    private String imagePath;

    private LocalDate startedAt;

    private LocalDate endedAt;

    private List<PrizeResponse> prizes;

    private List<ParticipantResponse> participants;

    public EventResponse(
            Integer id,
            User admin,
            String title,
            String description,
            String imagePath,
            LocalDate startedAt,
            LocalDate endedAt,
            List<PrizeResponse> prizes,
            List<ParticipantResponse> participants
    ) {
        this.id = id;
        this.admin = UserResponse.mapToUserResponse(admin);
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.prizes = prizes;
        this.participants = participants;
    }
}
