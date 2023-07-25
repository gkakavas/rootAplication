package com.example.app.utils.event;


import com.example.app.entities.Event;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.event.MyEventResponse;
import org.springframework.stereotype.Component;

@Component
public class EventToMyEvents {
    //24/7
    public EventResponseEntity convertToMyEvent(Event event){
        return MyEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .build();
    }
}
