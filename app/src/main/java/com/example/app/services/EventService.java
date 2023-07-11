package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.EventResponseEntity;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService implements CrudService<EventResponseEntity, EventRequestEntity> {
    private final EventRepository eventRepo;
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

//    public EventResponseEntity createEventWithUsers(List<UUID> userIdsList, EventRequestEntity request){
//        List<User> users = userRepo.findAllById(userIdsList);
//            var event = Event.builder()
//                    .eventBody(request.getEventBody())
//                    .eventCreator(request.getEventCreator())
//                    .eventDescription(request.getEventDescription())
//                    .eventDateTime(request.getEventDateTime())
//                    .eventExpiration(request.getEventExpiration())
//                    .build();
//            event.getUsersJoinInEvent().addAll(users);
//            for(User user:users){
//                user.getUserHasEvents().add(event);
//                userRepo.save(user);
//            }
//            eventRepo.save(event);
//            return new EventResponseEntity(
//                    event.getEventId(),
//                    event.getEventDescription(),
//                    event.getEventBody(),
//                    event.getEventCreator(),
//                    event.getEventDateTime(),
//                    event.getEventExpiration(),
//                    event.getUsersJoinInEvent()
//            );
//    }
//
//    public EventResponseEntity addUsersToEvent(UUID eventId,List<UUID> userIds){
//        List<User> users = userRepo.findAllById(userIds);
//        var event = eventRepo.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Not found event with this id"));
//            event.getUsersJoinInEvent().addAll(users);
//            users.forEach(user -> {
//                user.getUserHasEvents().add(event);
//                    userRepo.save(user);
//            });
//            eventRepo.save(event);
//            return new EventResponseEntity(
//                    event.getEventId(),
//                    event.getEventDescription(),
//                    event.getEventBody(),
//                    event.getEventCreator(),
//                    event.getEventDateTime(),
//                    event.getEventExpiration(),
//                    event.getUsersJoinInEvent());
//
//    }
//
//    public EventResponseEntity deleteUsersFromEvent(UUID eventId,List<UUID> userIds){
//        var event = eventRepo.findById(eventId).orElseThrow(()->new IllegalArgumentException("Not found event with this id"));
//        var users = userRepo.findAllById(userIds);
//        users.forEach(event.getUsersJoinInEvent()::remove);
//        eventRepo.save(event);
//        for(User user:users){
//            user.getUserHasEvents().remove(event);
//            userRepo.save(user);
//        }
//        return new EventResponseEntity(
//                event.getEventId(),
//                event.getEventDescription(),
//                event.getEventBody(),
//                event.getEventCreator(),
//                event.getEventDateTime(),
//                event.getEventExpiration(),
//                event.getUsersJoinInEvent());
//    }
}
