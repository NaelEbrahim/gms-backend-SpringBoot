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
public class DietRequest {

    @NotBlank(message = "Class Title is required")
    @Size(max = 100, message = "Class Title must not exceed 100 characters")
    private String title;

}
