package com.graduation.GMS.DTO.Response;


import com.graduation.GMS.Models.Enums.Level;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {

    private Integer id;

    private String name;

    private Level level;

    private Boolean isPublic;

    private Float rate;

    private List<WorkoutResponse> workouts;

    private List<UserFeedBackResponse> feedbacks;
}
