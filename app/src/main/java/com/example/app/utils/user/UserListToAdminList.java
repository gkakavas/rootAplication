package com.example.app.utils.user;

import com.example.app.entities.User;
import com.example.app.models.responses.user.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserListToAdminList {
    private final UserToAdminUser toAdminUser;
    public List<AdminUserResponse> convertToAdminUser(List<User> userList) {
        List<AdminUserResponse> responseList = new ArrayList<>();
        userList.forEach((user) -> responseList.add(
                (AdminUserResponse) toAdminUser.convertToAdminUser(user)));
        return responseList;
    }
}
