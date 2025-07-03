package com.graduation.GMS.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Integer id;
    private String title;
    private String description;
    private int classId;
    private UserResponse coach;
    private Float rate;
    private List<String> days;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxNumber;
    private int subscribersCount;
    private List<UserFeedBackResponse> feedbacks;
}
