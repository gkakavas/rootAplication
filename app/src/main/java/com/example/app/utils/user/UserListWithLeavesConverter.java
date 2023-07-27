package com.example.app.utils.user;

import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.UserWithLeaves;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.utils.leave.LeaveListToAdminHrMngLeaveList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserListWithLeavesConverter {
    private final UserToOtherUser toOtherUser;
    private final LeaveListToAdminHrMngLeaveList toAdminHrMngLeaveList;
    public List<LeaveResponseEntity> userWithLeavesConvert(List<User> users)  {
        List<LeaveResponseEntity> responseList = new ArrayList<>();
        for(User user:users){
            responseList.add(new UserWithLeaves(
                     toOtherUser.convertToOtherUser(user),
                    toAdminHrMngLeaveList.convertToAdminHrMngLeaveList(new ArrayList<>(user.getUserRequestedLeaves()))
                    )
            );
        }
        return responseList;
    }
}
