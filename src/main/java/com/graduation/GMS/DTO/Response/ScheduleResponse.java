package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.Enums.Day;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Map<Day, MealDayResponse> days;
}
