package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.CreateEventRequest;
import com.example.app.models.CreateEventResponse;
import com.example.app.models.FindOneEventRequest;
import com.example.app.models.FindOneEventResponse;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final UserService userService;

    public CreateEventResponse createEvent(CreateEventRequest request){
        try {
            var user = userService.retrieveUserProfile(request.getEventCreator());
            Event event = Event.builder()//
                    .eventCreator(user.getEmail()) //
                    .eventBody(request.getEventBody())
                    .eventDescription(request.getEventDescription())
                    .eventDateTime(request.getEventDateTime())
                    .eventExpiration(request.getEventExpiration())
                    .build();
            eventRepo.save(event);
            return CreateEventResponse.builder()
                    .eventCreator(event.getEventCreator())
                    .eventBody(event.getEventBody())
                    .eventDescription(event.getEventDescription())
                    .eventDateTime(event.getEventDateTime())
                    .eventExpiration(event.getEventExpiration())
                    .build();
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        return null;
        }

    public List<Event> retrieveAllEvents(){
       List<Event> allEvents =  eventRepo.findAll();
       return allEvents;
    }

    public FindOneEventResponse findOneEvent(FindOneEventRequest request){
        var event = eventRepo.findById(request.getEventId());
        if(event.isPresent()){
            return FindOneEventResponse.builder()
                    .eventId(event.get().getEventId())
                    .eventCreator(event.get().getEventCreator())
                    .eventBody(event.get().getEventBody())
                    .eventDescription(event.get().getEventDescription())
                    .eventDateTime(event.get().getEventDateTime())
                    .eventExpiration(event.get().getEventExpiration())
                    .build();
        }
        else
            throw new IllegalArgumentException();
    }

    public CreateEventResponse addUserToEvent(Integer eventId, Integer userId) {
            var user = userRepo.findById(userId).orElse(null);
            var event = eventRepo.findById(eventId).orElse(null);
            if(user!=null && event!=null) {
                user.getUserHasEvents().add(event);
                event.getUsersJoinInEvent().add(user);
                userRepo.save(user);
                eventRepo.save(event);
                return CreateEventResponse.builder()
                        .eventCreator(event.getEventCreator())
                        .eventDescription(event.getEventDescription())
                        .eventBody(event.getEventBody())
                        .eventExpiration(event.getEventExpiration())
                        .eventDateTime(event.getEventDateTime())
                        .eventJoinInUser(event.getUsersJoinInEvent())
                        .build();
            }
            else
                throw new IllegalArgumentException();
    }
}
