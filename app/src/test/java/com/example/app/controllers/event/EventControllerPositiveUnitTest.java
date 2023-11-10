package com.example.app.controllers.event;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestConfig;
import com.example.app.config.TestSecurityConfig;
import com.example.app.models.requests.UserIdsSet;
import com.example.app.controllers.EventController;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.services.EventService;
import com.example.app.tool.EventRelevantGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("unit")
@WebMvcTest
@ContextConfiguration(classes = {TestConfig.class,TestSecurityConfig.class, EventController.class, ApplicationExceptionHandler.class})
public class EventControllerPositiveUnitTest {
    @MockBean
    private EventService eventService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private User currentUser;
    private String roleValue;

    void setUp() {
        this.currentUser = Instancio.of(User.class)
                .set(field(User::getRole), Role.valueOf(this.roleValue))
                .create();
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR"})
    @DisplayName("Should create a new event and return this")
    void shouldCreateANewEventAndReturnThisBack(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var request = EventRelevantGenerator.generateValidEventRequestEntity("test event description");
        request.setIdsSet(Instancio.create(UserIdsSet.class));
        var response = AdminHrMngEventResponse.builder()
                .eventId(UUID.randomUUID())
                .eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody())
                .eventCreator(currentUser.getEmail())
                .eventDateTime(LocalDateTime.parse(request.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(request.getEventExpiration()))
                .users(request.getIdsSet().getUserIds().stream()
                        .map(uuid -> Instancio.create(String.class)).collect(Collectors.toSet()))
                .build();
        when(eventService.create(eq(request),nullable(User.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/event/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("Should create a new event by group and return this")
    void shouldCreateANewEventByGroupAndReturnThisBack(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var request = EventRelevantGenerator.generateValidEventRequestEntity("test event description");
        request.setIdsSet(Instancio.create(UserIdsSet.class));
        var response = AdminHrMngEventResponse.builder()
                .eventId(UUID.randomUUID())
                .eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody())
                .eventCreator(currentUser.getEmail())
                .eventDateTime(LocalDateTime.parse(request.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(request.getEventExpiration()))
                .users(request.getIdsSet().getUserIds().stream()
                        .map(uuid -> Instancio.create(String.class)).collect(Collectors.toSet()))
                .build();
        when(eventService.createForGroup(eq(request),eq(currentUser.getGroup().getGroupId()),nullable(User.class)))
                .thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/event/createGroupEvent/{id}",currentUser.getGroup().getGroupId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("Should return a specified event")
    void shouldReturnTheSpecifiedEvent(String roleValue) throws Exception {
        var adminHrMngResponse = Instancio.create(AdminHrMngEventResponse.class);
        this.roleValue = roleValue;
        setUp();
        when(eventService.read(eq(adminHrMngResponse.getEventId()))).thenReturn(adminHrMngResponse);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/event/{id}",adminHrMngResponse.getEventId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminHrMngResponse)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("Should return all events")
    void shouldReturnAllEvents(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var adminHrResponse = Instancio.createList(AdminHrMngEventResponse.class);
        when(eventService.read()).thenReturn(List.copyOf(adminHrResponse));
        this.mockMvc.perform(MockMvcRequestBuilders.get("/event/all"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminHrResponse)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","USER"})
    @DisplayName("Should update an event and return updated event")
    void shouldUpdateAnEventAndReturnThisBack(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var request = EventRelevantGenerator.generateValidEventRequestEntity("TestEventDescription");
        request.setIdsSet(Instancio.create(UserIdsSet.class));
        var response = AdminHrMngEventResponse.builder()
                .eventId(UUID.randomUUID())
                .eventDescription(request.getEventDescription())
                .eventBody(request.getEventBody())
                .eventCreator(currentUser.getEmail())
                .eventDateTime(LocalDateTime.parse(request.getEventDateTime()))
                .eventExpiration(LocalDateTime.parse(request.getEventExpiration()))
                .users(request.getIdsSet().getUserIds().stream()
                        .map(uuid -> Instancio.create(String.class)).collect(Collectors.toSet()))
                .build();
        when(eventService.update(eq(response.getEventId()),eq(request))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/event/update/{id}",response.getEventId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("Should delete a user")
    void shouldDeleteAUser(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var eventUUIDToDelete = UUID.randomUUID();
        when(eventService.delete(eq(eventUUIDToDelete))).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/event/delete/{id}",eventUUIDToDelete.toString()))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("should add users to an existing event")
    void shouldAddUsersToAnExistingEvent(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var request = UserIdsSet.builder().userIds(Instancio.ofSet(UUID.class).create()).build();
        var response = Instancio.of(AdminHrMngEventResponse.class).ignore(field(AdminHrMngEventResponse::getUsers)).create();
        response.setUsers(request.getUserIds().stream().map(uuid -> Instancio.create(String.class)).collect(Collectors.toSet()));
        when(eventService.addUsersToEvent(eq(request),eq(response.getEventId()))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/event/addUsers/{eventId}",response.getEventId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("should remove users from an existing event")
    void shouldRemoveUsersFromAnExistingEvent(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var request = UserIdsSet.builder().userIds(Instancio.ofSet(UUID.class).create()).build();
        var response = Instancio.of(AdminHrMngEventResponse.class).ignore(field(AdminHrMngEventResponse::getUsers)).create();
        response.setUsers(request.getUserIds().stream().map(uuid -> Instancio.create(String.class)).collect(Collectors.toSet()));
        when(eventService.removeUsersFromEvent(eq(request),eq(response.getEventId()))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/event/removeUsers/{eventId}",response.getEventId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER"})
    @DisplayName("should patch the details of an existing event")
    void shouldPatchTheDetailsOfAnExistingEvent(String roleValue) throws Exception{
        this.roleValue = roleValue;
        setUp();
        var eventRequestEntity = EventRelevantGenerator.generateValidEventRequestEntity("This is the event Description");
        var request = new HashMap<String,String>();
        request.put("eventDescription",eventRequestEntity.getEventDescription());
        request.put("eventBody",eventRequestEntity.getEventBody());
        request.put("eventDateTime",eventRequestEntity.getEventDateTime());
        request.put("eventExpiration",eventRequestEntity.getEventExpiration());
        var response = AdminHrMngEventResponse.builder()
                .eventId(UUID.randomUUID()).eventDescription(request.get("eventDescription"))
                .eventBody(request.get("eventBody")).eventCreator(currentUser.getEmail())
                .eventDateTime(LocalDateTime.parse(request.get("eventDateTime")))
                .eventExpiration(LocalDateTime.parse(request.get("eventExpiration"))).users(Instancio.createSet(String.class))
                .build();
        when(eventService.patchEventDetails(eq(response.getEventId()),eq(request))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/event/patchEventDetails/{eventId}",response.getEventId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

    }
}
