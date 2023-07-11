package com.example.app.services;

import com.example.app.entities.User;
import com.example.app.models.requests.RegisterRequest;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements CrudService<UserResponseEntity, UserRequestEntity>{
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final GroupRepository groupRepo;
    @Override
    public UserResponseEntity create(UserRequestEntity request, String token)  {
        if(request!=null){
            var user = User.builder()
                    .password("1234")
                    .firstname(request.getFirstname())
                    .lastname(request.getLastname())
                    .email(request.getEmail())
                    .specialization(request.getSpecialization())
                    .currentProject(request.getCurrentProject())
                    .createdBy(jwtService.extractUsername(token.substring(7)))
                    .registerDate(LocalDateTime.now())
                    .role(request.getRole())
                    .group(groupRepo.findById(request.getGroup()).orElse(null))
                    .build();
            var newUser =  userRepo.save(user);
            return UserResponseEntity.builder()
                    .userId(newUser.getUserId())
                    .firstname(newUser.getFirstname())
                    .lastname(newUser.getLastname())
                    .specialization(newUser.getSpecialization())
                    .email(newUser.getEmail())
                    .build();
        }
        return null;
    }

    public UserResponseEntity read(String email) {
        var user = userRepo.findByEmail(email).orElseThrow(()
                ->new IllegalArgumentException("Not found user with this email"));
        if(user!=null) {
            return UserResponseEntity.builder()
                    .userId(user.getUserId())
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .email(user.getEmail())
                    .specialization(user.getSpecialization())
                    .build();
        }
        return null;
    }
    @Override
    public UserResponseEntity read(UUID id) {
        var user = userRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Not found user with this id"));
        if(user!=null) {
            return UserResponseEntity.builder()
                    .userId(user.getUserId())
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .email(user.getEmail())
                    .specialization(user.getSpecialization())
                    .build();
        }
        return null;
    }
    @Override
    public List<UserResponseEntity> read() {
        List<User> users = userRepo.findAll();
        if(users!=null) {
            List<UserResponseEntity> userList = users.stream()
                    .map(user -> new UserResponseEntity(user.getUserId(),
                            user.getFirstname(),
                            user.getLastname(),
                            user.getEmail(),
                            user.getSpecialization(),
                            user.getUserHasEvents()))
                    .collect(Collectors.toList());
            return userList;
        }
        return null;
    }
    @Override
    public UserResponseEntity update(UUID id, UserRequestEntity request) {
        var user = userRepo.findById(id).orElseThrow(()
                ->new IllegalArgumentException("Not found user with this id"));
        if(user!=null && request!=null) {
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setSpecialization(request.getSpecialization());
            userRepo.save(user);
            return UserResponseEntity.builder()
                    .userId(id)
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .email(user.getEmail())
                    .specialization(user.getSpecialization())
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
}