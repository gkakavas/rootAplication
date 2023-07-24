package com.example.app.utils.event;


import com.example.app.entities.Event;
import com.example.app.models.responses.event.MyEventResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class EventToMyEvents {
    //24/7
    public MyEventResponseEntity eventToMyEvent(Event event){
        return MyEventResponseEntity.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .build();
    }
}
