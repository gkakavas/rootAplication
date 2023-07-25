package com.example.app.utils.event;

import com.example.app.entities.Event;
import com.example.app.models.requests.EventRequestEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EventRequestEntityToEvent {
    public Event convertToEvent(EventRequestEntity request, UUID eventCreator){
        return Event.builder()
                .eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody())
                .eventCreator(eventCreator)
                .eventDateTime(request.getEventDateTime())
                .eventExpiration(request.getEventExpiration())
                .build();
    }
}
