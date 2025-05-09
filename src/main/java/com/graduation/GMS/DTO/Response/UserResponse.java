package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.graduation.GMS.Models.Enums.Gender;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Gender gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Column(updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(unique = true, nullable = false)
    private String qr;
}
