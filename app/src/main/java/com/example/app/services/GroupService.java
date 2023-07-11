package com.example.app.services;

import com.example.app.entities.Group;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.GroupResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
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
public class GroupService implements CrudService<GroupResponseEntity, GroupRequestEntity>{
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    @Override
    public GroupResponseEntity create(GroupRequestEntity request,String header) {
        if(header.contains("Bearer")&&request!=null) {
            var users = userRepo.findAllById(request.getIdsSet());
            var user = userRepo.findByEmail(jwtService.extractUsername(header.substring(7)))
                                                            .orElseThrow(() -> new RuntimeException());
            var group = Group.builder()
                    .groupName(request.getGroupName())
                    .groupCreator(user.getUserId())
                    .groupCreationDate(LocalDateTime.now())
                    .build();
            group.getGroupHasUsers().addAll(users);
            var newGroup = groupRepo.save(group);
            return GroupResponseEntity.builder()
                    .groupId(newGroup.getGroupId())
                    .groupName(newGroup.getGroupName())
                    .groupCreationDate(newGroup.getGroupCreationDate())
                    .groupCreator(newGroup.getGroupCreator())
                    .userSet(newGroup.getGroupHasUsers())
                    .build();
        }
        return null;
    }

    @Override
    public GroupResponseEntity read(UUID id) {
        if(id!=null) {
            var group = groupRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Not Found group with this id"));
            return GroupResponseEntity.builder()
                    .groupId(group.getGroupId())
                    .groupName(group.getGroupName())
                    .groupCreator(group.getGroupCreator())
                    .groupCreationDate(group.getGroupCreationDate())
                    .userSet(group.getGroupHasUsers())
                    .build();
        }
        return null;
    }
    @Override
    public List<GroupResponseEntity> read() {
        List<Group> groups = groupRepo.findAll();
        if(groups!=null) {
            List<GroupResponseEntity> groupList = groups.stream()
                    .map(group -> new GroupResponseEntity(
                            group.getGroupId(),
                            group.getGroupName(),
                            group.getGroupCreator(),
                            group.getGroupCreationDate(),
                            group.getGroupHasUsers()))
                    .collect(Collectors.toList());
            return groupList;
        }
        return null;
    }
    @Override
    public GroupResponseEntity update(UUID id, GroupRequestEntity request) {
        if (id != null && request != null) {
            var group = groupRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Not Found group with this id"));
            group.setGroupName(request.getGroupName());
            group.getGroupHasUsers().addAll(userRepo.findAllById(request.getIdsSet()));
            var newGroup = groupRepo.save(group);
            var response = GroupResponseEntity.builder()
                    .groupId(newGroup.getGroupId())
                    .groupName(newGroup.getGroupName())
                    .groupCreator(newGroup.getGroupCreator())
                    .groupCreationDate(newGroup.getGroupCreationDate())
                    .build();
            response.getUserSet().addAll(newGroup.getGroupHasUsers());
            return response;
        }
        return null;
    }
    @Override
    public boolean delete(UUID id) {
        groupRepo.deleteById(id);
        return true;
    }

}
