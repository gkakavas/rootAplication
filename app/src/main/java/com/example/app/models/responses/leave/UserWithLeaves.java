package com.example.app.models.responses.leave;

import com.example.app.models.responses.user.UserResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserWithLeaves implements LeaveResponseEntity {
    private UserResponseEntity otherUser;
    private List<LeaveResponseEntity> leavesList;
}
