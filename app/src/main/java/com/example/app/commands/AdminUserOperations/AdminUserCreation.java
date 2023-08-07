package com.example.app.commands.AdminUserOperations;

import com.example.app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
    @RequiredArgsConstructor
    class AdminUserCreation implements AdminUserOperations<UserService> {

        private final UserService service;

        @Override
        public UserService adminUserCreation() {
            return null;
        }
    }