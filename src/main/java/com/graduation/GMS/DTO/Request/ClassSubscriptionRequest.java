package com.graduation.GMS.DTO.Request;


import jakarta.validation.constraints.DecimalMax;
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
public class ClassSubscriptionRequest {

    @NotNull(message = "Class Id is required")
    private Integer classId;

    @NotNull(message = "User Id is required")
    private Integer userId;

    @DecimalMin(value = "0.0", message = "Payment amount cannot be negative")
    private Float paymentAmount;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private Float discountPercentage;

}
