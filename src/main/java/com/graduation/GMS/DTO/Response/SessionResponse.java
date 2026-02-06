package com.graduation.GMS.DTO.Response;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Integer id;

    private String title;

    private String description;

    private int classId;

    private UserResponse coach;

    private Float rate;

    private List<Schedule> schedules;

    private LocalDateTime createdAt;

    private int maxNumber;

    private int subscribersCount;

    private List<UserFeedBackResponse> feedbacks;

    private String myFeedBack;

    private LocalDateTime joinedAt;

    private String className;

    private String classImage;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schedule {
        private String day;

        private LocalTime startTime;

        private LocalTime endTime;
    }
}
