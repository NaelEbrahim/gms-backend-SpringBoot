package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Event title is required")
    private String title;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startedAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endedAt;

    private MultipartFile image;

}
