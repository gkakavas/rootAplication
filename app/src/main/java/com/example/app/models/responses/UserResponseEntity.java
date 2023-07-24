package com.example.app.models.responses;

import com.example.app.entities.Event;
import com.example.app.entities.File;
import com.example.app.entities.Group;
import com.example.app.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
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
    private UUID createdBy;
    private Role role;
    private Group group;
    @Builder.Default
    private Set<File> files = new HashSet<>();
    @Builder.Default
    private Set<Event> events = new HashSet<>();
    private Collection<? extends GrantedAuthority> authority;

    public UserResponseEntity(UUID userId, String firstname, String lastname, String email, String specialization, String currentProject, Group group) {
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.specialization = specialization;
        this.currentProject = currentProject;
        this.group = group;
    }
}
