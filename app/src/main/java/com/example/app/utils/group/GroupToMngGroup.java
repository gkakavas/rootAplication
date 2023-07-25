package com.example.app.utils.group;

import com.example.app.entities.Group;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.models.responses.group.ManagerGroupResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.utils.user.UserToAdminUser;
import com.example.app.utils.user.UserToOtherUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupToMngGroup {
    private final UserToOtherUser toMngUser;
    public ManagerGroupResponse convertToMngGroup(Group group){
            var response = ManagerGroupResponse.builder()
                    .groupId(group.getGroupId())
                    .groupName(group.getGroupName())
                    .build();
            group.getGroupHasUsers().forEach((user)->
                    response.getUsers()
                            .add(toMngUser.convertToOtherUser(user)));
            return response;
    }

    public List<GroupResponseEntity> convertToMngGroup(List<Group> groups){
        List<GroupResponseEntity> managerGroupResponseList = new ArrayList<>();
        groups.forEach(group->managerGroupResponseList.add(ManagerGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .build()));
        return managerGroupResponseList;
    }
}

