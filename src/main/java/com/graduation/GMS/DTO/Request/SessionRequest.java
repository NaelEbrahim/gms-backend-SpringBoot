package com.graduation.GMS.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

    Integer coachId;

    Integer classId;

    String title;

    String description;

    Integer maxNumber;

    List<SessionScheduleRequest> schedules;

    @Setter
    @Getter
    public static class SessionScheduleRequest {
        DayOfWeek day;

        LocalTime startTime;

        LocalTime endTime;
    }

}
