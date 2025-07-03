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
public class FeedBackSessionRequest {

    @NotNull(message = "Session Id is required")
    private Integer sessionId;

    private String feedback;
}
