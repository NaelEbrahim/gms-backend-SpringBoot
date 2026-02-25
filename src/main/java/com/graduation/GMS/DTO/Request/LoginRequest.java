package com.graduation.GMS.DTO.Request;


import com.graduation.GMS.Tools.Generators;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {

    @NotBlank(message = "email is required")
    @Email(message = "invalid email format", regexp = Generators.emailRegex)
    private String email;

    @Pattern(regexp = Generators.passwordRegex,
            message = "password must contain 8 chars and contain 1 digit, 1 uppercase, 1 lowercase, and 1 special character")
    private String password;

}
