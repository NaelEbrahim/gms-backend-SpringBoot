package com.graduation.GMS.DTO.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.graduation.GMS.Models.Enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Setter
@Getter
public class UpdateProfileRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Invalid email format", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
    private String email;

    @Size(min = 10, max = 20, message = "phone number must between 10-20 digit")
    private String phoneNumber;

    private Gender gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

}
