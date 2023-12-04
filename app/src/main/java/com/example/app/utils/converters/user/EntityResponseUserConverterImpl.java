package com.example.app.utils.converters.user;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.CurrentUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EntityResponseUserConverterImpl implements EntityResponseUserConverter{

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    @Value("${default.password}")
    private String defaultPasswordForUserCreation;


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
                .registerDate(user.getRegisterDate().truncatedTo(ChronoUnit.SECONDS))
                .createdBy(null)
                .lastLogin(null)
                .role(user.getRole())
                .build();
            if(user.getGroup()!=null){
                response.setGroupName(user.getGroup().getGroupName());
            }
            if(user.getLastLogin()!= null){
                response.setLastLogin(user.getLastLogin().truncatedTo(ChronoUnit.SECONDS));
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
            for(User user:users){
                responseList.add(fromUserToAdminUser(user));
            }
            return responseList;
    }

    @Override
    public Set<OtherUserResponse> fromUserListToOtherList(Set<User> users) {
        Set<OtherUserResponse> responseList = new HashSet<>();
        for(User user:users){
            responseList.add(fromUserToOtherUser(user));
        }
        return responseList;
    }
    @Override
    public User fromRequestToEntity(UserRequestEntity request, UUID userCreator, Group userGroup) {
        return User.builder()
                .password(passwordEncoder.encode(defaultPasswordForUserCreation))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .specialization(request.getSpecialization())
                .currentProject(request.getCurrentProject())
                .createdBy(userCreator)
                .registerDate(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS))
                .roleValue(request.getRole())
                .role(Role.valueOf(request.getRole()))
                .group(userGroup)
                .build();
    }

    @Override
    public User updateSetting(User user, UserRequestEntity request, Group group){
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setSpecialization(request.getSpecialization());
        user.setCurrentProject(request.getCurrentProject());
        user.setRole(Role.valueOf(request.getRole()));
        user.setGroup(group);
        return user;
    }

    @Override
    public CurrentUserResponse fromUserToCurrentUser(User user) {
        var response = CurrentUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(null)
                .role(user.getRole())
                .authorities(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .build();
        if(user.getGroup()!=null){
            response.setGroupName(user.getGroup().getGroupName());
        }
        return response;
    }
}
