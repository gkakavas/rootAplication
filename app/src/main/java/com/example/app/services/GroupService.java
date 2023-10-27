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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService implements CrudService<GroupResponseEntity, GroupRequestEntity, GroupNotFoundException>{
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final EntityResponseGroupConverter groupConverter;


    @Override
    public GroupResponseEntity create(GroupRequestEntity request) throws UserNotFoundException {
        var users = userRepo.findAllById(request.getIdsSet());
        var groupCreator = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(UserNotFoundException::new);
        var group = groupConverter.fromRequestToGroup(request,groupCreator.getUserId());
        group.getGroupHasUsers().addAll(users);
        var newGroup = groupRepo.save(group);
        for(User user:users){
            user.setGroup(group);
            userRepo.save(user);
        }
        //must return only one the users and no the group inside users
        return groupConverter.fromGroupToAdminGroup(newGroup);
    }
    @Override
    public GroupResponseEntity read(UUID id) throws GroupNotFoundException{
        var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UsernameNotFoundException(""));
        if(currentUser.getRole().equals(Role.ADMIN)){
            return groupConverter.fromGroupToAdminGroup(group);
        }
        else if (currentUser.getRole().equals(Role.MANAGER)) {
            return groupConverter.fromGroupToMngGroup(group);
        }
        else
            throw new AccessDeniedException("You have not authority to access this resource");
    }
    @Override
    public List<GroupResponseEntity> read() {
        List<Group> groups = groupRepo.findAll();
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())

                .orElseThrow(() -> new UsernameNotFoundException(""));
        if (currentUser.getRole().equals(Role.ADMIN)) {
            return groupConverter.fromGroupListToAdminGroupList(groups);
        } else if (currentUser.getRole().equals(Role.MANAGER)) {
            return groupConverter.fromGroupListToMngGroupList(groups);
        } else {
            throw new AccessDeniedException("You have not authority to access this resource");
        }
    }
    @Override
    public GroupResponseEntity update(UUID id, GroupRequestEntity request) throws GroupNotFoundException {
        var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
        group.setGroupName(request.getGroupName());
        var updatedGroup = groupRepo.save(group);
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new UsernameNotFoundException(""));
        if(currentUser.getRole().equals(Role.ADMIN)){
            return groupConverter.fromGroupToAdminGroup(updatedGroup);
        }
        else if(currentUser.getRole().equals(Role.MANAGER)){
            return groupConverter.fromGroupToMngGroup(updatedGroup);
        }
        else
            throw new AccessDeniedException("You have not the authority to access this resource");

    }
    @Override
    public boolean delete(UUID id) throws GroupNotFoundException{
        var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
        for(User user:group.getGroupHasUsers()){
            user.setGroup(null);
        }
        group.getGroupHasUsers().clear();
        groupRepo.save(group);
        groupRepo.deleteById(id);
        return true;
    }

}
