package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.EventNotFoundException;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.EventResponseEntity;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.EventMapper;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService implements CrudService<EventResponseEntity, EventRequestEntity, EventNotFoundException> {
    private final EventRepository eventRepo;
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final EventMapper eventMapper;
    @Override
    public EventResponseEntity create(EventRequestEntity request, String token)
    throws UserNotFoundException{
        var eventCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                .orElseThrow(UserNotFoundException::new);
            var newEvent = eventMapper.convertToEvent(request,eventCreator.getUserId());
            var users = userRepo.findAllById(request.getIdsSet());
            newEvent.getUsersJoinInEvent().addAll(users);
            var event = eventRepo.save(newEvent);
            for(User user:users){
                user.getUserHasEvents().add(event);
                userRepo.save(user);
            }
            return eventMapper.convertToResponse(event);
    }
    @Override
    public EventResponseEntity read(UUID id)
    throws EventNotFoundException{
        if (id != null) {
            Event event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            if(event!=null) {
                return eventMapper.convertToResponse(event);
            }
        }
        return null;
    }

    @Override
    public List<EventResponseEntity> read() {
        List<Event> events = eventRepo.findAll();
            List<EventResponseEntity> eventList = new ArrayList<>();
            for (Event event : events) {
                eventList.add(eventMapper.convertToResponse(event));
            }
        return eventList;
    }
    @Override
    public EventResponseEntity update(UUID id, EventRequestEntity request)
    throws EventNotFoundException{
        if(id!=null) {
            var event = eventRepo.findById(id).orElseThrow(EventNotFoundException::new);
            event.setEventDescription(request.getEventDescription());
            event.setEventBody(request.getEventBody());
            event.setEventExpiration(request.getEventExpiration());
            event.setEventDateTime(request.getEventDateTime());
            var newEvent = eventRepo.save(event);
            return eventMapper.convertToResponse(newEvent);
        }
       return null;
    }
    @Override
    public boolean delete(UUID id)
    throws EventNotFoundException{
        if(id!=null){
            eventRepo.deleteById(id);
            return true;
        }
        else{
            throw new EventNotFoundException();
        }
    }

    public EventResponseEntity createForGroup(EventRequestEntity request, String token, UUID groupId)
            throws GroupNotFoundException, UserNotFoundException {
        if (groupId != null && request != null) {
            var eventCreator = userRepo.findByEmail(jwtService.extractUsername(token.substring(7)))
                    .orElseThrow(UserNotFoundException::new);
            var group = groupRepo.findById(groupId).orElseThrow(GroupNotFoundException::new);
            Set<User> userSet = new HashSet<>(userRepo.findAllByGroup(group));
            Event event = eventMapper.convertToEvent(request,eventCreator.getUserId());
            event.getUsersJoinInEvent().addAll(userSet);
           var newEvent = eventRepo.save(event);
           for(User user:userSet){
               user.getUserHasEvents().add(newEvent);
               userRepo.save(user);
           }
            return eventMapper.convertToResponse(newEvent);
        }
        return null;
    }

    public EventResponseEntity addUserToEvent(Set<UUID> idsSet, UUID eventId) throws EventNotFoundException{
        var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
        var users = userRepo.findAllById(idsSet);
        users.forEach((user)->{
            event.getUsersJoinInEvent().add(user);
            user.getUserHasEvents().add(event);
        });
        var updatedEvent =  eventRepo.save(event);
        return eventMapper.convertToResponse(updatedEvent);
    }

    public EventResponseEntity removeUserFromEvent(Set<UUID> idsSet, UUID eventId)throws EventNotFoundException{
        var event = eventRepo.findById(eventId).orElseThrow(EventNotFoundException::new);
        var users = userRepo.findAllById(idsSet);
        users.forEach((user)->{
            event.getUsersJoinInEvent().remove(user);
            user.getUserHasEvents().remove(event);
        });
        var updatedEvent = eventRepo.save(event);
        return eventMapper.convertToResponse(updatedEvent);
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
             return eventMapper.convertToResponse(patcedEvent);
         }
         return null;
     }
}
