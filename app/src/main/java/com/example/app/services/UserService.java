package com.example.app.services;

import com.example.app.entities.*;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
@RequiredArgsConstructor
public class UserService implements CrudService<UserResponseEntity, UserRequestEntity,UserNotFoundException>{
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final GroupRepository groupRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    @Override
    public UserResponseEntity create(UserRequestEntity request, String token) throws UserNotFoundException {
        if(request!=null){
            var userCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                    .orElseThrow(UserNotFoundException::new);
            var group = groupRepo.findById(request.getGroup()).orElse(null);
            var user =  userRepo.save(userMapper.convertToEntity(
                    request,userCreator.getUserId(), group));
            return userMapper.convertToResponse(user);
        }
        return null;
    }

    public UserResponseEntity read(String email) throws UserNotFoundException {
        var user = userRepo.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if(user!=null) {
            return userMapper.convertToResponse(user);
        }
        return null;
    }
    @Override
    public UserResponseEntity read(UUID id) throws UserNotFoundException {
        var user = userRepo.findById(id).orElseThrow(UserNotFoundException::new);
        if(user!=null) {
            return userMapper.convertToResponse(user);
        }
        return null;
    }
    @Override
    public List<UserResponseEntity> read() {
        List<User> users = userRepo.findAll();
            List<UserResponseEntity> userList = new ArrayList<>();
            for(User user:users){
                userList.add(userMapper.convertToResponse(user));
            }
            return userList;
    }
    @Override
    public UserResponseEntity update(UUID id, UserRequestEntity request) throws UserNotFoundException {
        var user = userRepo.findById(id).orElseThrow(UserNotFoundException::new);
            user.setPassword(encoder.encode(request.getPassword()));
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setSpecialization(request.getSpecialization());
            user.setCurrentProject(request.getCurrentProject());
            user.setRole(request.getRole());
            var response = userRepo.save(user);
            return userMapper.convertToResponse(response);
    }
    @Override
    public boolean delete(UUID id) throws UserNotFoundException {
        if(id!=null){
            userRepo.deleteById(id);
            return true;
        }
        else{
            throw new UserNotFoundException();
        }
    }

    public UserResponseEntity patch(UUID userId, Map<String, String> userField) throws UserNotFoundException {
        var user = userRepo.findById(userId).orElseThrow(UserNotFoundException::new);
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
            var patcedUser = userRepo.save(user);
        return userMapper.convertToResponse(patcedUser);
    }
}