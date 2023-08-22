package com.example.app.controllers;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.entities.User;
import com.example.app.exception.EventNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.services.EventService;
import com.example.app.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestSecurityConfig.class,EventController.class, ApplicationExceptionHandler.class})
public class EventControllerTest {
    @MockBean
    private EventService eventService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String TEST_TOKEN = "testToken";
    private static final String USERNAME1 = "test@user1.com";
    private static final String USERNAME2 = "test@user2.com";
    private static final LocalDateTime eventDateTime = LocalDateTime.of(2023,9,18,15,0,1);
    private static final LocalDateTime eventExpiration = LocalDateTime.of(2023,9,18,16,0,1);
    private static final Set<String> usernameSet = new HashSet<>(Arrays.asList(USERNAME1,USERNAME2));
    private static final Set<UUID> uuidSet = new HashSet<>(Arrays.asList(UUID.randomUUID(),UUID.randomUUID()));
    private static final EventRequestEntity request = EventRequestEntity.builder()
            .eventBody("test_event_body")
            .eventDescription("test_event_description")
            .eventDateTime(eventDateTime)
            .eventExpiration(eventExpiration)
            .idsSet(uuidSet)
            .build();
    private static final AdminHrMngEventResponse response= AdminHrMngEventResponse.builder()
            .eventId(UUID.randomUUID())
            .eventBody("Test event Body")
            .eventDescription("Test event Description")
            .eventCreator("testEvent@creator.com")
            .eventDateTime(eventDateTime)
            .eventExpiration(eventExpiration)
            .users(usernameSet)
            .build();

    @Test
    @DisplayName("Should create a new event and return this")
    void shouldCreateANewEventAndReturnThisBack() throws Exception {
        when(eventService.create(any(EventRequestEntity.class),any(String.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/event/create")
                        .header("Authorization",TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId",equalTo(response.getEventId().toString())))
                .andExpect(jsonPath("$.eventBody",equalTo(response.getEventBody())))
                .andExpect(jsonPath("$.eventDescription",equalTo(response.getEventDescription())))
                .andExpect(jsonPath("$.eventCreator",equalTo(response.getEventCreator())))
                .andExpect(jsonPath("$.eventDateTime",equalTo(response.getEventDateTime().toString())))
                .andExpect(jsonPath("$.eventExpiration",equalTo(response.getEventExpiration().toString())))
                .andExpect(jsonPath("$.users",containsInAnyOrder(response.getUsers().toArray())));
    }

    @Test
    @DisplayName("Should create a new event by group and return this")
    void shouldCreateANewEventByGroupAndReturnThisBack() throws Exception {
        when(eventService.createForGroup(any(EventRequestEntity.class),any(String.class),any(UUID.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/event/createGroupEvent/{id}",UUID.randomUUID())
                        .header("Authorization",TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId",equalTo(response.getEventId().toString())))
                .andExpect(jsonPath("$.eventBody",equalTo(response.getEventBody())))
                .andExpect(jsonPath("$.eventDescription",equalTo(response.getEventDescription())))
                .andExpect(jsonPath("$.eventCreator",equalTo(response.getEventCreator())))
                .andExpect(jsonPath("$.eventDateTime",equalTo(response.getEventDateTime().toString())))
                .andExpect(jsonPath("$.eventExpiration",equalTo(response.getEventExpiration().toString())))
                .andExpect(jsonPath("$.users",containsInAnyOrder(response.getUsers().toArray())));
    }
    @Test
    @DisplayName("Should return a specified event")
    void shouldReturnTheSpecifiedUser() throws Exception {
        when(eventService.read(any(UUID.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/event/{id}",UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId",equalTo(response.getEventId().toString())))
                .andExpect(jsonPath("$.eventBody",equalTo(response.getEventBody())))
                .andExpect(jsonPath("$.eventDescription",equalTo(response.getEventDescription())))
                .andExpect(jsonPath("$.eventCreator",equalTo(response.getEventCreator())))
                .andExpect(jsonPath("$.eventDateTime",equalTo(response.getEventDateTime().toString())))
                .andExpect(jsonPath("$.eventExpiration",equalTo(response.getEventExpiration().toString())))
                .andExpect(jsonPath("$.users", containsInAnyOrder(response.getUsers().toArray())));
    }

    @Test
    @DisplayName("Should return all events")
    void shouldReturnAllEvents() throws Exception {
        EventResponseEntity response1 = AdminHrMngEventResponse.builder()
                .eventId(UUID.randomUUID())
                .eventBody("Event_Response_1_Body")
                .eventDescription("Event_Response_1_Description")
                .eventCreator("creator1@email.com")
                .eventDateTime(eventDateTime)
                .eventExpiration(eventExpiration)
                .users(Set.of("test1@user.com","test2@user.com"))
                .build();
        EventResponseEntity response2 = AdminHrMngEventResponse.builder()
                .eventId(UUID.randomUUID())
                .eventBody("Event_Response_2_Body")
                .eventDescription("Event_Response_2_Description")
                .eventCreator("creator2@email.com")
                .eventDateTime(eventDateTime)
                .eventExpiration(eventExpiration)
                .users(Set.of("test3@user.com","test4@user.com"))
                .build();
        List<EventResponseEntity> responseEntityList = List.of(response1,response2);
        when(eventService.read()).thenReturn(responseEntityList);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/event/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",equalTo(responseEntityList.size())));
    }

    @Test
    @DisplayName("Should update an event and return updated event")
    void shouldUpdateAnEventAndReturnThisBack() throws Exception {
        request.setEventBody("test_updated_event_body");
        request.setEventDescription("test_updated_event_description");
        var updateResponse = AdminHrMngEventResponse.builder()
                .eventId(UUID.randomUUID())
                .eventBody(request.getEventBody())
                .eventDescription(request.getEventDescription())
                .eventDateTime(request.getEventDateTime())
                .eventExpiration(request.getEventExpiration())
                .eventCreator(response.getEventCreator())
                .users(response.getUsers())
                .build();
        when(eventService.update(any(UUID.class),any(EventRequestEntity.class))).thenReturn(updateResponse);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/event/update/{id}",updateResponse.getEventId())
                        .header("Authorization",TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId",equalTo(updateResponse.getEventId().toString())))
                .andExpect(jsonPath("$.eventBody",equalTo(updateResponse.getEventBody())))
                .andExpect(jsonPath("$.eventDescription",equalTo(updateResponse.getEventDescription())))
                .andExpect(jsonPath("$.eventCreator",equalTo(updateResponse.getEventCreator())))
                .andExpect(jsonPath("$.eventDateTime",equalTo(updateResponse.getEventDateTime().toString())))
                .andExpect(jsonPath("$.eventExpiration",equalTo(updateResponse.getEventExpiration().toString())))
                .andExpect(jsonPath("$.users",containsInAnyOrder(updateResponse.getUsers().toArray())));
    }

    @Test
    @DisplayName("Should delete a user")
    void shouldDeleteAUser() throws Exception {
        when(eventService.delete(any(UUID.class))).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/event/delete/{id}",UUID.randomUUID())
                .header("Authorization",TEST_TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should add an user to an existing event")
    void shouldAddAnUserToAnExistingEvent() throws Exception {
        when(eventService.addUserToEvent(uuidSet,any(UUID.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/event/addUserToEvent/{id}",UUID.randomUUID())
                        .header("Authorization",TEST_TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should remove a user from an existing event")
    void shouldRemoveAnUserFromAnExistingEvent(){

    }
    @Test
    @DisplayName("should patch the details of an existing event")
    void shouldPatchTheDetailsOfAnExistingEvent(){

    }
}
