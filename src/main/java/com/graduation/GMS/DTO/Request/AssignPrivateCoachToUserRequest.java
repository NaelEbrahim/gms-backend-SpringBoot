package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignPrivateCoachToUserRequest {

    @NotNull(message = "Coach Id is required")
    private Integer coachId;

    @NotNull(message = "User Id is required")
    private Integer userId;

    @DecimalMin(value = "0.0", message = "Payment amount cannot be negative")
    private Float paymentAmount;
}
