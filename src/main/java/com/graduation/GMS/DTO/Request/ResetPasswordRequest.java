package com.graduation.GMS.DTO.Request;


import com.graduation.GMS.Tools.Generators;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordRequest {

    @Pattern(regexp = Generators.passwordRegex,
            message = "password must contain 8 chars and contain 1 digit, 1 uppercase, 1 lowercase, and 1 special character")
    private String oldPassword;

    @Pattern(regexp = Generators.passwordRegex,
            message = "password must contain 8 chars and contain 1 digit, 1 uppercase, 1 lowercase, and 1 special character")
    private String newPassword;

}
