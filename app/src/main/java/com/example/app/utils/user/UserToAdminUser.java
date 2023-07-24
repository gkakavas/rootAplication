package com.example.app.utils.user;

import com.example.app.entities.User;
import com.example.app.models.responses.user.AdminUserResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserToAdminUser implements UserResponseEntity{
    //24/7
    public AdminUserResponse convertToAdminUser(User user) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ROLE_ADMIN"))) {
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
        return null;
    }
}
