package com.example.app.utils.event;

import com.example.app.entities.Event;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class EntityResponseEventConverterImpl implements EntityResponseEventConverter{
private final UserRepository userRepo;

    @Override
    public EventResponseEntity fromEventToMyEvent(Event event){
        return MyEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .build();
    }
    @Override
    public EventResponseEntity fromEventToAdminHrMngEvent(Event event){
        var response = AdminHrMngEventResponse.builder()
                .eventId(event.getEventId())
                .eventDescription(event.getEventDescription())
                .eventBody(event.getEventBody())
                .eventCreator(null)
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .build();
        event.getUsersJoinInEvent().forEach((user)->response.getUsers().add(user.getEmail()));
        try {
            response.setEventCreator(userRepo.findById(event.getEventCreator()).orElseThrow().getEmail());
        }catch (NoSuchElementException e){
            response.setEventCreator(null);
        }
        return response;
    }

    @Override
    public Set<EventResponseEntity> fromEventListToAdminHrMngList(Set<Event> events){
        Set<EventResponseEntity> responseList = new HashSet<>();
        events.forEach(((event) -> responseList.add(fromEventToAdminHrMngEvent(event))));
        return responseList;
    }
    @Override
    public Set<EventResponseEntity> fromEventListToMyList(Set<Event> events){
        Set<EventResponseEntity> responseList = new HashSet<>();
        events.forEach(((event) -> responseList.add(fromEventToMyEvent(event))));
        return responseList;
    }

    @Override
    public Event fromRequestToEvent(EventRequestEntity request, UUID eventCreator){
        return Event.builder()
                .eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody())
                .eventCreator(eventCreator)
                .eventDateTime(request.getEventDateTime())
                .eventExpiration(request.getEventExpiration())
                .build();
    }

    @Override
    public Event eventUpdate(EventRequestEntity request, Event event) {
        event.setEventDescription(request.getEventDescription());
        event.setEventBody(request.getEventBody());
        event.setEventDateTime(request.getEventDateTime());
        event.setEventExpiration(request.getEventExpiration());
        return event;
    }
}
