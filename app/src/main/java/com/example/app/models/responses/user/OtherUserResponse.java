package com.example.app.models.responses.user;

import com.example.app.entities.Group;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class OtherUserResponse implements UserResponseEntity {
    //24/7
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private String groupName;
}
