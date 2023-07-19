package com.example.app.utils;

import com.example.app.entities.Group;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.GroupResponseEntity;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class GroupMapper {
    public Group convertToGroup(GroupRequestEntity request, UUID createdBy){
        return Group.builder()
                .groupName(request.getGroupName())
                .groupCreator(createdBy)
                .groupCreationDate(LocalDateTime.now())
                .build();
    }
    public GroupResponseEntity convertToResponse(Group group){
        return GroupResponseEntity.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupCreator(group.getGroupCreator())
                .groupCreationDate(group.getGroupCreationDate())
                .userSet(group.getGroupHasUsers())
                .build();
    }
}
