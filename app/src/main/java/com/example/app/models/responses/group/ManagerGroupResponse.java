package com.example.app.models.responses.group;

import com.example.app.models.responses.user.OtherUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagerGroupResponse implements GroupResponseEntity{
    private UUID groupId;
    private String groupName;
    @Builder.Default
    private Set<OtherUserResponse> users = new HashSet<>();
}
