package com.graduation.GMS.DTO.Request;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.graduation.GMS.Models.Enums.WeekDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

    private Integer coachId;

    private Integer classId;
    @NotBlank(message = "Program title is required")
    @Size(max = 100, message = "Program title must not exceed 100 characters")
    private String title;

    private String description;

    private Integer maxNumber;

    private List<String> days;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
