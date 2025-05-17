package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UpdateScoreRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Event ID is required")
    private Integer eventId;

    @NotNull(message = "Score is required")
    private Float score;
}
