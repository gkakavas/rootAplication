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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService implements CrudService<EventResponseEntity, EventRequestEntity, EventNotFoundException> {
    private final EventRepository eventRepo;
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final EntityResponseEventConverter eventConverter;
    private Object key;

    @Override
    public EventResponseEntity create(EventRequestEntity request, String token)
    throws UserNotFoundException{
        var eventCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                .orElseThrow(UserNotFoundException::new);
            var newEvent = eventConverter.fromRequestToEvent(request,eventCreator.getUserId());
            var users = userRepo.findAllById(request.getIdsSet());
            newEvent.getUsersJoinInEvent().addAll(users);
            for(User user:users){
                user.getUserHasEvents().add(newEvent);
            }
            var responseEvent = eventRepo.save(newEvent);
            return eventConverter.fromEventToAdminHrMngEvent(responseEvent);
    }
    @Override
    public EventResponseEntity read(UUID id)
    throws EventNotFoundException{
            Event event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            return eventConverter.fromEventToAdminHrMngEvent(event);

    }

    @Override
    public List<EventResponseEntity> read() {
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()
                -> new AccessDeniedException("You have not authority to access this resource"));
            if(currentUser.getRole().equals(Role.ADMIN)||currentUser.getRole().equals(Role.MANAGER)
                    ||currentUser.getRole().equals(Role.HR)){
                var events = Set.copyOf(eventRepo.findAll());
                return List.copyOf(eventConverter.fromEventListToAdminHrMngList(events));
            } else if (currentUser.getRole().equals(Role.USER)) {
                return List.copyOf(eventConverter.fromEventListToMyList(currentUser.getUserHasEvents()));
            }
            else throw new AccessDeniedException("You have not authority to access this resource");
    }
    @Override
    public EventResponseEntity update(UUID id, EventRequestEntity request)
    throws EventNotFoundException{
            var event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            var updatedEvent = eventConverter.eventUpdate(request,event);
            var newEvent = eventRepo.save(updatedEvent);
            return eventConverter.fromEventToAdminHrMngEvent(newEvent);
    }
    @Override
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

    public EventResponseEntity createForGroup(EventRequestEntity request, String token, UUID groupId)
            throws GroupNotFoundException, UserNotFoundException {

        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()
                -> new AccessDeniedException("You have not authority to access this resource"));

        if(currentUser.getRole().equals(Role.ADMIN)||currentUser.getRole().equals(Role.HR)){
            var eventCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                    .orElseThrow(UserNotFoundException::new);
            var group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
            var event = eventConverter.fromRequestToEvent(request,eventCreator.getUserId());
            event.getUsersJoinInEvent().addAll(group.getGroupHasUsers());
            var newEvent = eventRepo.save(event);
            return eventConverter.fromEventToAdminHrMngEvent(newEvent);
        }
        else if (currentUser.getRole().equals(Role.MANAGER)) {
            var eventCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                    .orElseThrow(UserNotFoundException::new);
            var event = eventConverter.fromRequestToEvent(request,eventCreator.getUserId());
            event.getUsersJoinInEvent().addAll(currentUser.getGroup().getGroupHasUsers());
            var newEvent = eventRepo.save(event);
            return eventConverter.fromEventToAdminHrMngEvent(newEvent);
        }
        else throw new AccessDeniedException("You have not authority to access this resource");
    }

    public EventResponseEntity addUserToEvent(Set<UUID> idsSet, UUID eventId) throws EventNotFoundException{
        var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
        var users = userRepo.findAllById(idsSet);
        users.forEach((user)-> event.getUsersJoinInEvent().add(user));
        for(User user:users){
            user.getUserHasEvents().add(event);
        }
        var updatedEvent =  eventRepo.save(event);
        return eventConverter.fromEventToAdminHrMngEvent(updatedEvent);
    }

    public EventResponseEntity removeUserFromEvent(Set<UUID> idsSet, UUID eventId)throws EventNotFoundException{
        var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
        var users = userRepo.findAllById(idsSet);
        users.forEach((user)-> event.getUsersJoinInEvent().remove(user));
        for(User user:users){
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
