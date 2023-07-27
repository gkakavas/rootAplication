package com.example.app.models.responses.leave;

import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.utils.user.UserToOtherUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class UserWithLeaves implements LeaveResponseEntity {
    private UserResponseEntity otherUser;
    private List<LeaveResponseEntity> leavesList;
}
