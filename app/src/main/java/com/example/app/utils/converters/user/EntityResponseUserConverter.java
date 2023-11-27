package com.example.app.utils.converters.user;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.CurrentUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;

import java.util.Set;
import java.util.UUID;

public interface EntityResponseUserConverter {
    AdminUserResponse fromUserToAdminUser(User user);
    OtherUserResponse fromUserToOtherUser(User user);
    Set<AdminUserResponse> fromUserListToAdminList(Set<User> users);
    Set<OtherUserResponse> fromUserListToOtherList(Set<User> users);
    User fromRequestToEntity(UserRequestEntity request, UUID userCreator, Group userGroup);
    User updateSetting(User user, UserRequestEntity request, Group group);
    CurrentUserResponse fromUserToCurrentUser(User user);
}
