package com.example.app.models.responses.user;
import com.example.app.entities.*;
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
    private Group group;
    private UUID createdBy;
    private LocalDateTime registerDate;
    private LocalDateTime lastLogin;
    private Role role;

}
