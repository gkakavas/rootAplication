package com.example.app.services;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.group.GroupRequestEntityToGroup;
import com.example.app.utils.group.GroupToAdminGroup;
import com.example.app.utils.group.GroupToMngGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService implements CrudService<GroupResponseEntity, GroupRequestEntity, GroupNotFoundException>{
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final JwtService jwtService;
    private final GroupRequestEntityToGroup toGroup;
    private final GroupToAdminGroup toAdminGroup;
    private final GroupToMngGroup toManagerGroup;


    @Override
    public GroupResponseEntity create(GroupRequestEntity request,String token) throws UserNotFoundException {
            var users = userRepo.findAllById(request.getIdsSet());
            var groupCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                                                            .orElseThrow(UserNotFoundException::new);
            var group = toGroup.convertToGroup(request,groupCreator.getUserId());
            group.getGroupHasUsers().addAll(users);
            var newGroup = groupRepo.save(group);
            for(User user:users){
                user.setGroup(group);
                userRepo.save(user);
            }
            //must return only one the users and no the group inside users
            return toAdminGroup.convertToAdminGroup(newGroup);
    }

    @Override
    public GroupResponseEntity read(UUID id) throws GroupNotFoundException{
            var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
            var authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            if(authority.stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"))){
                return toAdminGroup.convertToAdminGroup(group);
            }
            else if (authority.stream().anyMatch(a->a.getAuthority().equals("ROLE_MANAGER"))) {
                return toManagerGroup.convertToMngGroup(group);
            }
            else
                throw new AccessDeniedException("You have not authority to access this resource");
    }
    @Override
    public List<GroupResponseEntity> read() {
        List<Group> groups = groupRepo.findAll();

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return toAdminGroup.convertToAdminGroup(groups);
        } else if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            return toManagerGroup.convertToMngGroup(groups);
        } else {
            throw new AccessDeniedException("You have not authority to access this resource");
        }
    }
    @Override
    public GroupResponseEntity update(UUID id, GroupRequestEntity request) throws GroupNotFoundException {
            var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
            group.setGroupName(request.getGroupName());
            var updatedGroup = groupRepo.save(group);
            var authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            if(authority.stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"))){
                return toAdminGroup.convertToAdminGroup(updatedGroup);
            }
            else if(authority.stream().anyMatch(a->a.getAuthority().equals("ROLE_MANAGER"))){
                return toManagerGroup.convertToMngGroup(updatedGroup);
            }
            else
                throw new AccessDeniedException("You have not the authority to access this resource");

    }
    @Override
    public boolean delete(UUID id) throws GroupNotFoundException{
            var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
            group.getGroupHasUsers().clear();
            groupRepo.deleteById(id);
            return true;
    }

}
