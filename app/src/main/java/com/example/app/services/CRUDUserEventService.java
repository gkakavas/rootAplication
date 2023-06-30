package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.CreateEventRequest;
import com.example.app.models.CreateEventResponse;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CRUDUserEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

//    public Event createEvent(CreateEventRequest request){
//        Set<User> usersSet= null;
//        var event = Event.builder()
//                .eventCreator(request.getEventCreator())
//                .eventBody(request.getEventBody())
//                .eventDateTime(request.getEventDateTime())
//                .eventDescription(request.getEventDescription())
//                .userSet()
//                .build();
//        eventRepository.save(event);
//        //request.getUsers().forEach(eventRepository.save(event.se));
//        return event;
//
//    }

}

