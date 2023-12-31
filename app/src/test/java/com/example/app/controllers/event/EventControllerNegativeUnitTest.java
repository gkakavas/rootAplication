package com.example.app.controllers.event;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestConfig;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.EventController;
import com.example.app.exception.EventNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.requests.UserIdsSet;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestConfig.class,TestSecurityConfig.class, EventController.class, ApplicationExceptionHandler.class})
@ActiveProfiles("unit")
public class EventControllerNegativeUnitTest {
    @MockBean
    private EventService eventService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    static String invalidDetailsEventRequest;

    static String invalidIdsSetEventRequest;

    @BeforeAll
    static void setUpJsons() throws IOException {
        Path invalidDetailsEventRequestPath = Path.of("src/test/resources/event/Invalid_Details_Event_Request.json");
        Path invalidIdsSetEventRequestPath = Path.of("src/test/resources/event/Invalid_idsSet_event_request.json");
        StringBuilder builder = new StringBuilder();
        var invalidDetailsEventRequestList = Files.readAllLines(invalidDetailsEventRequestPath);
        var invalidIdsSetEventRequestList = Files.readAllLines(invalidIdsSetEventRequestPath);
        invalidDetailsEventRequestList.forEach(builder::append);
        invalidDetailsEventRequest = builder.toString();
        builder.setLength(0);
        invalidIdsSetEventRequestList.forEach(builder::append);
        invalidIdsSetEventRequest = builder.toString();
        builder.setLength(0);

    }

