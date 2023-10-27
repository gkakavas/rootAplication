package com.example.app.integration.positive;

import com.example.app.entities.Event;
import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.example.app.tool.EventRelevantGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventPositiveIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private ObjectMapper objectMapper;
    private static TestRestTemplate restTemplate;
    private String baseUrl;
    @Container
    public static PostgreSQLContainer<?> myPostgresContainer = new PostgreSQLContainer<>("postgres:13.11")
            .withCommand("postgres", "-c", "log_statement=all");
    private Group existingGroup;
    private List<User> existingUsers;

    private List<Event> existingEvents;
    private Event existingEvent;
    private User currentUser;
    private String currentToken;
    private String roleValue;
    private static HttpHeaders headers;
    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", myPostgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", myPostgresContainer::getUsername);
        registry.add("spring.datasource.password", myPostgresContainer::getPassword);
    }


    @BeforeAll
    public static void init() {
        myPostgresContainer.start();
        restTemplate = new TestRestTemplate();
        headers = new HttpHeaders();
    }

    public void setUp() {
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/event"));
        this.existingGroup = groupRepo.findByGroupName("group1").orElseThrow();
        this.existingUsers = userRepo.findAll();
        this.existingEvent = eventRepo.findByEventDescription("event_description_1").orElseThrow();
        this.existingEvents = eventRepo.findAll();
        switch (this.roleValue) {
            case "ADMIN" -> {
                currentUser = userRepo.findByEmail("firstname1@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "HR" -> {
                currentUser = userRepo.findByEmail("firstname4@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "MANAGER" -> {
                currentUser = userRepo.findByEmail("firstname3@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
        }

        headers.set("Authorization", "Bearer " + currentToken);
    }
        @AfterEach
        void tearDown () {
            SecurityContextHolder.clearContext();
        }

        @AfterAll
        public static void afterAll () {
            myPostgresContainer.stop();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ADMIN", "HR"})
        @DisplayName("when a create request dispatched should create a new event in database and return this event")
        void shouldCreateANewEventInDatabaseAndReturnThisEvent (String roleValue) throws JsonProcessingException {
            this.roleValue = roleValue;
            setUp();
            this.baseUrl = baseUrl.concat("/create");
            var createRequest = EventRelevantGenerator.generateValidEventRequestEntity("A_Random_Test_Event_Description");
            var existingUserEmails = existingUsers.stream().map(User::getEmail).collect(Collectors.toSet());
            var existingUserIds = existingUsers.stream().map(User::getUserId).collect(Collectors.toSet());
            createRequest.getIdsSet().addAll(existingUserIds);
            HttpEntity<EventRequestEntity> request = new HttpEntity<>(createRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    request,
                    String.class);
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngEventResponse>() {});
            var expectedResponse = AdminHrMngEventResponse.builder()
                    .eventId(actualResponse.getEventId())
                    .eventDescription(createRequest.getEventDescription())
                    .eventBody(createRequest.getEventBody()).eventCreator(currentUser.getEmail())
                    .eventDateTime(LocalDateTime.parse(createRequest.getEventDateTime()))
                    .eventExpiration(LocalDateTime.parse(createRequest.getEventExpiration()))
                    .users(existingUserEmails)
                    .build();
            assertEquals(expectedResponse, actualResponse);
            var event = eventRepo.findById(actualResponse.getEventId()).orElseThrow();
            var users = event.getUsersJoinInEvent();
            users.forEach(user -> user.getUserHasEvents().remove(event));
            userRepo.saveAll(users);
            eventRepo.deleteById(event.getEventId());

        }
        @ParameterizedTest
        @ValueSource(strings = {"ADMIN", "HR", "MANAGER"})
        @DisplayName("when a create group request dispatched " +
                "should create a group event in database and return this event")
        void shouldCreateAGroupEventInDatabaseAndReturnThisEvent (String roleValue) throws JsonProcessingException {
            this.roleValue = roleValue;
            setUp();
            this.baseUrl = baseUrl.concat("/createGroupEvent/").concat(this.existingGroup.getGroupId().toString());
            var createRequest = EventRelevantGenerator.generateValidEventRequestEntity("A_Random_Test_Event_Description");
            HttpEntity<EventRequestEntity> request = new HttpEntity<>(createRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    request,
                    String.class);
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngEventResponse>() {});
            var expectedResponse = AdminHrMngEventResponse.builder()
                    .eventId(actualResponse.getEventId()).eventDescription(createRequest.getEventDescription())
                    .eventBody(createRequest.getEventBody()).eventCreator(currentUser.getEmail())
                    .eventDateTime(LocalDateTime.parse(createRequest.getEventDateTime()))
                    .eventExpiration(LocalDateTime.parse(createRequest.getEventExpiration()))
                    .users(this.existingUsers.stream().map(User::getEmail).collect(Collectors.toSet()))
                    .build();
            assertEquals(expectedResponse, actualResponse);
            var event = eventRepo.findById(actualResponse.getEventId()).orElseThrow();
            var users = event.getUsersJoinInEvent();
            users.forEach(user -> user.getUserHasEvents().remove(event));
            userRepo.saveAll(users);
            eventRepo.deleteById(event.getEventId());
        }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("when a read one request dispatched should read an existing event from database and return this")
    void shouldReadAnExistingEventFromDatabaseAndReturnThisEventInFormatThatCorrespondsInUserType(String roleValue) throws JsonProcessingException {
        this.roleValue =roleValue;
        setUp();
        this.baseUrl = baseUrl.concat("/").concat(this.existingEvent.getEventId()+"");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngEventResponse>() {});
            var expectedResponse = AdminHrMngEventResponse.builder()
                    .eventId(existingEvent.getEventId()).eventDescription(existingEvent.getEventDescription())
                    .eventBody(existingEvent.getEventBody()).eventCreator(null)
                    .eventDateTime(existingEvent.getEventDateTime()).eventExpiration(existingEvent.getEventExpiration())
                    .users(existingEvent.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                    .build();
            assertEquals(expectedResponse, actualResponse);
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("when a read all request dispatched should read all existing events from database and return them")
    void shouldReadAllExistingEventsFromDatabaseAndReturnThemInFormatThatCorrespondsToUserType(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        this.baseUrl = baseUrl.concat("/all");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<AdminHrMngEventResponse>>() {});
        var expectedResponse = existingEvents.stream().map(event ->
                AdminHrMngEventResponse.builder()
                            .eventId(event.getEventId()).eventDescription(event.getEventDescription())
                            .eventBody(event.getEventBody()).eventCreator(null)
                            .eventDateTime(event.getEventDateTime()).eventExpiration(event.getEventExpiration())
                            .users(event.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                            .build()
        ).collect(Collectors.toSet());

        assertEquals(expectedResponse,actualResponse);
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("when an update request dispatched should update the existing event save it in database and return this")
    void shouldUpdateTheExistingEventSaveItInDatabaseAndReturnThis(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        this.baseUrl = baseUrl.concat("/update/").concat(this.existingEvent.getEventId()+"");
        var updateRequest = EventRelevantGenerator.generateValidEventRequestEntity(this.existingEvent.getEventDescription());
        HttpEntity<EventRequestEntity> request = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngEventResponse>() {});
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(existingEvent.getEventId())
                .eventDescription(updateRequest.getEventDescription())
                .eventBody(updateRequest.getEventBody())
                .eventCreator(null)
                .eventDateTime(LocalDateTime.parse(updateRequest.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(updateRequest.getEventExpiration()))
                .build();
        assertEquals(expectedResponse,actualResponse);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("when a delete request dispatched should delete the existing event from database and return no content 204")
    void shouldDeleteTheExistingEventFromDatabaseAndReturnNoContent204(String roleValue){
        this.roleValue = roleValue;
        setUp();
        var event = EventRelevantGenerator.generateValidEvent();
        event.getUsersJoinInEvent().addAll(this.existingUsers);
        var savedEvent = eventRepo.save(event);
        this.baseUrl = baseUrl.concat("/delete/").concat(savedEvent.getEventId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(204));
        assertFalse(eventRepo.existsById(savedEvent.getEventId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("when a addUsersToEvent request dispatched should add existing users to existing event and return the patched event")
    void shouldAddExistingUsersInAnExistingEventAndReturnThePatchedEvent(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        this.baseUrl = baseUrl.concat("/addUsers/").concat(this.existingEvent.getEventId().toString());
        var addUsersToEventRequest = new HashSet<>(
                Set.of(
                    UUID.fromString("45b3df4b-f5bf-49d1-b928-16bbdb8e323e"),
                    UUID.fromString("4d0dd9db-b777-4e8e-97ba-ef0b57534927")
                )
        );
        HttpEntity<Set<UUID>> request = new HttpEntity<>(addUsersToEventRequest,headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PATCH,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngEventResponse>() {});
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(existingEvent.getEventId())
                .eventDescription(existingEvent.getEventDescription())
                .eventBody(existingEvent.getEventBody())
                .eventCreator(null)
                .eventDateTime(existingEvent.getEventDateTime())
                .eventExpiration(existingEvent.getEventExpiration())
                .users(Set.of
                        (
                                "firstname1@email.com",
                                "firstname2@email.com",
                                "firstname3@email.com",
                                "firstname4@email.com",
                                "firstname5@email.com"
                        )
                )
                .build();
        assertEquals(expectedResponse, actualResponse);
        existingEvent = eventRepo.save(existingEvent);
        assertEquals(3,existingEvent.getUsersJoinInEvent().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("when a removeUsersFromEvent request dispatched should remove existing users from existing event and return the patched event")
    void shouldRemoveExistingUsersFromAnExistingEventAndReturnThePatchedEvent(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        this.baseUrl = baseUrl.concat("/removeUsers/").concat(this.existingEvent.getEventId().toString());
        var usersToAdd = userRepo.findAllById(Set.of(
                UUID.fromString("45b3df4b-f5bf-49d1-b928-16bbdb8e323e"),
                UUID.fromString("4d0dd9db-b777-4e8e-97ba-ef0b57534927")
        ));
        for(User user:usersToAdd){
            this.existingEvent.getUsersJoinInEvent().add(user);
            user.getUserHasEvents().add(this.existingEvent);
        }
        this.existingEvent = eventRepo.save(this.existingEvent);
        assertEquals(5,this.existingEvent.getUsersJoinInEvent().size());
        var removeUsersFromEventRequest = new HashSet<>(
                Set.of(
                        UUID.fromString("45b3df4b-f5bf-49d1-b928-16bbdb8e323e"),
                        UUID.fromString("4d0dd9db-b777-4e8e-97ba-ef0b57534927")
                )
        );

        HttpEntity<Set<UUID>> request = new HttpEntity<>(removeUsersFromEventRequest,headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PATCH,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngEventResponse>() {});
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(existingEvent.getEventId())
                .eventDescription(existingEvent.getEventDescription())
                .eventBody(existingEvent.getEventBody())
                .eventCreator(null)
                .eventDateTime(existingEvent.getEventDateTime())
                .eventExpiration(existingEvent.getEventExpiration())
                .users(Set.of
                        (
                                "firstname1@email.com",
                                "firstname2@email.com",
                                "firstname3@email.com"
                        )
                )
                .build();
        assertEquals(expectedResponse, actualResponse);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("when an patch request dispatched should patch the existing event save it in database and return this")
    void shouldPatchTheExistingEventSaveItInDatabaseAndReturnThis(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        this.baseUrl = baseUrl.concat("/patchEventDetails/").concat(this.existingEvent.getEventId().toString());
        var patchRequest = new HashMap<String,String>();
        patchRequest.put("eventDescription","TestDescriptionForTestEvent");
        patchRequest.put("eventBody","Lorem ipsum dolor sit amet," +
                "consectetur adipiscing elit," +
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco " +
                "laboris nisi ut aliquip ex ea commodo consequat.");
        patchRequest.put("eventDateTime","2023-12-28T13:20:00");
        patchRequest.put("eventExpiration","2023-12-28T15:00:00");
        HttpEntity<Map<String,String>> request = new HttpEntity<>(patchRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PATCH,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngEventResponse>() {});
        var expectedResponse = AdminHrMngEventResponse.builder()
                .eventId(existingEvent.getEventId())
                .eventDescription(patchRequest.get("eventDescription"))
                .eventBody(patchRequest.get("eventBody"))
                .eventCreator(null)
                .eventDateTime(LocalDateTime.parse(patchRequest.get("eventDateTime")))
                .eventExpiration(LocalDateTime.parse(patchRequest.get("eventExpiration")))
                .users(existingEvent.getUsersJoinInEvent().stream().map(User::getEmail).collect(Collectors.toSet()))
                .build();
        assertEquals(expectedResponse,actualResponse);
        eventRepo.save(existingEvent);
    }
}

