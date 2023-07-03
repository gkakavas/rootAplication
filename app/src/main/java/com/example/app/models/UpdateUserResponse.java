package com.example.app.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserResponse {
    private Integer userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
}

