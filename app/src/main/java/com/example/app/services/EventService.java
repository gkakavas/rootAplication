package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.EventResponseEntity;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.EventMapper;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService implements CrudService<EventResponseEntity, EventRequestEntity> {
    private final EventRepository eventRepo;
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final EventMapper eventMapper;
    @Override
    public EventResponseEntity create(EventRequestEntity request, String token) {
        if(request!=null&&token!=null) {
            var newEvent = eventMapper.convertToEvent(request,jwtService.extractUsername(token.substring(7)));
            var users = userRepo.findAllById(request.getIdsSet());
            if(request.getIdsSet()!=null) {
                newEvent.getUsersJoinInEvent().addAll(users);
            }
            var event = eventRepo.save(newEvent);
            for(User user:users){
                user.getUserHasEvents().add(event);
                userRepo.save(user);
            }
            return eventMapper.convertToResponse(event);
        }
        return null;
    }
    @Override
    public EventResponseEntity read(UUID id) {
        if (id != null) {
            Event event = eventRepo.findById(id).orElseThrow(()
                    -> new IllegalArgumentException("Not found event with this id"));
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
    public EventResponseEntity update(UUID id, EventRequestEntity request) {
        if(id!=null) {
            var event = eventRepo.findById(id).orElseThrow(()
                    -> new IllegalArgumentException("Not found event with this id"));
            if (event != null && request != null) {
                eventMapper.convertToEvent(request,event.getEventCreator());
                var updatedEvent = eventRepo.save(event);
                return eventMapper.convertToResponse(updatedEvent);
            }
        }
       return null;
    }
    @Override
    public boolean delete(UUID id) {
        if(id!=null){
            eventRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public EventResponseEntity createForGroup(EventRequestEntity request, String token, UUID groupId) {
        if (groupId != null && request != null) {
            var group = groupRepo.findById(groupId).orElseThrow(()->new IllegalArgumentException(""));
            Set<User> userSet = new HashSet<>(userRepo.findAllByGroup(group));
            Event event = eventMapper.convertToEvent(request,token);
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
}
