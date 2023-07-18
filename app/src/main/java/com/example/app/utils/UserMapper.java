package com.example.app.utils;

import com.example.app.entities.*;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMapper {
private final PasswordEncoder passwordEncoder;

    public User convertToEntity(UserRequestEntity request, UUID userCreator, Group userGroup) {
        User user = new User();
        user.setPassword(passwordEncoder.encode("Cdb3zgy2"));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setSpecialization(request.getSpecialization());
        user.setCurrentProject(request.getCurrentProject());
        user.setCreatedBy(userCreator);
        user.setRegisterDate(LocalDateTime.now());
        user.setLastLogin(null);
        user.setRole(request.getRole());
        user.setGroup(userGroup);
        user.setUserHasEvents(null);
        user.setUserHasFiles(null);
        return user;
    }

    //Response for ROLE_ADMIN, ROLE_HR, ROLE_MANAGER
    public UserResponseEntity convertToResponse(User user) {
        UserResponseEntity response = new UserResponseEntity();
        response.setUserId(user.getUserId());
        response.setFirstname(user.getFirstname());
        response.setLastname(user.getLastname());
        response.setEmail(user.getEmail());
        response.setSpecialization(user.getSpecialization());
        response.setCurrentProject(user.getCurrentProject());
        response.setCreatedBy(user.getCreatedBy());
        response.setRole(user.getRole());
        response.setGroup(user.getGroup());
        response.setEvents(user.getUserHasEvents());
        response.setFiles(user.getUserHasFiles());
        return response;
    }
}