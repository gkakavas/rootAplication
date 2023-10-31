package com.example.app.models.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message ="Old password is required")
    private String oldPassword;
    @NotBlank(message ="New password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must be at least 8 characters long and it contains at least one letter and one digit")
    private String newPassword;
    @NotBlank(message ="Confirmation of new password is required")
    private String confirmationNewPassword;
}
