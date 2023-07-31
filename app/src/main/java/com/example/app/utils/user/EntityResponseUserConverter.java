package com.example.app.utils.user;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.UserResponseEntity;

import java.util.List;
import java.util.UUID;

public interface EntityResponseUserConverter {
    UserResponseEntity fromUserToAdminUser(User user);
    UserResponseEntity fromUserToOtherUser(User user);
    List<UserResponseEntity> fromUserListToAdminList(List<User> users);
    List<UserResponseEntity> fromUserListToOtherList(List<User> users);
    User fromRequestToEntity(UserRequestEntity request, UUID userCreator, Group userGroup);
    User updateSetting(User user, UserRequestEntity request, Group group);
}
