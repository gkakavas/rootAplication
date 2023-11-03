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
import com.example.app.utils.group.EntityResponseGroupConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final EntityResponseGroupConverter groupConverter;


    public GroupResponseEntity create(GroupRequestEntity request, Principal connectedUser) throws UserNotFoundException {
        var groupCreator = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var users = userRepo.findAllById(request.getIdsSet());
        var group = groupConverter.fromRequestToGroup(request,groupCreator.getUserId());
        group.getGroupHasUsers().addAll(users);
        for(User user:users){
            user.setGroup(group);
        }
        var newGroup = groupRepo.save(group);
        return groupConverter.fromGroupToAdminGroup(newGroup);
    }

    public GroupResponseEntity read(UUID id,Principal connectedUser) throws GroupNotFoundException{
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
        if(currentUser.getRole().equals(Role.ADMIN)){
            return groupConverter.fromGroupToAdminGroup(group);
        }
        else if (currentUser.getRole().equals(Role.MANAGER)) {
            return groupConverter.fromGroupToMngGroup(currentUser.getGroup());
        }
        else
            throw new AccessDeniedException("You have not authority to access this resource");
    }
    public List<GroupResponseEntity> read(Principal connectedUser) {
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        List<Group> groups = groupRepo.findAll();
        if(currentUser.getRole().equals(Role.ADMIN)){
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
