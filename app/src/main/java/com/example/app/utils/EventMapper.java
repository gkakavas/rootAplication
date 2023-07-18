package com.example.app.utils;

import com.example.app.entities.Event;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.EventResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public Event convertToEvent(EventRequestEntity request, String eventCreator){
        Event event = new Event();
        event.setEventDescription(request.getEventDescription());
        event.setEventBody(request.getEventBody());
        event.setEventCreator(eventCreator);
        event.setEventDateTime(request.getEventDateTime());
        event.setEventExpiration(request.getEventExpiration());
        return event;
    }
    public EventResponseEntity convertToResponse(Event event){
        EventResponseEntity response = new EventResponseEntity();
        response.setEventId(event.getEventId());
        response.setEventDescription(event.getEventDescription());
        response.setEventBody(event.getEventBody());
        response.setEventCreator(event.getEventCreator());
        response.setEventDateTime(event.getEventDateTime());
        response.setEventExpiration(event.getEventExpiration());
        response.getUserSet().addAll(event.getUsersJoinInEvent());
        return response;

    }

}
