package com.example.app.services;

import com.example.app.entities.*;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.event.EntityResponseEventConverter;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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
        var roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if(roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_ADMIN"))) {
            return userConverter.fromUserToAdminUser(user);
        }
        else if(roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_USER"))||
                roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_HR"))||
                roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_MANAGER"))){
            return userConverter.fromUserToOtherUser(user);
        }
        else
            throw new AccessDeniedException("Unauthorized request");
    }
    @Override
    public List<UserResponseEntity> read() {
        Set<User> users = Set.copyOf(userRepo.findAll());
        var roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if(roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_ADMIN"))) {
            return List.copyOf(userConverter.fromUserListToAdminList(users));
        }
        else if(roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_USER"))||
                roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_HR"))||
                roles.stream().anyMatch(a->a.getAuthority().contains("ROLE_MANAGER"))){

            return List.copyOf(userConverter.fromUserListToOtherList(users));
        }
        else
            throw new AccessDeniedException("Unauthorized request");
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

    public UserResponseEntity patch(UUID userId, Map<String, String> userFields) throws UserNotFoundException {
        if (!userFields.isEmpty()) {
            var user = userRepo.findById(userId).orElseThrow(UserNotFoundException::new);
            userFields.forEach((key, value) -> {
                        Field field = ReflectionUtils.findField(User.class, key);
                        assert field != null;
                        field.setAccessible(true);
                        ReflectionUtils.setField(field, user, value);
                    }
            );
            var patcedUser = userRepo.save(user);
            return userConverter.fromUserToAdminUser(patcedUser);
        }
        else
            throw new NullPointerException("");

    }

    public Set<EventResponseEntity> readUserEvents(UUID userId)throws UserNotFoundException {
        var user = userRepo.findById(userId).orElseThrow(UserNotFoundException::new);
        return eventConverter.fromEventListToMyList(user.getUserHasEvents());
    }
}