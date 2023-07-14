package com.example.app.services;

import com.example.app.entities.*;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
@Service
@RequiredArgsConstructor
public class UserService implements CrudService<UserResponseEntity, UserRequestEntity>{
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final GroupRepository groupRepo;
    private final EventRepository eventRepo;
    private final FileRepository fileRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    @Override
    public UserResponseEntity create(UserRequestEntity request, String token, UUID groupId)  {
        if(request!=null){
            var userCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)));
            var group = groupRepo.findById(groupId).orElse(null);
            var user =  userRepo.save(userMapper.convertToEntity(
                    request,userCreator.get().getUserId(), group));
            return userMapper.convertToResponse(user);
        }
        return null;
    }

    public UserResponseEntity read(String email) {
        var user = userRepo.findByEmail(email).orElseThrow(()
                ->new IllegalArgumentException("Not found user with this email"));
        if(user!=null) {
            return userMapper.convertToResponse(user);
        }
        return null;
    }
    @Override
    public UserResponseEntity read(UUID id) {
        var user = userRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Not found user with this id"));
        if(user!=null) {
            return userMapper.convertToResponse(user);
        }
        return null;
    }
    @Override
    public List<UserResponseEntity> read() {
        List<User> users = userRepo.findAll();
        if(users!=null) {
            List<UserResponseEntity> userList = new ArrayList<>();
            for(User user:users){
                userList.add(userMapper.convertToResponse(user));
            }
            return userList;
        }
        return null;
    }
    @Override
    public UserResponseEntity update(UUID id, UserRequestEntity request) {
        var user = userRepo.findById(id).orElseThrow(()
                ->new IllegalArgumentException("Not found user with this id"));
        if(user!=null && request!=null) {
            userMapper.convertToEntity(request,user.)
            var updatedUser = userRepo.save(user);
            return UserResponseEntity.builder()
                    .userId(updatedUser.getUserId())
                    .firstname(updatedUser.getFirstname())
                    .lastname(updatedUser.getLastname())
                    .email(updatedUser.getEmail())
                    .specialization(updatedUser.getSpecialization())
                    .currentProject(updatedUser.getCurrentProject())
                    .createdBy(updatedUser.getCreatedBy())
                    .group(updatedUser.getGroup())
                    .files(updatedUser.getUserHasFiles())
                    .events(updatedUser.getUserHasEvents())
                    .build();
        }
        return null;
    }
    @Override
    public boolean delete(UUID id) {
        if(id!=null){
            userRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public UserResponseEntity patch(UUID userId, Map<String, String> userField) {
        var user = userRepo.findById(userId).orElseThrow(()-> new IllegalArgumentException(""));
        userField.keySet().forEach((key)-> {
                    switch (key) {
                        //case password:
                        case "firstname" -> user.setFirstname(userField.get(key));
                        case "lastname" -> user.setLastname(userField.get(key));
                        case "email" -> user.setEmail(userField.get(key));
                        case "specialization" -> user.setSpecialization(userField.get(key));
                        case "currentProject" -> user.setCurrentProject(userField.get(key));
                        case "role" -> user.setRole(Role.valueOf(userField.get(key)));
                        case "group" -> user.setGroup(groupRepo.findById(UUID.fromString(userField.get(key)))
                                        .orElseThrow(() -> new IllegalArgumentException("")));
                    }
                }
                );
            var newUser = userRepo.save(user);
        return UserResponseEntity.builder()
                .userId(newUser.getUserId())
                .firstname(newUser.getFirstname())
                .lastname(newUser.getLastname())
                .email(newUser.getEmail())
                .specialization(newUser.getSpecialization())
                .currentProject(newUser.getCurrentProject())
                .role(newUser.getRole())
                .group(newUser.getGroup())
                .build();
    }
}