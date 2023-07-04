package com.example.app.services;

import com.example.app.entities.User;
import com.example.app.models.*;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements CrudService<UserResponseEntity, UserRequestEntity>{
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final List<PersonalDetailsResponse> pDR;

    public PersonalAccountResponse retrievePersonalAccount(PersonalAccountRequest request) {
        var user = userRepo.findByEmail(jwtService.extractUsername(
                request.getToken().substring(7)));

        return  PersonalAccountResponse.builder()
                .firstname(user.get().getFirstname())
                .lastname(user.get().getLastname())
                .email(user.get().getEmail())
                .specialization(user.get().getSpecialization())
                .currentProject(user.get().getCurrentProject())
                .lastLogin(user.get().getLastLogin())
                .registerDate(user.get().getRegisterDate())
                .build();
    }

    public List<PersonalDetailsResponse> retrieveAllUsers() {
        List<User> users = userRepo.findAll();
        for (User user : users) {
            pDR.add(new PersonalDetailsResponse(user.getUserId(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getEmail()));
        }
        return pDR;
    }

    public UserProfileResponse retrieveUserProfile(Integer id) {
        try {
            var user = userRepo.findById(id);
            if (user.isPresent()) {
                var userProfResp = UserProfileResponse.builder()
                        .userId(user.get().getUserId())
                        .firstname(user.get().getFirstname())
                        .lastname(user.get().getLastname())
                        .specialization(user.get().getSpecialization())
                        .email(user.get().getEmail())
                        .build();
                return userProfResp;
            } else
                return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Integer deleteUser(Integer id) {
        try {
            var user = userRepo.findById(id);
            if (user.isPresent()) {
                userRepo.deleteById(id);
                return user.get().getUserId();
            } else
                return -1;
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            e.printStackTrace();
        }
        return null;
    }
    public UpdateUserResponse updateUser(Integer id, UpdateUserRequest request) {
        try {
            var userOptional = userRepo.findById(id);
            if (userOptional.isPresent()) {
                var user = userOptional.get();
                user.setFirstname(request.getFirstname());
                user.setLastname(request.getLastname());
                user.setCurrentProject(request.getCurrentProject());
                user.setEmail(request.getEmail());
                user.setSpecialization(request.getSpecialization());
                userRepo.save(user);
                return new UpdateUserResponse(user.getUserId(),
                        user.getFirstname(), user.getLastname(),
                        user.getEmail(), user.getSpecialization(), user.getCurrentProject());
            } else
                return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
    public UserProfileResponse retrieveUserProfile(String email) {
        try {
            var user = userRepo.findByEmail(email);
            if (user.isPresent()) {
                var userProfResp = UserProfileResponse.builder()
                        .userId(user.get().getUserId())
                        .firstname(user.get().getFirstname())
                        .lastname(user.get().getLastname())
                        .specialization(user.get().getSpecialization())
                        .email(user.get().getEmail())
                        .build();
                return userProfResp;
            } else
                return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
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
                        user.getSpecialization()))
                .collect(Collectors.toList());
        return userList;
    }
    @Override
    public UserResponseEntity update(UserRequestEntity request) {
        var oldUser = userRepo.findById(request.getUserId()).orElse(null);
        if(oldUser!=null && request!=null)
            var newUser = oldUser.setFirstname();
        return null;
    }

    @Override
    public void delete(UUID id) {

    }
}