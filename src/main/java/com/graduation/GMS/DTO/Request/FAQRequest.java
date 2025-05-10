package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FAQRequest {

    @NotBlank(message = "AdminId is required")
    private int adminId;

    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question name must not exceed 500 characters")
    private String question;

    @NotBlank(message = "Answer is required")
    @Size(max = 500, message = "Answer name must not exceed 500 characters")
    private String answer;

}
