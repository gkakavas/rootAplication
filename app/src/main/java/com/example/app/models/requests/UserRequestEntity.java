package com.example.app.models.requests;

import com.example.app.entities.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull
    @Size(min = 1, max = 255)
    private String firstname;
    @NotNull
    @Size(min = 1, max = 255)
    private String lastname;
    @NotNull
    private String password;
    @NotNull
    @Email
    private String email;
    private String specialization;
    private UUID group;
    private UUID event;
    private UUID file;
    private String currentProject;
    @NotNull
    @Enumerated(value = EnumType.STRING)
    private Role role;

}

