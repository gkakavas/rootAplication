package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.*;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventService implements CrudService<EventResponseEntity,EventRequestEntity> {
    private final EventRepository eventRepo;
    @Override
    public EventResponseEntity create(EventRequestEntity request) {
        var newEvent = Event.builder()
                .eventCreator(request.getEventCreator())
                .eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody())
                .eventDateTime(request.getEventDateTime())
                .eventExpiration(request.getEventExpiration())
                .build();

        var event =eventRepo.save(newEvent);
        return EventResponseEntity.builder()
                .eventId(event.getEventId())
                .eventCreator(event.getEventCreator())
                .eventDescription(event.getEventDescription())
                .eventBody(event.getEventBody())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .build();
    }

    @Override
    public EventResponseEntity read(UUID id) {
        if (id != null)
            try {
                Event event = eventRepo.findById(id).orElseThrow(()
                       -> new IllegalArgumentException("Not found event with this id"));
                return EventResponseEntity.builder()
                        .eventId(event.getEventId())
                        .eventCreator(event.getEventCreator())
                        .eventDescription(event.getEventDescription())
                        .eventBody(event.getEventBody())
                        .eventDateTime(event.getEventDateTime())
                        .eventExpiration(event.getEventExpiration())
                        .build();
            }catch(IllegalArgumentException e){
                e.printStackTrace();
            }
        return null;
    }

    @Override
    public List<EventResponseEntity> read() {
        List<Event> events = eventRepo.findAll();
        List<EventResponseEntity> eventList = events.stream()
                .map(event -> new EventResponseEntity(
                        event.getEventId(),
                        event.getEventDescription(),
                        event.getEventBody(),
                        event.getEventCreator(),
                        event.getEventDateTime(),
                        event.getEventExpiration()))
                .collect(Collectors.toList());
        return eventList;
    }
    @Override
    public EventResponseEntity update(UUID id, EventRequestEntity request) {
        var event = eventRepo.findById(id).orElseThrow(()
                -> new IllegalArgumentException("Not found user with this id"));
        if(event!=null&&request!=null){
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
}
