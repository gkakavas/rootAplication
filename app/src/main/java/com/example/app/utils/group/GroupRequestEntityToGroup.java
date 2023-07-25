package com.example.app.utils.group;

import com.example.app.entities.Group;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.GroupResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class GroupRequestEntityToGroup {
    public Group convertToGroup(GroupRequestEntity request, UUID createdBy) {
        return Group.builder()
                .groupName(request.getGroupName())
                .groupCreator(createdBy)
                .groupCreationDate(LocalDateTime.now())
                .build();
    }
}
