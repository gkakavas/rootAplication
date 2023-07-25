package com.example.app.utils.event;

import com.example.app.entities.Event;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import org.springframework.stereotype.Component;

@Component
public class EventToAdminHrMngEvent {
    public EventResponseEntity convertToResponse(Event event){
        return AdminHrMngEventResponse.builder()
                .eventId(event.getEventId())
                .eventDescription(event.getEventDescription())
                .eventBody(event.getEventBody())
                .eventCreator(event.getEventCreator())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .users(event.getUsersJoinInEvent())
                .build();
    }
}
