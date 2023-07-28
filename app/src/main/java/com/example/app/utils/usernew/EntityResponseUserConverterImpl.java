package com.example.app.utils.usernew;

import com.example.app.entities.User;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EntityResponseUserConverterImpl implements EntityResponseUserConverter{
    @Override
    public AdminUserResponse fromUserToAdminUser(User user) {
        return null;
    }

    @Override
    public OtherUserResponse fromUserToOtherUser(User user) {
        return null;
    }

    @Override
    public List<AdminUserResponse> fromUserListToAdminList(List<User> users) {
        return null;
    }

    @Override
    public List<OtherUserResponse> fromUserListToOtherList(List<User> users) {
        return null;
    }

    @Override
    public List<UserWithLeaves> usersWithLeaves() {
        return null;
    }

    @Override
    public List<UserWithFiles> usersWithFilesList(List<User> users) {
        return null;
    }
}
