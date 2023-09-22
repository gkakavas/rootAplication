package com.example.app.models.requests;

import com.example.app.entities.Role;
import com.example.app.utils.validator.user.ValueOfEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @Size(min = 3, max = 255)
    private String firstname;
    @Size(min = 3, max = 255)
    private String lastname;
    @Email
    private String email;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must be at least 8 characters long and it contains at least one letter and one digit")
    private String password;
    @ValueOfEnum(enumClass = Role.class)
    private String role;
    private String specialization;
    private String currentProject;
}
