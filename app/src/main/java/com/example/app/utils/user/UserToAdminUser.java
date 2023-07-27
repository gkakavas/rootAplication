package com.example.app.utils.user;

import com.example.app.entities.User;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.utils.group.GroupToAdminGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserToAdminUser {
    //24/7
    public UserResponseEntity convertToAdminUser(User user) {
            return new AdminUserResponse(
                    user.getUserId(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getEmail(),
                    user.getSpecialization(),
                    user.getCurrentProject(),
                    user.getGroup(),
                    user.getCreatedBy(),
                    user.getRegisterDate(),
                    user.getLastLogin(),
                    user.getRole());
    }
}

