package com.example.app.controllers;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.entities.User;
import com.example.app.exception.EventNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.services.EventService;
import com.example.app.services.UserService;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    @DisplayName("Should create a new user and return this")
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
                .andExpect(jsonPath("$.users",equalTo(response.getUsers())));
    }

    @Test
    @DisplayName("Should create a new user and return this")
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
                .andExpect(jsonPath("$.users",equalTo(response.getUsers())));
    }
    @Test
    @DisplayName("Should return the specified event")
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
}
