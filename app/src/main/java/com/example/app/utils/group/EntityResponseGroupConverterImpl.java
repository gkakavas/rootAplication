package com.example.app.utils.group;

import com.example.app.entities.Group;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.models.responses.group.ManagerGroupResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EntityResponseGroupConverterImpl implements EntityResponseGroupConverter{
    private final EntityResponseUserConverter userConverter;
    private final UserRepository userRepo;
    @Override
    public ManagerGroupResponse fromGroupToMngGroup(Group group){
        var response = ManagerGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .build();
        group.getGroupHasUsers().forEach((user)->
                response.getUsers()
                        .add((OtherUserResponse) userConverter.fromUserToOtherUser(user)));
        return response;
    }
    @Override
    public List<GroupResponseEntity> fromGroupListToMngGroupList(List<Group> groups){
        List<GroupResponseEntity> managerGroupResponseList = new ArrayList<>();
        groups.forEach(group->managerGroupResponseList.add(fromGroupToMngGroup(group)));
        return managerGroupResponseList;
    }
    @Override
    public GroupResponseEntity fromGroupToAdminGroup(Group group){
        var response = AdminGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupCreationDate(group.getGroupCreationDate())
                .groupCreator(null)
                .build();
        group.getGroupHasUsers().forEach((user)->
                response.getUsers()
                        .add((AdminUserResponse)
                                userConverter.fromUserToAdminUser(user)));
        try {
            response.setGroupCreator(userRepo.findById(group.getGroupCreator()).orElseThrow().getEmail());
        }catch (NoSuchElementException e){
            response.setGroupCreator(null);
        }
        return response;
    }

    @Override
    public List<GroupResponseEntity> fromGroupListToAdminGroupList(List<Group> groups){
        List<GroupResponseEntity> adminGroupResponseList = new ArrayList<>();
        groups.forEach(group->adminGroupResponseList.add(fromGroupToAdminGroup(group)));
        return adminGroupResponseList;
    }

    @Override
    public Group fromRequestToGroup(GroupRequestEntity request, UUID createdBy) {
        return Group.builder()
                .groupName(request.getGroupName())
                .groupCreator(createdBy)
                .groupCreationDate(LocalDateTime.now())
                .build();
    }
}
