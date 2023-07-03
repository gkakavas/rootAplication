package com.example.app.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalAccountResponse {
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private Instant registerDate;
    private Instant lastLogin;
}