    /*
     *create event negative unit test cases
     *_____________________________________
    */
    @ParameterizedTest
    @ValueSource(strings = {"/event/create", "/event/createGroupEvent/{id}"})
    @DisplayName("If the create request contains invalid values except for user idsSet" +
            "should return 400 and the incorrect values error messages")
    void shouldReturn400AndTheIncorrectValuesErrorMessages(String uri) throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.post(uri,UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidDetailsEventRequest)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.eventBody",
                        equalTo("Event body should contain at least 100 characters including spaces")))
                .andExpect(jsonPath("$.message.eventDescription",
                        equalTo("Event description should contain at least 5 and no many than 100 characters including spaces")))
                .andExpect(jsonPath("$.message.eventDateTime",
                        equalTo("Invalid event date and time format. The correct format is yyyy-MM-ddTHH:mm:ss")))
                .andExpect(jsonPath("$.message.eventExpiration",
                        equalTo("Invalid event expiration format. The correct format is yyyy-MM-ddTHH:mm:ss")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/event/create", "/event/createGroupEvent/{id}"})
    @DisplayName("If the create request contains null values " +
            "should return 400 and the null values error messages")
    void shouldReturn400AndTheNullValuesErrorMessages(String uri) throws Exception {
        var nullCreateRequest = EventRequestEntity.builder().build();
        this.mockMvc.perform(MockMvcRequestBuilders.post(uri,UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullCreateRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.eventDescription",
                        equalTo("Event description is required")))
                .andExpect(jsonPath("$.message.eventBody",
                        equalTo("Event body is required")))
                .andExpect(jsonPath("$.message.eventDateTime",
                        equalTo("Event date and time is required")))
                .andExpect(jsonPath("$.message.eventExpiration",
                        equalTo("Event expiration is required")));
    }

    @Test
    @DisplayName("If the create request contains not valid UUID values in idsSet" +
            "should return 400 and a response with error message and response status code")
    void shouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/event/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidIdsSetEventRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.userIds",equalTo("Invalid UUID values provided")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }
    /*
      read one event negative unit test cases
      _______________________________________
    */
    @Test
    @DisplayName("If the readOne request provide a UUID that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void readOneShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var nonExistingUUID = UUID.randomUUID();
        when(eventService.read(nonExistingUUID)).thenThrow(new EventNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.get("/event/{id}",nonExistingUUID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found event with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }

    /*
      update event negative unit test cases
      ______________________________________
    */
    @Test
    @DisplayName("If the update request contains invalid values except from idsSet UUID's "+
            "should return 400 and the incorrect values error messages")
    void eventUpdateShouldReturn400AndTheIncorrectValuesErrorMessages() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.put("/event/update/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDetailsEventRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.eventDescription",equalTo("Event description should contain at least 5 and no many than 100 characters including spaces")))
                .andExpect(jsonPath("$.message.eventBody",equalTo("Event body should contain at least 100 characters including spaces")))
                .andExpect(jsonPath("$.message.eventDateTime",equalTo("Invalid event date and time format. The correct format is yyyy-MM-ddTHH:mm:ss")))
                .andExpect(jsonPath("$.message.eventExpiration",equalTo("Invalid event expiration format. The correct format is yyyy-MM-ddTHH:mm:ss")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));

    }
    @Test
    @DisplayName("If the update request contains not valid UUID values in idsSet" +
            "should return 400 and a response with error message and response status code")
    void eventUpdateRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.put("/event/update/{id}",UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidIdsSetEventRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.userIds",equalTo("Invalid UUID values provided")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }
    @Test
    @DisplayName("If update request provide a UUID as path variable that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void updateRequestShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var nonExistingUUID = UUID.randomUUID();
        var updateRequest = Instancio.of(EventRequestEntity.class)
                .generate(field("eventDescription"),gen -> gen.string().length(5,100))
                .generate(field("eventBody"),gen -> gen.string().length(100,200))
                .set(field("eventDateTime"),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .set(field("eventExpiration"),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .create();
        System.err.println(objectMapper.writeValueAsString(updateRequest));
        when(eventService.update(nonExistingUUID,updateRequest)).thenThrow(new EventNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.put("/event/update/{id}",nonExistingUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found event with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }

    /*
      delete event negative unit test cases
      ______________________________________
     */
    @Test
    @DisplayName("If delete request provide a UUID as path variable that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void deleteRequestShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var nonExistingUUID = UUID.randomUUID();
        when(eventService.delete(nonExistingUUID)).thenThrow(new EventNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/event/delete/{id}",nonExistingUUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found event with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }
    /*
    Add Users To Event and Remove Users From Event negative test cases
    */

    @ParameterizedTest
    @ValueSource(strings = {
            "/event/addUsers/{eventId}",
            "/event/removeUsers/{eventId}"
    })
    @DisplayName("If addUsersToEvent request provide a UUID as path variable that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void addRemoveRequestShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode(String url) throws Exception {
        when(eventService.addUsersToEvent(any(UserIdsSet.class),any(UUID.class))).thenThrow(new EventNotFoundException());
        when(eventService.removeUsersFromEvent(any(UserIdsSet.class),any(UUID.class))).thenThrow(new EventNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.patch(url,UUID.randomUUID())
                .content(objectMapper.writeValueAsString(Instancio.create(UserIdsSet.class)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found event with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/event/addUsers/{eventId}",
            "/event/removeUsers/{eventId}"
    })
    @DisplayName("If addUsersToEvent request has invalid UUID format on idsSet of request body" +
            "should return 400 and a response with error message and response status code")
    void invalidAddRemoveRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode(String url) throws Exception {
        var request = "{\"userIds\":[\"invalidUUID1\",\"invalidUUID2\",\"invalidUUID3\"]}";
        this.mockMvc.perform(MockMvcRequestBuilders.patch(url,UUID.randomUUID())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.userIds",equalTo("Invalid UUID values provided")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }
    @ParameterizedTest
    @ValueSource(strings = {
            "/event/create",
            "/event/createGroupEvent/{id}",
            "/event/update/{id}",
            "/event/addUsers/{eventId}",
            "/event/removeUsers/{eventId}",
            "/event/patchEventDetails/{eventId}"
    })
    @DisplayName("If any request performs that have to contain body and request body is empty" +
            "should return 400 and a response with error message and response status code")
    void emptyRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode(String uri) throws Exception {
        switch (uri) {
            case "/event/create", "/event/createGroupEvent/{id}" ->
                    this.mockMvc.perform(MockMvcRequestBuilders.post(uri, UUID.randomUUID()))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.message.error", equalTo("Request body is required")))
                            .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
            case "/event/update/{id}" -> this.mockMvc.perform(MockMvcRequestBuilders.put(uri, UUID.randomUUID()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message.error", equalTo("Request body is required")))
                    .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
            case "/event/addUsers/{eventId}", "/event/removeUsers/{eventId}", "/event/patchEventDetails/{eventId}" ->
                    this.mockMvc.perform(MockMvcRequestBuilders.patch(uri, UUID.randomUUID()))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.message.error", equalTo("Request body is required")))
                            .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
        }

    }
    @ParameterizedTest
    @ValueSource(strings = {
            "/event/createGroupEvent/{id}",
            "/event/{id}",
            "/event/update/{id}",
            "/event/delete/{id}",
            "/event/addUsers/{eventId}",
            "/event/removeUsers/{eventId}",
            "/event/patchEventDetails/{eventId}"
    })
    @DisplayName("If any request performed that have to contains a UUID path variable and it is not valid" +
            "should return 400 and a response with error message and response status code")
    void invalidPathVariableUUIDRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode(String uri) throws Exception {
        String invalidUUID ="invalidUUID";
        switch (uri) {
            case "/event/createGroupEvent/{id}" -> this.mockMvc.perform(MockMvcRequestBuilders.post(uri, invalidUUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(UserRequestEntity.builder().build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Invalid path variable's UUID")))
                    .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
            case "/event/{id}" -> this.mockMvc.perform(MockMvcRequestBuilders.get(uri, invalidUUID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Invalid path variable's UUID")))
                    .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
            case "/event/update/{id}" -> this.mockMvc.perform(MockMvcRequestBuilders.put(uri, invalidUUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(UserRequestEntity.builder().build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Invalid path variable's UUID")))
                    .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
            case "/event/delete/{id}" -> this.mockMvc.perform(MockMvcRequestBuilders.delete(uri, invalidUUID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Invalid path variable's UUID")))
                    .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
            case "/event/addUsers/{eventId}", "/event/removeUsers/{eventId}" ->
                    this.mockMvc.perform(MockMvcRequestBuilders.patch(uri, invalidUUID)
                            .content(objectMapper.writeValueAsString(Instancio.create(UserIdsSet.class)))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Invalid path variable's UUID")))
                    .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
            case "/event/patchEventDetails/{eventId}" ->
                    this.mockMvc.perform(MockMvcRequestBuilders.patch(uri, invalidUUID)
                            .content(objectMapper.writeValueAsString(new HashMap<String, String>()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Invalid path variable's UUID")))
                    .andExpect(jsonPath("$.responseCode", equalTo(HttpStatus.BAD_REQUEST.name())));
        }
    }
    @ParameterizedTest
    @CsvSource({"invalidField1, invalidField2, invalidField3, inv, invalid size event body, invalid date value"})
    @DisplayName("If patchEventDetails request map contains invalid fields to patch" +
            "should return 400 and a response with error message and response status code")
    void invalidFieldsPatchRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode(
            String key1, String key2, String key3, String value1, String value2, String value3) throws Exception {
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put(key1,value1);
        requestMap.put(key2,value2);
        requestMap.put(key3,value3);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/event/patchEventDetails/{eventId}",UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message."+key1,equalTo("Field name is not valid")))
                .andExpect(jsonPath("$.message."+key2,equalTo("Field name is not valid")))
                .andExpect(jsonPath("$.message."+key3,equalTo("Field name is not valid")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If patchEventDetails request map contains invalid values" +
            "should return 400 and a response with error message and response status code")
    void invalidValuesPatchRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var invalidFieldsPatchRequest = "{" +
                "\"eventDescription\":\"inv\"," +
                "\"eventBody\":\"invalid size event body\"," +
                "\"eventDateTime\":\"invalid date value\"," +
                "\"eventExpiration\":\"invalid date value\"" +
                "}";
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/event/patchEventDetails/{eventId}",UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidFieldsPatchRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.eventDescription",equalTo("Event description should contain at least 5 and no many than 100 characters including spaces")))
                .andExpect(jsonPath("$.message.eventBody",equalTo("Event body should contain at least 100 characters including spaces")))
                .andExpect(jsonPath("$.message.eventDateTime",equalTo("Invalid event date and time format. The correct format is yyyy-MM-ddTHH:mm:ss")))
                .andExpect(jsonPath("$.message.eventExpiration",equalTo("Invalid event expiration format. The correct format is yyyy-MM-ddTHH:mm:ss")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }




}
