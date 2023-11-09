package com.example.app.converters.event;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.converters.event.EntityResponseEventConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
public class EventConverterPositiveUnitTest {
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
            .generate(field(EventRequestEntity::getEventDateTime),gen -> gen.temporal().localDateTime().as(localDateTime -> LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()))
            .generate(field(EventRequestEntity::getEventExpiration),gen -> gen.temporal().localDateTime().as(localDateTime -> LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()))
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
        var events = Instancio.stream(Event.class)
                .peek(event1 -> {
                    event1.setEventCreator(null);
                    event1.setEventDateTime(event1.getEventDateTime().truncatedTo(ChronoUnit.SECONDS));
                    event1.setEventExpiration(event1.getEventExpiration().truncatedTo(ChronoUnit.SECONDS));
                })
                .limit(10)
                .collect(Collectors.toSet());
        var expectedResult = events.stream().map(event1 -> AdminHrMngEventResponse.builder()
                        .eventId(event1.getEventId())
                        .eventDescription(event1.getEventDescription())
                        .eventBody(event1.getEventBody())
                        .eventDateTime(event1.getEventDateTime())
                        .eventExpiration(event1.getEventExpiration())
                        .users(event1.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toSet());
        var result = eventConverter.fromEventListToAdminHrMngList(Set.copyOf(events));
        Assertions.assertEquals(expectedResult,result);
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
                .eventDescription(eventRequest.getEventDescription())
                .eventBody(eventRequest.getEventBody())
                .eventCreator(user.getUserId())
                .eventDateTime(LocalDateTime.parse(eventRequest.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(eventRequest.getEventExpiration()))
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
                .eventDateTime(LocalDateTime.parse(eventRequest.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(eventRequest.getEventExpiration()))
                .usersJoinInEvent(event.getUsersJoinInEvent())
                .build();
        var response = eventConverter.eventUpdate(eventRequest,event);
        Assertions.assertEquals(expectedResponse,response);
    }
}
