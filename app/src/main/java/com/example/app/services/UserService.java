package com.example.app.services;

import com.example.app.entities.*;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.event.EntityResponseEventConverter;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.*;
@Service
@RequiredArgsConstructor
public class UserService implements CrudService<UserResponseEntity, UserRequestEntity,UserNotFoundException>{
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final GroupRepository groupRepo;
    private final EntityResponseUserConverter userConverter;
    private final EntityResponseEventConverter eventConverter;
    @Override
    public UserResponseEntity create(UserRequestEntity request, String token) throws UserNotFoundException {
            var userCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                    .orElseThrow(UserNotFoundException::new);
            var group = groupRepo.findById(request.getGroup()).orElse(null);
            var user =  userRepo.save(userConverter.fromRequestToEntity(
                    request,userCreator.getUserId(), group));
            return userConverter.fromUserToAdminUser(user);
    }

    public User read(String email) throws UserNotFoundException {
        return userRepo.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }
    @Override
    public UserResponseEntity read(UUID id) throws UserNotFoundException {
        var user = userRepo.findById(id).orElseThrow(UserNotFoundException::new);
        var currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(currentUserEmail).orElseThrow(UserNotFoundException::new);
        if(currentUser.getRole().equals(Role.ADMIN)) {
            return userConverter.fromUserToAdminUser(user);
        }
        else if(currentUser.getRole().equals(Role.USER)|| currentUser.getRole().equals(Role.HR)
                ||currentUser.getRole().equals(Role.MANAGER)){
            return userConverter.fromUserToOtherUser(user);
        }
        else throw new AccessDeniedException("Unauthorized request");
    }
    @Override
    public List<UserResponseEntity> read() {
        Set<User> users = Set.copyOf(userRepo.findAll());
        var currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            User currentUser = userRepo.findByEmail(currentUserEmail).orElseThrow(UserNotFoundException::new);
            if (currentUser.getRole().name().equals("ADMIN")) {
                return List.copyOf(userConverter.fromUserListToAdminList(users));
            }
            else if (currentUser.getRole().name().equals("USER")||currentUser.getRole().name().equals("HR")
                    ||currentUser.getRole().name().equals("MANAGER")) {
                return List.copyOf(userConverter.fromUserListToOtherList(users));
            } else
                throw new AccessDeniedException("Unauthorized request");
        }catch (UserNotFoundException e){
            throw new AccessDeniedException("Unauthorized request");
        }
    }
    @Override
    public UserResponseEntity update(UUID id, UserRequestEntity request) throws UserNotFoundException {
        var user = userRepo.findById(id).orElseThrow(UserNotFoundException::new);
            var updatedUser = userConverter.updateSetting(user,request,
                    groupRepo.findById(request.getGroup()).orElse(null));
            var response = userRepo.save(updatedUser);
            return userConverter.fromUserToAdminUser(response);
    }
    @Override
    public boolean delete(UUID id) throws UserNotFoundException {
        if(id!=null){
            var user = userRepo.findById(id).orElseThrow(UserNotFoundException::new);
            user.getUserHasEvents().forEach((event)-> event.getUsersJoinInEvent().remove(user));
            user.getUserHasEvents().clear();
            userRepo.save(user);
            userRepo.deleteById(id);
         return true;
        }
        else{
            throw new UserNotFoundException();
        }
    }

    public UserResponseEntity patch(UUID userId, Map<String, String> userFields) throws UserNotFoundException, GroupNotFoundException {
            var user = userRepo.findById(userId).orElseThrow(UserNotFoundException::new);
        for (Map.Entry<String, String> entry : userFields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Field field = ReflectionUtils.findField(User.class, key);
            assert field != null;
            field.setAccessible(true);
            if (key.equals("group")) {
                var group = groupRepo.findById(UUID.fromString(userFields.get("group"))).orElseThrow(GroupNotFoundException::new);
                group.getGroupHasUsers().add(user);
                user.setGroup(group);
                groupRepo.save(group);
            }
            else if (key.equals("role")) {
                ReflectionUtils.setField(field, user, Role.valueOf(value));
            }
            else {
                ReflectionUtils.setField(field, user, value);
            }
        }
        userRepo.save(user);
        var patchedUser = userRepo.findById(user.getUserId()).orElseThrow(UserNotFoundException::new);
        return userConverter.fromUserToAdminUser(patchedUser);
    }

    public Set<EventResponseEntity> readUserEvents(UUID userId)throws UserNotFoundException {
        var user = userRepo.findById(userId).orElseThrow(UserNotFoundException::new);
        return eventConverter.fromEventListToMyList(user.getUserHasEvents());
    }
}