package com.example.app.models.responses.group;

import com.example.app.models.responses.user.AdminUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminGroupResponse implements GroupResponseEntity{
    private UUID groupId;
    private String groupName;
    private String groupCreator;
    private LocalDateTime groupCreationDate;
    @Builder.Default
    private Set<AdminUserResponse> users = new HashSet<>();
}
