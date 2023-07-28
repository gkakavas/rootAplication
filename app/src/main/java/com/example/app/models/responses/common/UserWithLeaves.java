package com.example.app.models.responses.common;

import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.user.UserResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserWithLeaves {
    private UserResponseEntity user;
    private List<LeaveResponseEntity> leaves;
}
