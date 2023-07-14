package com.example.app.utils;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.GroupResponseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class GroupMapper {
    public Group convertToGroup(GroupRequestEntity request, UUID createdBy){
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setGroupCreator(createdBy);
        group.setGroupCreationDate(LocalDateTime.now());
        //group.getGroupHasUsers().add(request.getIdsSet());
        return group;
    }
    public GroupResponseEntity convertToResponse(Group group){
        GroupResponseEntity response = new GroupResponseEntity();
        response.setGroupId(group.getGroupId());
        response.setGroupName(group.getGroupName());
        response.setGroupCreator(group.getGroupCreator());
        response.setGroupCreationDate(group.getGroupCreationDate());
        response.getUserSet().addAll(group.getGroupHasUsers());
        return response;
    }
}
