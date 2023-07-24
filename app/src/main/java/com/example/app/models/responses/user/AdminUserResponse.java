package com.example.app.models.responses.user;

import com.example.app.entities.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class AdminUserResponse extends MyHrManagerUserResponse{
   //24/7
    private UUID createdBy;
    private LocalDateTime registerDate;
    private LocalDateTime lastLogin;
    private Role role;

    public AdminUserResponse(UUID userId, String firstname, String lastname, String email, String specialization, String currentProject, Group group, UUID createdBy, LocalDateTime registerDate, LocalDateTime lastLogin, Role role) {
        super(userId, firstname, lastname, email, specialization, currentProject, group);
        this.createdBy = createdBy;
        this.registerDate = registerDate;
        this.lastLogin = lastLogin;
        this.role = role;
    }
}
