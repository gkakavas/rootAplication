package com.example.app.models.responses.user;
import com.example.app.entities.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AdminUserResponse implements UserResponseEntity {
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private String groupName;
    private String createdBy;
    private LocalDateTime registerDate;
    //last login must be removed
    private LocalDateTime lastLogin;
    private Role role;

}
