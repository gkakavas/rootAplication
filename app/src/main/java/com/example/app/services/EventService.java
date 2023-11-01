package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.EventNotFoundException;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.event.EntityResponseEventConverter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepo;
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final EntityResponseEventConverter eventConverter;


    public EventResponseEntity create(EventRequestEntity request, Principal connectedUser)
    throws UserNotFoundException{
        var eventCreator = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
            var newEvent = eventConverter.fromRequestToEvent(request,eventCreator.getUserId());
            var users = userRepo.findAllById(request.getIdsSet());
            newEvent.getUsersJoinInEvent().addAll(users);
            for(User user:users){
                user.getUserHasEvents().add(newEvent);
            }
            var responseEvent = eventRepo.save(newEvent);
            return eventConverter.fromEventToAdminHrMngEvent(responseEvent);
    }

    public EventResponseEntity createForGroup(EventRequestEntity request, UUID groupId,Principal connectedUser)
            throws GroupNotFoundException{
        var currentUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(List.of(Role.ADMIN,Role.HR).contains(currentUser.getRole())){
            var group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
            var event = eventConverter.fromRequestToEvent(request,currentUser.getUserId());
            event.getUsersJoinInEvent().addAll(group.getGroupHasUsers());
            for(User user: group.getGroupHasUsers()){
                user.getUserHasEvents().add(event);
            }
            var newEvent = eventRepo.save(event);
            return eventConverter.fromEventToAdminHrMngEvent(newEvent);
        }
        else if (currentUser.getRole().equals(Role.MANAGER) && currentUser.getGroup().getGroupId().equals(groupId)) {
            var event = eventConverter.fromRequestToEvent(request,currentUser.getUserId());
            event.getUsersJoinInEvent().addAll(currentUser.getGroup().getGroupHasUsers());
            for(User user: currentUser.getGroup().getGroupHasUsers()){
                user.getUserHasEvents().add(event);
            }
            var newEvent = eventRepo.save(event);
            return eventConverter.fromEventToAdminHrMngEvent(newEvent);
        }
        else throw new AccessDeniedException("You have not authority to access this resource");
    }
    public EventResponseEntity read(UUID id)
    throws EventNotFoundException{
            Event event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            return eventConverter.fromEventToAdminHrMngEvent(event);
    }


    public List<EventResponseEntity> read(Principal connectedUser) {
        var currentUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(List.of(Role.ADMIN,Role.HR,Role.MANAGER).contains(currentUser.getRole())){
                var events = Set.copyOf(eventRepo.findAll());
                return List.copyOf(eventConverter.fromEventListToAdminHrMngList(events));
            } else if (currentUser.getRole().equals(Role.USER)) {
                return List.copyOf(eventConverter.fromEventListToMyList(currentUser.getUserHasEvents()));
            }
            else throw new AccessDeniedException("You have not authority to access this resource");
    }

    public EventResponseEntity update(UUID id, EventRequestEntity request)
    throws EventNotFoundException{
            var event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            var updatedEvent = eventConverter.eventUpdate(request,event);
            var newEvent = eventRepo.save(updatedEvent);
            return eventConverter.fromEventToAdminHrMngEvent(newEvent);
    }

    public boolean delete(UUID id)
    throws EventNotFoundException{
        if(id!=null){
            var event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            event.getUsersJoinInEvent().forEach((user)-> user.getUserHasEvents().remove(event));
            event.getUsersJoinInEvent().clear();
            eventRepo.save(event);
            eventRepo.deleteById(id);
            return true;
        }
        else{
            throw new EventNotFoundException();
        }
    }

    public EventResponseEntity addUsersToEvent(Set<UUID> idsSet, UUID eventId) throws EventNotFoundException{
        var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
        var users = userRepo.findAllById(idsSet);
        for(User user:users){
            event.getUsersJoinInEvent().add(user);
            user.getUserHasEvents().add(event);
        }
        var updatedEvent = eventRepo.save(event);
        return eventConverter.fromEventToAdminHrMngEvent(updatedEvent);
    }

    public EventResponseEntity removeUsersFromEvent(Set<UUID> idsSet, UUID eventId)throws EventNotFoundException{
        var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
        var users = userRepo.findAllById(idsSet);
        for(User user:users){
            event.getUsersJoinInEvent().remove(user);
            user.getUserHasEvents().remove(event);
        }
        var updatedEvent = eventRepo.save(event);
        return eventConverter.fromEventToAdminHrMngEvent(updatedEvent);
    }
     public EventResponseEntity patchEventDetails(UUID eventId, Map<String,String> eventFields)
     throws EventNotFoundException {
         if (!eventFields.isEmpty()) {
             var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
             eventFields.forEach((key, value) -> {
                 Field field = ReflectionUtils.findField(Event.class, key);
                 assert field != null;
                 field.setAccessible(true);
                 if(field.getType().equals(LocalDateTime.class)){
                   var parsedValue =  LocalDateTime.parse(value);
                   ReflectionUtils.setField(field, event, parsedValue);
                 }
                 else if (field.getType().equals(String.class)) {
                     ReflectionUtils.setField(field, event, value);
                 }
             }
             );
             var patcedEvent = eventRepo.save(event);
             return eventConverter.fromEventToAdminHrMngEvent(patcedEvent);
         }
         return null;
     }
}
