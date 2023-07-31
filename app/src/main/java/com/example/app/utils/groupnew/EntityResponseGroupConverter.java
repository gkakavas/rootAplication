package com.example.app.utils.groupnew;

import com.example.app.entities.Group;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.models.responses.group.ManagerGroupResponse;

import java.util.List;
import java.util.UUID;

public interface EntityResponseGroupConverter {
    ManagerGroupResponse fromGroupToMngGroup(Group group);

    List<ManagerGroupResponse> fromGroupListToMngGroupList(List<Group> groups);

    GroupResponseEntity fromGroupToAdminGroup(Group group);

    List<GroupResponseEntity> fromGroupListToAdminGroupList(List<Group> groups);

    Group convertToGroup(GroupRequestEntity request, UUID createdBy);
}
