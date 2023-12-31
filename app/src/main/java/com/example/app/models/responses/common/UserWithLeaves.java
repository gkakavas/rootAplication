package com.example.app.models.responses.common;

import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.user.UserResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithLeaves implements LeaveResponseEntity{
    private UserResponseEntity user;
    private List<LeaveResponseEntity> leaves;
}
