package com.example.app.utils.converters.group;

import com.example.app.entities.Group;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.models.responses.group.ManagerGroupResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.converters.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EntityResponseGroupConverterImpl implements EntityResponseGroupConverter{
    private final EntityResponseUserConverter userConverter;
    private final UserRepository userRepo;
    @Override
    public ManagerGroupResponse fromGroupToMngGroup(Group group){
        return ManagerGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .users(userConverter.fromUserListToOtherList(group.getGroupHasUsers()))
                .build();
    }
    @Override
    public List<GroupResponseEntity> fromGroupListToMngGroupList(List<Group> groups){
        return groups.stream().map(this::fromGroupToMngGroup).distinct().collect(Collectors.toList());
    }
    @Override
    public GroupResponseEntity fromGroupToAdminGroup(Group group){
        var response = AdminGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupCreationDate(group.getGroupCreationDate())
                .groupCreator(null)
                .users(userConverter.fromUserListToAdminList(group.getGroupHasUsers()))
                .build();
        if(group.getGroupCreator()!=null) {
            try {
                response.setGroupCreator(userRepo.findById(group.getGroupCreator()).orElseThrow().getEmail());
            } catch (NoSuchElementException e) {
                response.setGroupCreator(null);
            }
        }
        return response;
    }

    @Override
    public List<GroupResponseEntity> fromGroupListToAdminGroupList(List<Group> groups){
        return groups.stream().map(this::fromGroupToAdminGroup).distinct().collect(Collectors.toList());
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
