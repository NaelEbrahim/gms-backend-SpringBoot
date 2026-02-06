package com.graduation.GMS.DTO.Response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietResponse {

    private Integer id;

    private UserResponse coach;

    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

    private Float rate;

    private Boolean isActive;

    private ScheduleResponse schedule;

    private List<UserFeedBackResponse> feedBacks;

    private String myFeedback;

    private LocalDateTime startedAt;

    public DietResponse(Integer id, UserResponse coach, String title, LocalDateTime createdAt, LocalDateTime lastModifiedAt, Float rate, ScheduleResponse schedule, List<UserFeedBackResponse> feedBacks) {
        this.id = id;
        this.coach = coach;
        this.title = title;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.rate = rate;
        this.schedule = schedule;
        this.feedBacks = feedBacks;
    }
}
