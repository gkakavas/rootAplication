package com.example.app.utils;

import com.example.app.entities.*;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMapper {
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

    public User updateSetting(User user,UserRequestEntity request,Group group){
        user.setPassword(request.getPassword());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setSpecialization(request.getSpecialization());
        user.setCurrentProject(request.getCurrentProject());
        user.setRole(request.getRole());
        user.setGroup(group);
        return user;
    }
}