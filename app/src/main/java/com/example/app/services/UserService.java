package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.*;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements CrudService<UserResponseEntity, UserRequestEntity>{
    private final UserRepository userRepo;
    private final AuthenticationService authService;
    private final EventRepository eventRepo;
    @Override
    public UserResponseEntity create(UserRequestEntity request) {
        if(request!=null)
        authService.register(RegisterRequest.builder()
                        .firstname(request.getFirstname())
                        .lastname(request.getLastname())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .currentProject(request.getEmail())
                        .role(request.getRole())
                        .specialization(request.getSpecialization())
                        .build());
        return null;
    }

    public UserResponseEntity read(String email) {
        var user = userRepo.findByEmail(email).orElseThrow(()
                ->new IllegalArgumentException("Not found user with this id"));
        return UserResponseEntity.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .build();
    }
    @Override
    public UserResponseEntity read(UUID id) {
        var user = userRepo.findById(id).orElse(null);
        return UserResponseEntity.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .build();
    }
    @Override
    public List<UserResponseEntity> read() {
        List<User> users = userRepo.findAll();
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
        }
        return UserResponseEntity.builder()
                .userId(id)
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .build();
    }
    @Override
    public boolean delete(UUID id) {
        if(id!=null){
            userRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<UserResponseEntity> addEventsToUser( UUID eventId,Iterable<UUID> userIds) {
        try {
            List<User> users = userRepo.findAllById(userIds);
            Event event = eventRepo.findById(eventId).orElseThrow(() -> new IllegalArgumentException());
            List<UserResponseEntity> UREList = new ArrayList<>();
            for(User user:users){
            user.getUserHasEvents().add(event);
            userRepo.save(user);
            UREList.add(new UserResponseEntity(
                    user.getUserId(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getEmail(),
                    user.getSpecialization(),
                    user.getUserHasEvents()
            ));
            }
            return UREList;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}