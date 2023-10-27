package com.example.app.utils.event;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
                .eventDateTime(event.getEventDateTime().truncatedTo(ChronoUnit.SECONDS))
                .eventExpiration(event.getEventExpiration().truncatedTo(ChronoUnit.SECONDS))
                .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        if(event.getEventCreator()!=null) {
            try {
                response.setEventCreator(userRepo.findById(event.getEventCreator()).orElseThrow(UserNotFoundException::new).getEmail());
            } catch (UserNotFoundException e) {
                response.setEventCreator(null);
            }
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
                .eventDateTime(LocalDateTime.parse(request.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(request.getEventExpiration()))
                .build();
    }

    @Override
    public Event eventUpdate(EventRequestEntity request, Event event) {
        event.setEventDescription(request.getEventDescription());
        event.setEventBody(request.getEventBody());
        event.setEventDateTime(LocalDateTime.parse(request.getEventDateTime()));
        event.setEventExpiration(LocalDateTime.parse(request.getEventExpiration()));
        if(request.getIdsSet()!=null){
            try {
                var users = userRepo.findAllById(request.getIdsSet());
                event.getUsersJoinInEvent().clear();
                event.getUsersJoinInEvent().addAll(users);
            }catch (Exception e){
                throw new RuntimeException("error during retrieving the users");
            }

        }
        return event;
    }
}
