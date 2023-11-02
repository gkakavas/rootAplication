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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;
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
        var event = eventConverter.fromRequestToEvent(request,currentUser.getUserId());
        Set<User> usersToAdd = new HashSet<>();
        if(List.of(Role.ADMIN,Role.HR).contains(currentUser.getRole())){
            var group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
            usersToAdd = group.getGroupHasUsers();
        }
        else if (currentUser.getRole().equals(Role.MANAGER) && currentUser.getGroup().getGroupId().equals(groupId)) {
            usersToAdd = currentUser.getGroup().getGroupHasUsers();
        }
        event.getUsersJoinInEvent().addAll(usersToAdd);
        for(User user : usersToAdd){
            user.getUserHasEvents().add(event);
        }
        var newEvent = eventRepo.save(event);
        return eventConverter.fromEventToAdminHrMngEvent(newEvent);
    }
    public EventResponseEntity read(UUID id)
    throws EventNotFoundException{
            Event event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            return eventConverter.fromEventToAdminHrMngEvent(event);
    }

    public List<EventResponseEntity> read() {
        var events = Set.copyOf(eventRepo.findAll());
        return List.copyOf(eventConverter.fromEventListToAdminHrMngList(events));
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
            var event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            event.getUsersJoinInEvent().forEach((user)-> user.getUserHasEvents().remove(event));
            event.getUsersJoinInEvent().clear();
            eventRepo.save(event);
            eventRepo.delete(event);
            return eventRepo.existsById(event.getEventId());
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
