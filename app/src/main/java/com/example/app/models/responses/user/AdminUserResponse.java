package com.example.app.models.responses.user;
import com.example.app.entities.*;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AdminUserResponse implements UserResponseEntity {
   //24/7
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private String groupName;
    private String cratedBy;
    private LocalDateTime registerDate;
    private LocalDateTime lastLogin;
    private Role role;

}
