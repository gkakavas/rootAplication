package com.example.app.models;

import com.example.app.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UserRequestEntity {
    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    @NotNull
    private String password;
    @NotNull
    @Email
    private String email;
    @NotNull
    private String specialization;
    @NotNull
    private Role role;

    public UserRequestEntity(@NotNull String email){
        this.email = email;
    }
    }

