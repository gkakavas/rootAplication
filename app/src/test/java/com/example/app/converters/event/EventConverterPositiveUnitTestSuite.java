package com.example.app.converters.event;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.event.EntityResponseEventConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import java.util.stream.Collectors;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EventConverterPositiveUnitTestSuite {
    @InjectMocks
    private EntityResponseEventConverterImpl eventConverter;
    @Mock
    private UserRepository userRepo;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        eventConverter = new EntityResponseEventConverterImpl(userRepo);
    }

    private static final Event event =  Instancio.of(Event.class)
            .generate(field("usersJoinInEvent"),gen -> gen.collection().size(5))
            .create();
    private static final User user = Instancio.of(User.class)
            .create();
    private static final EventRequestEntity eventRequest = Instancio.of(EventRequestEntity.class)
            .create();
    @Test
    @DisplayName("Should Convert An Event To My Event")
    void shouldConvertAnEventToMyEvent(){
        var expectedResponse = MyEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .build();
        var response = eventConverter.fromEventToMyEvent(event);
        Assertions.assertEquals(response,expectedResponse);

    }

    @Test
    @DisplayName("Should Convert An Event To AdminHrMngEvent")
    void shouldConvertAnEventToAdminHrMngEvent(){
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(user));
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .eventCreator(user.getEmail())
                .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        var response = eventConverter.fromEventToAdminHrMngEvent(event);
        Assertions.assertEquals(expectedResponse,response);
    }
    @Test
    @DisplayName("Should Convert An EventList To AdminHrMngList")
    void shouldConvertAnEventListToAdminHrMngList(){
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(user));
        var usersJoinInEvent = Instancio.ofSet(User.class).size(2).create();
        var eventsList = Instancio.ofList(Event.class)
                .size(2)
                .create();
        eventsList.forEach(event1 -> {
            event1.getUsersJoinInEvent().clear();
            event1.getUsersJoinInEvent().addAll(usersJoinInEvent);
        });

        var expectedResponse = new HashSet<>();
        expectedResponse.add(AdminHrMngEventResponse.builder()
                        .eventId(eventsList.get(0).getEventId())
                        .eventBody(eventsList.get(0).getEventBody())
                        .eventDescription(eventsList.get(0).getEventDescription())
                        .eventDateTime(eventsList.get(0).getEventDateTime())
                        .eventExpiration(eventsList.get(0).getEventExpiration())
                        .eventCreator(user.getEmail())
                        .users(eventsList.get(0).getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build());
        expectedResponse.add(AdminHrMngEventResponse.builder()
                        .eventId(eventsList.get(1).getEventId())
                        .eventBody(eventsList.get(1).getEventBody())
                        .eventDescription(eventsList.get(1).getEventDescription())
                        .eventDateTime(eventsList.get(1).getEventDateTime())
                        .eventExpiration(eventsList.get(1).getEventExpiration())
                        .eventCreator(user.getEmail())
                        .users(eventsList.get(1).getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                        .build());
        var response = eventConverter.fromEventListToAdminHrMngList(Set.copyOf(eventsList));
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should Convert An EventList To toMyList")
    void shouldConvertAnEventListTotoMyList(){
        var eventsList = Instancio.ofList(Event.class)
                .size(2)
                .create();
        var expectedResponse = new HashSet<>();
        expectedResponse.add(MyEventResponse.builder()
                .eventId(eventsList.get(0).getEventId())
                .eventBody(eventsList.get(0).getEventBody())
                .eventDescription(eventsList.get(0).getEventDescription())
                .eventDateTime(eventsList.get(0).getEventDateTime())
                .eventExpiration(eventsList.get(0).getEventExpiration())
                .build());
        expectedResponse.add(MyEventResponse.builder()
                .eventId(eventsList.get(1).getEventId())
                .eventBody(eventsList.get(1).getEventBody())
                .eventDescription(eventsList.get(1).getEventDescription())
                .eventDateTime(eventsList.get(1).getEventDateTime())
                .eventExpiration(eventsList.get(1).getEventExpiration())
                .build());
        var response = eventConverter.fromEventListToMyList(Set.copyOf(eventsList));
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should Convert An EventRequestEntity To Event")
    void shouldConvertAnEventRequestEntityToEvent(){

        var expectedResponse = Event.builder()
                .eventId(null)
                .eventDescription(eventRequest.getEventDescription())
                .eventBody(eventRequest.getEventBody())
                .eventCreator(user.getUserId())
                .eventDateTime(eventRequest.getEventDateTime())
                .eventExpiration(eventRequest.getEventExpiration())
                .build();
        var response = eventConverter.fromRequestToEvent(eventRequest,user.getUserId());
        Assertions.assertEquals(expectedResponse, response);

    }
    @Test
    @DisplayName("Should Update An Event By EventRequestEntity")
    void shouldUpdateAnEventByEventRequestEntity(){
        var expectedResponse = Event.builder()
                .eventId(event.getEventId())
                .eventDescription(eventRequest.getEventDescription())
                .eventBody(eventRequest.getEventBody())
                .eventCreator(event.getEventCreator())
                .eventDateTime(eventRequest.getEventDateTime())
                .eventExpiration(eventRequest.getEventExpiration())
                .usersJoinInEvent(event.getUsersJoinInEvent())
                .build();
        var response = eventConverter.eventUpdate(eventRequest,event);
        Assertions.assertEquals(expectedResponse,response);
    }
}
