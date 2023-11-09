package com.example.app.services;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.converters.group.EntityResponseGroupConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final EntityResponseGroupConverter groupConverter;


    public GroupResponseEntity create(GroupRequestEntity request,User connectedUser) throws UserNotFoundException {
        var users = userRepo.findAllById(request.getIdsSet());
        var group = groupConverter.fromRequestToGroup(request,connectedUser.getUserId());
        group.getGroupHasUsers().addAll(users);
        for(User user:users){
            user.setGroup(group);
        }
        var newGroup = groupRepo.save(group);
        return groupConverter.fromGroupToAdminGroup(newGroup);
    }

    public GroupResponseEntity read(UUID id,User connectedUser) throws GroupNotFoundException{
        var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
        if(connectedUser.getRole().equals(Role.ADMIN)){
            return groupConverter.fromGroupToAdminGroup(group);
        }
        else if (connectedUser.getRole().equals(Role.MANAGER)) {
            return groupConverter.fromGroupToMngGroup(connectedUser.getGroup());
        }
        else
            throw new AccessDeniedException("You have not authority to access this resource");
    }
    public List<GroupResponseEntity> read(User connectedUser) {
        List<Group> groups = groupRepo.findAll();
        if(connectedUser.getRole().equals(Role.ADMIN)){
            return groupConverter.fromGroupListToAdminGroupList(groups);
        }
        else{
            return groupConverter.fromGroupListToMngGroupList(groups);
        }
    }

    public GroupResponseEntity update(UUID id, GroupRequestEntity request) throws GroupNotFoundException {
        var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
        group.setGroupName(request.getGroupName());
        if(request.getIdsSet()!=null){
            var updatedGroupUsers = userRepo.findAllById(request.getIdsSet());
            group.getGroupHasUsers().clear();
            group.getGroupHasUsers().addAll(updatedGroupUsers);
        }
        var updatedGroup = groupRepo.save(group);
        return groupConverter.fromGroupToAdminGroup(updatedGroup);
    }
    public boolean delete(UUID id) throws GroupNotFoundException{
        var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
        for(User user:group.getGroupHasUsers()){
            user.setGroup(null);
        }
        group.getGroupHasUsers().clear();
        groupRepo.save(group);
        groupRepo.delete(group);
        return !groupRepo.existsById(group.getGroupId());
    }

}
