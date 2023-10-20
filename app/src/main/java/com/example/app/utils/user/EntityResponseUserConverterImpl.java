package com.example.app.utils.user;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Validator;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
public class EntityResponseUserConverterImpl implements EntityResponseUserConverter{

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminUserResponse fromUserToAdminUser(User user) {
        var response = AdminUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(null)
                .registerDate(user.getRegisterDate())
                .createdBy(null)
                .lastLogin(user.getLastLogin())
                .role(user.getRole())
                .build();
            if(user.getGroup()!=null){
                response.setGroupName(user.getGroup().getGroupName());
            }
            if(user.getCreatedBy()!=null) {
                userRepo.findById(user.getCreatedBy()).ifPresent(
                        value -> response.setCreatedBy(value.getEmail()));
            }
            return response;
    }

    @Override
    public OtherUserResponse fromUserToOtherUser(User user) {
        var response=  OtherUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(null)
                .build();
        if(user.getGroup()!=null){
            response.setGroupName(user.getGroup().getGroupName());
        }
       return response;
    }

    @Override
    public Set<AdminUserResponse> fromUserListToAdminList(Set<User> users) {
            Set<AdminUserResponse> responseList = new HashSet<>();
            users.forEach((user) -> responseList.add(
                        fromUserToAdminUser(user)));
            return responseList;
    }

    @Override
    public Set<OtherUserResponse> fromUserListToOtherList(Set<User> users) {
        Set<OtherUserResponse> responseList = new HashSet<>();
        users.forEach((user)->responseList.add(
                fromUserToOtherUser(user)));
        return responseList;
    }
    @Override
    public User fromRequestToEntity(UserRequestEntity request, UUID userCreator, Group userGroup) {
        return User.builder()
                .password(passwordEncoder.encode("Cdb3zgy2"))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .specialization(request.getSpecialization())
                .currentProject(request.getCurrentProject())
                .createdBy(userCreator)
                .registerDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .lastLogin(null)
                .roleValue(request.getRole())
                .role(Role.valueOf(request.getRole()))
                .group(userGroup)
                .userHasEvents(null)
                .userHasFiles(null)
                .userRequestedLeaves(null)
                .build();
    }

    @Override
    public User updateSetting(User user, UserRequestEntity request, Group group){
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setSpecialization(request.getSpecialization());
        user.setCurrentProject(request.getCurrentProject());
        user.setRole(Role.valueOf(request.getRole()));
        user.setGroup(group);
        return user;
    }
}
