package com.example.app.services.event;

import com.example.app.entities.Event;
import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.EventNotFoundException;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.EventService;
import com.example.app.services.JwtService;
import com.example.app.utils.event.EntityResponseEventConverter;
import com.example.app.utils.event.EntityResponseEventConverterImpl;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializer;
import org.instancio.Instancio;
import org.instancio.Scope;
import org.instancio.Select;
import org.instancio.TypeToken;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class EventServicePositiveUnitTest {
    @InjectMocks
    private EventService eventService;
    @Mock
    private EventRepository eventRepo;
    @Mock
    private GroupRepository groupRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private JwtService jwtService;
    @Mock
    private EntityResponseEventConverter eventConverter;
    private final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventService(eventRepo, groupRepo, userRepo, jwtService, eventConverter);
        this.securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", List.of()));
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    @DisplayName("Should stores an event in database and returns the response")
    void shouldStoreAnEventInDatabaseAndReturnsTheCreatedEvent() throws UserNotFoundException {
        var request = Instancio.create(EventRequestEntity.class);
        var testEventCreator = "test@email.com";
        var testUser = Instancio.create(User.class);
        var testEmail = "test@creator.com";
        var testEvent = Event.builder()
                .eventId(UUID.randomUUID())
                .eventBody(request.getEventBody())
                .eventDescription(request.getEventDescription())
                .eventDateTime(LocalDateTime.parse(request.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(request.getEventExpiration()))
                .eventCreator(UUID.randomUUID())
                .usersJoinInEvent(Instancio.createSet(User.class))
                .build();
        var testUserEmails = Set.of("test1@email.com", "test2@email.com");
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(testEvent.getEventId())
                .eventBody(testEvent.getEventBody())
                .eventDescription(testEvent.getEventDescription())
                .eventDateTime(testEvent.getEventDateTime())
                .eventExpiration(testEvent.getEventExpiration())
                .eventCreator(testEventCreator)
                .users(testUserEmails)
                .build();
        when(jwtService.extractUsername(any(String.class))).thenReturn(testEmail);
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(testUser));
        when(eventConverter.fromRequestToEvent(any(EventRequestEntity.class), any(UUID.class))).thenReturn(testEvent);
        when(userRepo.findAllById(anySet())).thenReturn(Instancio.createList(User.class));
        when(eventRepo.save(any(Event.class))).thenReturn(testEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(testEvent)).thenReturn(expectedResponse);
        var response = eventService.create(request);
        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should Return An Event In Form of AdminHrManagerResponse")
    void shouldReturnAnEventInFormThatCorrespondsToTheCurrentUser() throws EventNotFoundException {
        var event = Instancio.create(Event.class);
        var userEmails = Instancio.ofSet(String.class).size(10).create();
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .eventCreator("email of" + event.getEventCreator())
                .users(userEmails)
                .build();
        when(eventRepo.findById(any(UUID.class))).thenReturn(Optional.of(event));
        when(eventConverter.fromEventToAdminHrMngEvent(eq(event))).thenReturn(expectedResponse);
        var response = eventService.read(UUID.randomUUID());
        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should Return All Events In Form That Corresponds To The Current User")
    void shouldReturnAllEventsInFormThatCorrespondsToTheCurrentUser() {
        var currentUser = Instancio.of(User.class)
                .generate(field("role"), gen -> gen.enumOf(Role.class))
                .generate(field("userHasEvents"), gen -> gen.collection().size(5))
                .create();
        var eventList = Instancio.ofList(Event.class).size(5).create();
        var adminHrMngSet = Instancio.ofSet(AdminHrMngEventResponse.class).size(5).create();
        var myEventSet = Instancio.ofSet(MyEventResponse.class).size(5).create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(eventRepo.findAll()).thenReturn(eventList);
        when(eventConverter.fromEventListToAdminHrMngList(Set.copyOf(eventList))).thenReturn(adminHrMngSet.stream().
                map(adminResponse -> (EventResponseEntity) adminResponse)
                .collect(Collectors.toSet()));
        when(eventConverter.fromEventListToMyList(currentUser.getUserHasEvents())).thenReturn(myEventSet.stream().
                map(adminResponse -> (EventResponseEntity) adminResponse)
                .collect(Collectors.toSet()));
        var response = eventService.read();
        if (currentUser.getRole().name().equals("ADMIN") || currentUser.getRole().name().equals("MANAGER")
                || currentUser.getRole().name().equals("HR")) {
            Assertions.assertEquals(response, List.copyOf(adminHrMngSet));
        } else if (currentUser.getRole().name().equals("USER")) {
            Assertions.assertEquals(response, List.copyOf(myEventSet));
        }
    }

    //update
    @Test
    @DisplayName("Should Update An Event, Save It And Returns Updated Event")
    void shouldUpdateAnEventSaveItAndReturnUpdatedEvent() throws EventNotFoundException {
        var updateRequest = Instancio.of(EventRequestEntity.class).create();
        var event = Instancio.of(Event.class).generate(field("usersJoinInEvent"), gen -> gen.collection().size(5)).create();
        var updatedEvent = Instancio.of(Event.class)
                .set(field("eventId"), event.getEventId())
                .set(field("usersJoinInEvent"), event.getUsersJoinInEvent())
                .create();
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(updateRequest.getEventBody())
                .eventDescription(updateRequest.getEventDescription())
                .eventDateTime(LocalDateTime.parse(updateRequest.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(updateRequest.getEventExpiration()))
                .eventCreator("creator with id" + event.getEventCreator().toString())
                .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventRepo.findById(eq(event.getEventId()))).thenReturn(Optional.of(event));
        when(eventConverter.eventUpdate(eq(updateRequest), eq(event))).thenReturn(updatedEvent);
        when(eventRepo.save(eq(updatedEvent))).thenReturn(updatedEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(eq(updatedEvent))).thenReturn(expectedResponse);
        var response = eventService.update(event.getEventId(), updateRequest);
        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should delete a specified event from database")
    void shouldDeleteASpecifiedEventFromDatabase() {

    }

    @Test
    @DisplayName("Should Create An Event By Group And Return The Response")
    void shouldCreateAnEventBasedOnGroupAndReturnTheResponse() throws UserNotFoundException, GroupNotFoundException {
        var currentUser = Instancio.of(User.class)
                .generate(field("role"), gen -> gen.enumOf(Role.class)
                        .excluding(Role.USER))
                .create();
        var group = Instancio.of(Group.class).create();
        var eventRequest = Instancio.of(EventRequestEntity.class)
                .create();
        var newEvent = Instancio.of(Event.class)
                .set(field("eventCreator"), currentUser.getUserId())
                .create();
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(newEvent.getEventId())
                .eventBody(newEvent.getEventBody())
                .eventDescription(newEvent.getEventDescription())
                .eventDateTime(newEvent.getEventDateTime())
                .eventExpiration(newEvent.getEventExpiration())
                .eventCreator("user with id " + newEvent.getEventCreator())
                .users(newEvent.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(currentUser));
        when(groupRepo.findById(group.getGroupId())).thenReturn(Optional.of(group));
        when(eventConverter.fromRequestToEvent(eventRequest, currentUser.getUserId())).thenReturn(newEvent);
        when(eventRepo.save(newEvent)).thenReturn(newEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(newEvent)).thenReturn(expectedResponse);
        var response = eventService.createForGroup(eventRequest, group.getGroupId());
        Assertions.assertEquals(expectedResponse, response);
    }

    //addUsersToEvent
    @Test
    @DisplayName("Should Add Users In An Existing Event And Return The  Response")
    void shouldAddUsersToAnExistingEventAndReturnTheResponse() throws EventNotFoundException {
        var idsSet = Instancio.ofSet(UUID.class).size(3).create();

        Stream<User> existingUsersSet = Instancio.stream(User.class)
                .limit(3)
                .peek(user -> user.setUserId(Objects.requireNonNull(idsSet.stream().findFirst().orElse(null))));
        var event = Instancio.of(Event.class)
                .generate(field("usersJoinInEvent"), gen -> gen.collection().size(5))
                .create();
        var userList = List.copyOf(existingUsersSet.collect(Collectors.toSet()));

        when(eventRepo.findById(event.getEventId())).thenReturn(Optional.of(event));
        when(userRepo.findAllById(idsSet)).thenReturn(userList);
        event.getUsersJoinInEvent().addAll(userList);
        when(eventRepo.save(event)).thenReturn(event);
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .eventCreator("creator with id " + event.getEventCreator())
                .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventConverter.fromEventToAdminHrMngEvent(event)).thenReturn(expectedResponse);
        var response = eventService.addUsersToEvent(idsSet, event.getEventId());
        Assertions.assertEquals(expectedResponse, response);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(expectedResponse.getUsers().size(), 8);
    }

    //removeUsersFromEvent
    @Test
    @DisplayName("Should Remove Users From An Existing Event And Return The Response")
    void shouldRemoveUsersFromAnExistingEventAndReturnTheResponse() throws EventNotFoundException {
        var idsSet = Instancio.ofSet(UUID.class).size(2).create();
        var event = Instancio.of(Event.class)
                .generate(field("usersJoinInEvent"), gen -> gen.collection().size(5))
                .create();
        Stream<User> existingUsersSet = Instancio.stream(User.class)
                .limit(2)
                .peek(user -> user.setUserId(Objects.requireNonNull(idsSet.stream().findFirst().orElse(null))));
        var userList = List.copyOf(existingUsersSet.collect(Collectors.toSet()));
        event.getUsersJoinInEvent().addAll(userList);
        Assertions.assertEquals(event.getUsersJoinInEvent().size(), 7);
        when(eventRepo.findById(event.getEventId())).thenReturn(Optional.of(event));
        when(userRepo.findAllById(idsSet)).thenReturn(userList);
        userList.forEach(event.getUsersJoinInEvent()::remove);
        Assertions.assertEquals(event.getUsersJoinInEvent().size(), 5);
        when(eventRepo.save(event)).thenReturn(event);
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(event.getEventId())
                .eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription())
                .eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration())
                .eventCreator("creator with id " + event.getEventCreator())
                .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventConverter.fromEventToAdminHrMngEvent(event)).thenReturn(expectedResponse);
        var response = eventService.removeUsersFromEvent(idsSet, event.getEventId());
        Assertions.assertEquals(expectedResponse, response);
        Assertions.assertNotNull(response);
    }

    //patchEventDetails
    @Test
    @DisplayName("Should Patch An Existing Event Details And Return The Response")
    void shouldPatchAnExistingEventDetailsAndReturnTheResponse() throws EventNotFoundException {
        var existingEvent = Instancio.of(Event.class)
                .generate(field("usersJoinInEvent"), gen -> gen.collection().size(5))
                .create();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("eventBody", "patched event body");
        requestMap.put("eventDateTime", "2023-07-11T11:00:01");
        existingEvent.setEventBody(requestMap.get("eventBody"));
        existingEvent.setEventDateTime(LocalDateTime.parse(requestMap.get("eventDateTime")));
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(existingEvent.getEventId())
                .eventBody(existingEvent.getEventBody())
                .eventDescription(existingEvent.getEventDescription())
                .eventDateTime(existingEvent.getEventDateTime())
                .eventExpiration(existingEvent.getEventExpiration())
                .eventCreator("creator with id " + existingEvent.getEventCreator())
                .users(existingEvent.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventRepo.findById(existingEvent.getEventId())).thenReturn(Optional.of(existingEvent));
        when(eventRepo.save(existingEvent)).thenReturn(existingEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(existingEvent)).thenReturn(expectedResponse);
        var response = eventService.patchEventDetails(existingEvent.getEventId(), requestMap);
        Assertions.assertEquals(expectedResponse, response);
        Assertions.assertEquals(expectedResponse.getEventBody(), requestMap.get("eventBody"));
        Assertions.assertEquals(expectedResponse.getEventDateTime(), LocalDateTime.of(2023, 7, 11, 11, 0, 1));
    }
}
