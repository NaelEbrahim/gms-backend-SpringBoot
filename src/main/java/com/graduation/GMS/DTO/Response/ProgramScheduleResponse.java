package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.Enums.Day;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProgramScheduleResponse {

    private Map<Day, WorkoutDayResponse> days;

}
