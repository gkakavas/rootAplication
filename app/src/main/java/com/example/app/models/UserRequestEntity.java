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
    private String firstname;

    private String lastname;

    private String password;

    @Email
    private String email;

    private String specialization;

    private Role role;

    public UserRequestEntity(String email){
        this.email = email;
    }
    }

