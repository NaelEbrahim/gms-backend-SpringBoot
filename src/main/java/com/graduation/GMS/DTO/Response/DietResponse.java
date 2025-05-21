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
public class DietResponse {

    private Integer id;

    private UserResponse coach;

    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

    private Float rate;

    private List<MealResponse> meals;

    private List<UserFeedBackResponse> feedBacks;

}
