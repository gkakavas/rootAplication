package com.example.app.services;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.GroupResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.GroupMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GroupService implements CrudService<GroupResponseEntity, GroupRequestEntity, GroupNotFoundException>{
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final JwtService jwtService;
    private final GroupMapper groupMapper;
    @Override
    public GroupResponseEntity create(GroupRequestEntity request,String token) throws UserNotFoundException {
            var users = userRepo.findAllById(request.getIdsSet());
            var groupCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                                                            .orElseThrow(UserNotFoundException::new);
            var group = groupMapper.convertToGroup(request,groupCreator.getUserId());
            group.getGroupHasUsers().addAll(users);
            var newGroup = groupRepo.save(group);
            for(User user:users){
                user.setGroup(group);
                userRepo.save(user);
            }
            return groupMapper.convertToResponse(newGroup);
    }

    @Override
    public GroupResponseEntity read(UUID id) throws GroupNotFoundException{
            var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
            return groupMapper.convertToResponse(group);
    }
    @Override
    public List<GroupResponseEntity> read() {
        List<Group> groups = groupRepo.findAll();
        return groups.stream()
                    .map(groupMapper::convertToResponse)
                    .collect(Collectors.toList());
    }
    @Override
    public GroupResponseEntity update(UUID id, GroupRequestEntity request) throws GroupNotFoundException {
            var group = groupRepo.findById(id).orElseThrow(GroupNotFoundException::new);
            var users = userRepo.findAllById(request.getIdsSet());
            group.setGroupName(request.getGroupName());
            group.getGroupHasUsers().addAll(users);
            var newGroup = groupRepo.save(group);
            for(User user:users){
                user.setGroup(group);
                userRepo.save(user);
            }
            var response = GroupResponseEntity.builder()
                    .groupId(newGroup.getGroupId())
                    .groupName(newGroup.getGroupName())
                    .groupCreator(newGroup.getGroupCreator())
                    .groupCreationDate(newGroup.getGroupCreationDate())
                    .build();
            response.getUserSet().addAll(newGroup.getGroupHasUsers());
            return response;
    }
    @Override
    public boolean delete(UUID id) throws GroupNotFoundException{
        if(id!=null) {
            groupRepo.deleteById(id);
            return true;
        }
        else{
            throw new GroupNotFoundException();
        }
    }

}
