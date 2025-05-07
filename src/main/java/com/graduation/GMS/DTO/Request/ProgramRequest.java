package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class  ProgramRequest {

    @NotBlank(message = "Program title is required")
    @Size(max = 100, message = "Program title must not exceed 100 characters")
    private String title;

    private Level level;

    private String isPublic;
}
