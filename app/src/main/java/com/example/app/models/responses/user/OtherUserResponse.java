package com.example.app.models.responses.user;

import com.example.app.entities.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class OtherUserResponse {
    //24/7
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private Group group;
}
