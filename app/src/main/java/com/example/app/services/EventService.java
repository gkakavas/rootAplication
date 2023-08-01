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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService implements CrudService<EventResponseEntity, EventRequestEntity, EventNotFoundException> {
    private final EventRepository eventRepo;
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final EntityResponseEventConverter eventConverter;
    @Override
    public EventResponseEntity create(EventRequestEntity request, String token)
    throws UserNotFoundException{
        var eventCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                .orElseThrow(UserNotFoundException::new);
            var newEvent = eventConverter.fromRequestToEvent(request,eventCreator.getUserId());
            var users = userRepo.findAllById(request.getIdsSet());
            newEvent.getUsersJoinInEvent().addAll(users);
            var responseEvent = eventRepo.save(newEvent);
            for(User user:users){
                user.getUserHasEvents().add(responseEvent);
                userRepo.save(user);
            }
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
            if(currentUser.getRole().equals(Role.ROLE_ADMIN)||currentUser.getRole().equals(Role.ROLE_MANAGER)
                    ||currentUser.getRole().equals(Role.ROLE_HR)){
                var events = Set.copyOf(eventRepo.findAll());
                return List.copyOf(eventConverter.fromEventListToAdminHrMngList(events));
            } else if (currentUser.getRole().equals(Role.ROLE_USER)) {
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

        if(currentUser.getRole().equals(Role.ROLE_ADMIN)||currentUser.getRole().equals(Role.ROLE_HR)){
            var eventCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                    .orElseThrow(UserNotFoundException::new);
            var group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
            var event = eventConverter.fromRequestToEvent(request,eventCreator.getUserId());
            event.getUsersJoinInEvent().addAll(group.getGroupHasUsers());
            var newEvent = eventRepo.save(event);
            return eventConverter.fromEventToAdminHrMngEvent(newEvent);
        }
        else if (currentUser.getRole().equals(Role.ROLE_MANAGER)) {
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
        var updatedEvent =  eventRepo.save(event);
        return eventConverter.fromEventToAdminHrMngEvent(updatedEvent);
    }

    public EventResponseEntity removeUserFromEvent(Set<UUID> idsSet, UUID eventId)throws EventNotFoundException{
        var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
        var users = userRepo.findAllById(idsSet);
        users.forEach((user)-> event.getUsersJoinInEvent().remove(user));
        var updatedEvent = eventRepo.save(event);
        return eventConverter.fromEventToAdminHrMngEvent(updatedEvent);
    }
     public EventResponseEntity patch(UUID eventId, Map<String,Object> eventFields)
     throws EventNotFoundException {
         if (!eventFields.isEmpty()) {
             var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
             eventFields.forEach((key, value) -> {
                         Field field = ReflectionUtils.findField(EventRequestEntity.class, key);
                         assert field != null;
                         field.setAccessible(true);
                         ReflectionUtils.setField(field, event, value);
                     }
             );
             var patcedEvent = eventRepo.save(event);
             return eventConverter.fromEventToAdminHrMngEvent(patcedEvent);
         }
         return null;
     }
}
