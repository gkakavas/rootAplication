package com.example.app.tool.utils;

import com.example.app.entities.User;

import java.security.Principal;

public class CustomPrincipal extends User implements Principal {
    public CustomPrincipal(User user) {
        super(
                user.getUserId(),
                user.getPassword(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getSpecialization(),
                user.getCurrentProject(),
                user.getCreatedBy(),
                user.getRegisterDate(),
                user.getLastLogin(),
                user.getRoleValue(),
                user.getRole(),
                user.getGroup(),
                user.getUserHasEvents(),
                user.getUserHasFiles(),
                user.getUserRequestedLeaves()
        );
    }
    @Override
    public String getName() {
        return getUsername();
    }
}
