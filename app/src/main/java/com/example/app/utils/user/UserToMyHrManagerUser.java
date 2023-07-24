package com.example.app.utils.user;

import com.example.app.entities.User;
import com.example.app.models.responses.user.MyHrManagerUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserToMyHrManagerUser {
    //24/7
    public MyHrManagerUserResponse convertToMyHrManagerUser(User user){
        return new MyHrManagerUserResponse(
                user.getUserId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getSpecialization(),
                user.getCurrentProject(),
                user.getGroup());

    }
}
