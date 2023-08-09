package com.example.app.models.requests;

import com.example.app.entities.Role;
import com.example.app.utils.validator.ValueOfEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UserRequestEntity {
    @NotNull(message = "Firstname is required")
    @Size(min = 4, max = 50,message = "Firstname must be between 4 and 50 characters")
    private String firstname;
    @NotNull(message = "Lastname is required")
    @Size(min = 4, max = 50,message = "Lastname must be between 4 and 50 characters")
    private String lastname;
    //only for update
    @NotNull(message ="Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must be at least 8 characters long and it contains at least one letter and one digit")
    private String password;
    @NotNull(message = "Email is required")
    @Email(message = "Email must be in a normal email form")
    private String email;
    private String specialization;
    private UUID group;
    private String currentProject;
    //must be validated for its type compatibility that gives the user
    @ValueOfEnum(enumClass = Role.class)
    @NotNull(message = "Role is required")
    private String role;

}

