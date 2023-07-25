package com.example.app.models.responses.group;

import com.example.app.entities.User;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
public class ManagerGroupResponse implements GroupResponseEntity{
    private UUID groupId;
    private String groupName;
    @Builder.Default
    private Set<UserResponseEntity> users = new HashSet<>();
}
