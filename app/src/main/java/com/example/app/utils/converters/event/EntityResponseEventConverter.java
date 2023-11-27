package com.example.app.utils.converters.event;

import com.example.app.entities.Event;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;

import java.util.Set;
import java.util.UUID;

public interface EntityResponseEventConverter {
    EventResponseEntity fromEventToMyEvent(Event event);

    EventResponseEntity fromEventToAdminHrMngEvent(Event event);

    Set<EventResponseEntity> fromEventListToAdminHrMngList(Set<Event> events);

    Set<EventResponseEntity> fromEventListToMyList(Set<Event> events);

    Event fromRequestToEvent(EventRequestEntity request, UUID eventCreator);

    Event eventUpdate(EventRequestEntity request, Event event);
}
