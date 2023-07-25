package com.example.app.utils.user;

import com.example.app.entities.User;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserToOtherUser{
    //24/7
    public UserResponseEntity convertToOtherUser(User user){
        return new OtherUserResponse(
                user.getUserId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getSpecialization(),
                user.getCurrentProject(),
                user.getGroup());
    }
}
