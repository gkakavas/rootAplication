package com.example.app.models;

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
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;

    public UserRequestEntity(UUID id){
        this.userId = id;
    }

    public UserRequestEntity(String email){
        this.email = email;
    }


}

