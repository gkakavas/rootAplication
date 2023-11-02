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
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.EventService;
import com.example.app.services.JwtService;
import com.example.app.utils.event.EntityResponseEventConverter;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private User currentUser;
    private String roleValue;
    private Principal principal;

    void setUpPrincipal(){
        this.currentUser = Instancio.of(User.class)
                .set(field(User::getRole),Role.valueOf(roleValue))
                .set(field(User::getRoleValue),roleValue)
                .create();
        this.principal = (Principal) currentUser;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventService(eventRepo, groupRepo, userRepo, jwtService, eventConverter);
    }

    @AfterEach
    void tearDown() {
    }


    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("Should store an event in database and returns the response")
    void shouldStoreAnEventInDatabaseAndReturnsTheCreatedEvent(String roleValue) throws UserNotFoundException {
        this.roleValue = roleValue;
        setUpPrincipal();
        var request = Instancio.create(EventRequestEntity.class);
        var usersOfEvent = request.getIdsSet().stream().map(uuid ->
                Instancio.of(User.class).set(field(User::getUserId),uuid).create()).collect(Collectors.toSet());
        var event = Event.builder()
                .eventId(UUID.randomUUID()).eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody()).eventCreator(currentUser.getUserId())
                .eventDateTime(LocalDateTime.parse(request.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(request.getEventExpiration()))
                .build();
        var eventWithAddedUser = Event.builder()
                .eventId(UUID.randomUUID()).eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody()).eventCreator(currentUser.getUserId())
                .eventDateTime(LocalDateTime.parse(request.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(request.getEventExpiration())).usersJoinInEvent(usersOfEvent)
                .build();
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(eventWithAddedUser.getEventId()).eventDescription(eventWithAddedUser.getEventDescription())
                .eventBody(eventWithAddedUser.getEventBody()).eventCreator("user with id " + eventWithAddedUser.getEventCreator())
                .eventDateTime(eventWithAddedUser.getEventDateTime()).eventExpiration(eventWithAddedUser.getEventExpiration())
                .users(eventWithAddedUser.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventConverter.fromRequestToEvent(request,currentUser.getUserId())).thenReturn(event);
        when(userRepo.findAllById(request.getIdsSet())).thenReturn(List.copyOf(usersOfEvent));
        when(eventRepo.save(eventWithAddedUser)).thenReturn(eventWithAddedUser);
        when(eventConverter.fromEventToAdminHrMngEvent(eventWithAddedUser)).thenReturn(expectedResponse);
        var response = eventService.create(request,this.principal);
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should Return An existing event")
    void shouldReturnAnExistingEvent() throws EventNotFoundException {
        var event = Instancio.create(Event.class);
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(event.getEventId()).eventBody(event.getEventBody())
                .eventDescription(event.getEventDescription()).eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration()).eventCreator("user with id" + event.getEventCreator())
                .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventRepo.findById(event.getEventId())).thenReturn(Optional.of(event));
        when(eventConverter.fromEventToAdminHrMngEvent(event)).thenReturn(expectedResponse);
        var response = eventService.read(event.getEventId());
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should Return All Events In Form That Corresponds To The Current User")
    void shouldReturnAllEventsInFormThatCorrespondsToTheCurrentUser() {
        var eventList = Instancio.ofList(Event.class).size(5).create();
        var expectedResponse = eventList.stream().map(event -> (EventResponseEntity) AdminHrMngEventResponse.builder()
                .eventId(event.getEventId()).eventDescription(event.getEventDescription())
                .eventBody(event.getEventBody()).eventCreator("user with id " + event.getEventCreator())
                .eventDateTime(event.getEventDateTime()).eventExpiration(event.getEventExpiration())
                .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build()).collect(Collectors.toSet());
        when(eventRepo.findAll()).thenReturn(eventList);
        when(eventConverter.fromEventListToAdminHrMngList(Set.copyOf(eventList))).thenReturn(expectedResponse);
        var response = eventService.read();
        assertEquals(List.copyOf(expectedResponse),response);
    }
    @Test
    @DisplayName("Should Update An Event, Save It And Returns Updated Event")
    void shouldUpdateAnEventSaveItAndReturnUpdatedEvent() throws EventNotFoundException {
        var updateRequest = Instancio.of(EventRequestEntity.class).create();
        var event = Instancio.of(Event.class)
                        .generate(field("usersJoinInEvent"), gen ->
                                gen.collection().size(updateRequest.getIdsSet().size())).create();
        var updatedEvent = Event.builder()
                .eventId(event.getEventId()).eventDescription(updateRequest.getEventDescription())
                .eventBody(updateRequest.getEventBody()).eventCreator(event.getEventCreator())
                .eventDateTime(LocalDateTime.parse(updateRequest.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(updateRequest.getEventExpiration()))
                .usersJoinInEvent(updateRequest.getIdsSet().stream().map(uuid
                        -> Instancio.of(User.class).set(field(User::getUserId),uuid).create())
                        .collect(Collectors.toSet()))
                .build();
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(updatedEvent.getEventId()).eventBody(updatedEvent.getEventBody())
                .eventDescription(updatedEvent.getEventDescription())
                .eventDateTime(updatedEvent.getEventDateTime())
                .eventExpiration(updatedEvent.getEventExpiration())
                .eventCreator("user with id" + event.getEventCreator())
                .users(updatedEvent.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventRepo.findById(event.getEventId())).thenReturn(Optional.of(event));
        when(eventConverter.eventUpdate(updateRequest, event)).thenReturn(updatedEvent);
        when(eventRepo.save(updatedEvent)).thenReturn(updatedEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(updatedEvent)).thenReturn(expectedResponse);
        var response = eventService.update(event.getEventId(), updateRequest);
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should delete a specified event from database")
    void shouldDeleteASpecificEventFromDatabase() throws EventNotFoundException {
        var event = Instancio.create(Event.class);
        when(eventRepo.findById(event.getEventId())).thenReturn(Optional.of(event));
        when(eventRepo.existsById(event.getEventId())).thenReturn(false);
        var response = eventService.delete(event.getEventId());
        assertFalse(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("Should Create An Event By Group And Return The Response")
    void shouldCreateAnEventBasedOnGroupAndReturnTheResponse(String roleValue) throws UserNotFoundException, GroupNotFoundException {
        this.roleValue = roleValue;
        setUpPrincipal();
        var group = Instancio.of(Group.class).create();
        var eventRequest = Instancio.create(EventRequestEntity.class);
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
        when(eventConverter.fromRequestToEvent(eventRequest, currentUser.getUserId())).thenReturn(newEvent);
        if(List.of(Role.ADMIN,Role.HR).contains(currentUser.getRole())){
            when(groupRepo.findById(group.getGroupId())).thenReturn(Optional.of(group));
        }
        when(eventRepo.save(newEvent)).thenReturn(newEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(newEvent)).thenReturn(expectedResponse);
        var response = eventService.createForGroup(eventRequest, group.getGroupId(),this.principal);
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should Add Users In An Existing Event And Return The  Response")
    void shouldAddUsersToAnExistingEventAndReturnTheResponse() throws EventNotFoundException {
        var idsSet = Instancio.ofSet(UUID.class).size(3).create();
        var usersToAdd = idsSet.stream().map(uuid -> Instancio.of(User.class).set(field(User::getUserId),uuid).create())
                .toList();
        var event = Instancio.create(Event.class);
        var mergedSet = event.getUsersJoinInEvent();
        mergedSet.addAll(usersToAdd);
        var patchedEvent = Event.builder()
                .eventId(event.getEventId()).eventDescription(event.getEventDescription()).eventBody(event.getEventBody())
                .eventCreator(event.getEventCreator()).eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration()).usersJoinInEvent(mergedSet)
                .build();
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(patchedEvent.getEventId()).eventBody(patchedEvent.getEventBody())
                .eventDescription(patchedEvent.getEventDescription()).eventDateTime(patchedEvent.getEventDateTime())
                .eventExpiration(patchedEvent.getEventExpiration()).eventCreator("creator with id " + patchedEvent.getEventCreator())
                .users(patchedEvent.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventRepo.findById(event.getEventId())).thenReturn(Optional.of(event));
        when(userRepo.findAllById(idsSet)).thenReturn(usersToAdd);
        when(eventRepo.save(patchedEvent)).thenReturn(patchedEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(patchedEvent)).thenReturn(expectedResponse);
        var response = eventService.addUsersToEvent(idsSet, event.getEventId());
        assertEquals(expectedResponse, response);
    }

    //removeUsersFromEvent
    @Test
    @DisplayName("Should Remove Users From An Existing Event And Return The Response")
    void shouldRemoveUsersFromAnExistingEventAndReturnTheResponse() throws EventNotFoundException {
        var event = Instancio.of(Event.class).ignore(field(Event::getUsersJoinInEvent)).create();
        var usersToRemain = Instancio.ofSet(User.class).size(5).create();
        var usersToRemove = Instancio.ofSet(User.class).size(4).create();
        var usersJoinInEvent = new HashSet<User>();
        usersJoinInEvent.addAll(usersToRemove);
        usersJoinInEvent.addAll(usersToRemain);
        event.getUsersJoinInEvent().addAll(usersJoinInEvent);
        var idsSet = usersToRemove.stream().map(User::getUserId).collect(Collectors.toSet());
        var patchedEvent = Event.builder()
                .eventId(event.getEventId()).eventDescription(event.getEventDescription()).eventBody(event.getEventBody())
                .eventCreator(event.getEventCreator()).eventDateTime(event.getEventDateTime())
                .eventExpiration(event.getEventExpiration()).usersJoinInEvent(usersToRemain)
                .build();
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(patchedEvent.getEventId()).eventBody(patchedEvent.getEventBody())
                .eventDescription(patchedEvent.getEventDescription()).eventDateTime(patchedEvent.getEventDateTime())
                .eventExpiration(patchedEvent.getEventExpiration()).eventCreator("creator with id " + patchedEvent.getEventCreator())
                .users(patchedEvent.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        when(eventRepo.findById(event.getEventId())).thenReturn(Optional.of(event));
        when(userRepo.findAllById(idsSet)).thenReturn(List.copyOf(usersToRemove));
        when(eventRepo.save(patchedEvent)).thenReturn(patchedEvent);
        when(eventConverter.fromEventToAdminHrMngEvent(patchedEvent)).thenReturn(expectedResponse);
        var response = eventService.addUsersToEvent(idsSet, event.getEventId());
        assertEquals(expectedResponse, response);
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
        assertEquals(expectedResponse, response);
        assertEquals(expectedResponse.getEventBody(), requestMap.get("eventBody"));
        assertEquals(expectedResponse.getEventDateTime(), LocalDateTime.of(2023, 7, 11, 11, 0, 1));
    }
}
