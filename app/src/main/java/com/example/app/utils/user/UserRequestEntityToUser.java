package com.example.app.utils.user;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRequestEntityToUser {
    private final PasswordEncoder passwordEncoder;
    public User convertToEntity(UserRequestEntity request, UUID userCreator, Group userGroup) {
        return User.builder()
                .password(passwordEncoder.encode("Cdb3zgy2"))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .specialization(request.getSpecialization())
                .currentProject(request.getCurrentProject())
                .createdBy(userCreator)
                .registerDate(LocalDateTime.now())
                .lastLogin(null)
                .role(request.getRole())
                .group(userGroup)
                .userHasEvents(null)
                .userHasFiles(null)
                .build();
    }
}
