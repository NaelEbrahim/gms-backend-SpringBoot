package com.graduation.GMS.DTO.Request;


import com.graduation.GMS.Tools.Generators;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ForgetPasswordRequest {

    @Email(message = "Invalid email format", regexp = Generators.emailRegex)
    private String email;

    @Size(min = 6, max = 6, message = "verification code must be 6-digit")
    private String code;

    @Pattern(regexp = Generators.passwordRegex,
            message = "password must contain 8 chars and contain 1 digit, 1 uppercase, 1 lowercase, and 1 special character")
    private String newPassword;

}
