package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.EventResponseEntity;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService implements CrudService<EventResponseEntity, EventRequestEntity> {
    private final EventRepository eventRepo;
    private final JwtService jwtService;
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    @Override
    public EventResponseEntity create(EventRequestEntity request, String creatorEmail) {
        if(request!=null&&creatorEmail!=null) {
            var newEvent = Event.builder()
                    .eventCreator(request.getEventCreator())
                    .eventDescription(request.getEventDescription())
                    .eventBody(request.getEventBody())
                    .eventDateTime(request.getEventDateTime())
                    .eventExpiration(request.getEventExpiration())
                    .build();
            var event = eventRepo.save(newEvent);
            return EventResponseEntity.builder()
                    .eventId(event.getEventId())
                    .eventCreator(event.getEventCreator())
                    .eventDescription(event.getEventDescription())
                    .eventBody(event.getEventBody())
                    .eventDateTime(event.getEventDateTime())
                    .eventExpiration(event.getEventExpiration())
                    .build();
        }
        return null;
    }
    @Override
    public EventResponseEntity read(UUID id) {
        if (id != null) {
            Event event = eventRepo.findById(id).orElseThrow(()
                    -> new IllegalArgumentException("Not found event with this id"));
            if(event!=null) {
                return EventResponseEntity.builder()
                        .eventId(event.getEventId())
                        .eventCreator(event.getEventCreator())
                        .eventDescription(event.getEventDescription())
                        .eventBody(event.getEventBody())
                        .eventDateTime(event.getEventDateTime())
                        .eventExpiration(event.getEventExpiration())
                        .userSet(event.getUsersJoinInEvent())
                        .build();
            }
        }
        return null;
    }

    @Override
    public List<EventResponseEntity> read() {
        List<Event> events = eventRepo.findAll();
        if(events!=null) {
            List<EventResponseEntity> eventList = new ArrayList<>();
            for (Event event : events) {
                eventList.add(new EventResponseEntity(
                        event.getEventId(),
                        event.getEventDescription(),
                        event.getEventBody(),
                        event.getEventCreator(),
                        event.getEventDateTime(),
                        event.getEventExpiration(),
                        event.getUsersJoinInEvent()));
            }
            return eventList;
        }
        return null;
    }
    @Override
    public EventResponseEntity update(UUID id, EventRequestEntity request) {
        if(id!=null) {
            var event = eventRepo.findById(id).orElseThrow(()
                    -> new IllegalArgumentException("Not found event with this id"));
            if (event != null && request != null) {
                event.setEventBody(request.getEventBody());
                event.setEventCreator(request.getEventCreator());
                event.setEventDescription(request.getEventDescription());
                event.setEventDateTime(request.getEventDateTime());
                event.setEventExpiration(request.getEventExpiration());
                var updatedEvent = eventRepo.save(event);
                return EventResponseEntity.builder()
                        .eventId(id)
                        .eventBody(updatedEvent.getEventBody())
                        .eventDescription(updatedEvent.getEventDescription())
                        .eventCreator(updatedEvent.getEventCreator())
                        .eventDateTime(updatedEvent.getEventDateTime())
                        .eventExpiration(updatedEvent.getEventExpiration())
                        .build();
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
            Event event = Event.builder()
                    .eventDescription(request.getEventBody())
                    .eventBody(request.getEventBody())
                    .eventCreator(jwtService.extractUsername(token.substring(7)))
                    .eventDateTime(request.getEventDateTime())
                    .eventExpiration(request.getEventExpiration())
                    .build();
            event.getUsersJoinInEvent().addAll(userSet);
           var newEvent = eventRepo.save(event);
           for(User user:userSet){
               user.getUserHasEvents().add(newEvent);
               userRepo.save(user);
           }
            return EventResponseEntity.builder()
                    .eventId(newEvent.getEventId())
                    .eventDescription(newEvent.getEventDescription())
                    .eventBody(newEvent.getEventBody())
                    .eventCreator(newEvent.getEventCreator())
                    .eventDateTime(newEvent.getEventDateTime())
                    .eventExpiration(newEvent.getEventExpiration())
                    .userSet(newEvent.getUsersJoinInEvent())
                    .build();
        }
        return null;
    }
}
