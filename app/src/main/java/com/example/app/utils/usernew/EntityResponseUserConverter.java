package com.example.app.utils.usernew;

import com.example.app.entities.User;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;

import java.util.List;

public interface EntityResponseUserConverter {
    AdminUserResponse fromUserToAdminUser(User user);
    OtherUserResponse fromUserToOtherUser(User user);
    List<AdminUserResponse> fromUserListToAdminList(List<User> users);
    List<OtherUserResponse> fromUserListToOtherList(List<User> users);
    List<UserWithLeaves> usersWithLeaves();
    List<UserWithFiles> usersWithFilesList(List<User> users);

}
