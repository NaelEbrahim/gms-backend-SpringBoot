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
public class QrSessionAttendanceRequest {

    @NotNull(message = "QR is required")

    private String qrCode;

    private Integer sessionId;
}
