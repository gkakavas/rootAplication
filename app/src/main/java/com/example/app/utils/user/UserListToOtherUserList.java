package com.example.app.utils.user;

import com.example.app.entities.User;
import com.example.app.models.responses.user.OtherUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserListToOtherUserList {
    private final UserToOtherUser toOtherUser;
    public List<OtherUserResponse> convertToOtheUserList(List<User> userList){
        List<OtherUserResponse> responseList = new ArrayList<>();
        userList.forEach((user)->responseList.add(
                (OtherUserResponse) toOtherUser.convertToOtherUser(user)));
        return responseList;
    }
}
