package com.example.app.models.responses.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherUserResponse implements UserResponseEntity {
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private String groupName;
}
