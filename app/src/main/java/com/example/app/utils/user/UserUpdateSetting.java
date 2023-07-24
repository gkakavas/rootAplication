package com.example.app.utils.user;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class UserUpdateSetting {
    public User updateSetting(User user, UserRequestEntity request, Group group){
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
