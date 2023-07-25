package com.example.app.utils.group;

import com.example.app.entities.Group;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.utils.user.UserToAdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupToAdminGroup {
    private final UserToAdminUser toAdminUser;
    public GroupResponseEntity convertToAdminGroup(Group group){
        var response = AdminGroupResponse.builder()
                        .groupId(group.getGroupId())
                        .groupName(group.getGroupName())
                        .groupCreationDate(group.getGroupCreationDate())
                        .groupCreator(group.getGroupCreator())
                        .build();
        group.getGroupHasUsers().forEach((user)->
                response.getUsers()
                        .add((AdminUserResponse)
                                toAdminUser.convertToAdminUser(user)));
        return response;
    }

    public List<GroupResponseEntity> convertToAdminGroup(List<Group> groups){
        List<GroupResponseEntity> adminGroupResponseList = new ArrayList<>();
        groups.forEach(group->adminGroupResponseList.add(AdminGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .build()));
        return adminGroupResponseList;
    }

}
