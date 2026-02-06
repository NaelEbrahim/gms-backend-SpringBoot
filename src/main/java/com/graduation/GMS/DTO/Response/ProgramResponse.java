package com.graduation.GMS.DTO.Response;


import com.graduation.GMS.Models.Enums.Level;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramResponse {

    private Integer id;

    private String name;

    private Level level;

    private Boolean isPublic;

    private Float rate;

    private ProgramScheduleResponse schedule;

    private List<UserFeedBackResponse> feedbacks;
}
