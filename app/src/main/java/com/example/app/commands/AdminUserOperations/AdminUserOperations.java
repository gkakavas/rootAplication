package com.example.app.commands.AdminUserOperations;

import com.example.app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@FunctionalInterface
public interface AdminUserOperations<SERVICE> {

    SERVICE adminUserCreation();



}
