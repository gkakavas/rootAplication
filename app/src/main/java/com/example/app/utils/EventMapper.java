package com.example.app.utils;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.EventResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class EventMapper {
    public Event convertToEvent(EventRequestEntity request, UUID eventCreator){
        return Event.builder()
                .eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody())
                .eventCreator(eventCreator)
                .eventDateTime(request.getEventDateTime())
                .eventExpiration(request.getEventExpiration())
                .build();
    }
    public EventResponseEntity convertToResponse(Event event){
        return EventResponseEntity.builder()
                .eventId(event.getEventId())
                .eventDescription(event.getEventDescription())
                .eventBody(event.getEventBody())
                .eventCreator(event.getEventCreator())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .userSet(event.getUsersJoinInEvent())
                .build();
    }

}
