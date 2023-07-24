package com.example.app.services;

import com.example.app.entities.*;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.example.app.models.responses.event.MyEventResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.UserMapper;
import com.example.app.utils.event.EventToMyEvents;
import com.example.app.utils.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
@RequiredArgsConstructor
public class UserService implements CrudService<Response, UserRequestEntity,UserNotFoundException>{
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final GroupRepository groupRepo;
    private final UserToOtherUser toOther;
    private final UserToMyHrManagerUser toMyHrManager;
    private final UserToAdminUser toAdmin;
    private final UserUpdateSetting updateUser;
    private final UserRequestEntityToUser toUser;
    private final EventToMyEvents converter;
    @Override
    public Response create(UserRequestEntity request, String token) throws UserNotFoundException {
            var userCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                    .orElseThrow(UserNotFoundException::new);
            var group = groupRepo.findById(request.getGroup()).orElse(null);
            var user =  userRepo.save(toUser.convertToEntity(
                    request,userCreator.getUserId(), group));
            return  toAdmin.convertToAdminUser(user);
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
        if(SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains("ROLE_ADMIN")) {
            return userMapper.convertToAdminResponse(user);
        }
        return userMapper.convertToResponse(user);
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
            var updatedUser = userMapper.updateSetting(user,request,
                    groupRepo.findById(request.getGroup()).orElse(null));
            var response = userRepo.save(updatedUser);
            return userMapper.convertToResponse(response);
    }
    @Override
    public boolean delete(UUID id) throws UserNotFoundException {
        if(id!=null){
            var user = userRepo.findById(id).orElseThrow(UserNotFoundException::new);
            user.getUserHasEvents().forEach((event)->{
                event.getUsersJoinInEvent().remove(user);
            });
            user.getUserHasEvents().clear();
            userRepo.save(user);
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

    //24/7
    public List<MyEventResponseEntity> readUserEvents(UUID userId)throws UserNotFoundException {
        var userEvents = userRepo.findById(userId).orElseThrow(UserNotFoundException::new).getUserHasEvents();
        List<MyEventResponseEntity> responseList = new ArrayList<>();
        userEvents.forEach((event) -> responseList.add(converter.eventToMyEvent(event)));
        return responseList;
    }
}