package com.example.app.utils.user;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EntityResponseUserConverterImpl implements EntityResponseUserConverter{
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseEntity fromUserToAdminUser(User user) {
            var response =  AdminUserResponse.builder()
            .userId(user.getUserId())
            .firstname(user.getFirstname())
            .lastname(user.getLastname())
            .email(user.getEmail())
            .specialization(user.getSpecialization())
            .currentProject(user.getCurrentProject())
            .groupName(user.getGroup().getGroupName())
            .registerDate(user.getRegisterDate())
            .lastLogin(user.getLastLogin())
            .role(user.getRole())
                    .build();
            try {
                var createdBy = userRepo.findById(user.getCreatedBy()).orElseThrow().getEmail();
                response.setCratedBy(createdBy);
            }catch(NoSuchElementException e){
                response.setCratedBy(null);
                e.printStackTrace();
            }
            return response;
    }

    @Override
    public UserResponseEntity fromUserToOtherUser(User user) {
        return OtherUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(user.getGroup().getGroupName())
                .build();

    }

    @Override
    public List<UserResponseEntity> fromUserListToAdminList(List<User> users) {
            List<UserResponseEntity> responseList = new ArrayList<>();
            users.forEach((user) -> responseList.add(
                        fromUserToAdminUser(user)));
            return responseList;
    }

    @Override
    public List<UserResponseEntity> fromUserListToOtherList(List<User> users) {
        List<UserResponseEntity> responseList = new ArrayList<>();
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
                .registerDate(LocalDateTime.now())
                .lastLogin(null)
                .role(request.getRole())
                .group(userGroup)
                .userHasEvents(null)
                .userHasFiles(null)
                .build();
    }

    public User updateSetting(User user, UserRequestEntity request, Group group){
        user.setPassword(request.getPassword());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setSpecialization(request.getSpecialization());
        user.setCurrentProject(request.getCurrentProject());
        user.setRole(request.getRole());
        user.setGroup(group);
        return user;
    }
}
