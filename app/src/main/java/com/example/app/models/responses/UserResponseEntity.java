package com.example.app.models.responses;

import com.example.app.entities.Event;
import com.example.app.entities.File;
import com.example.app.entities.Group;
import com.example.app.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseEntity {

    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private String createdBy;
    private Role role;
    private Group group;
    @Builder.Default
    private Set<File> files = new HashSet<>();
    @Builder.Default
    private Set<Event> events = new HashSet<>();
}
