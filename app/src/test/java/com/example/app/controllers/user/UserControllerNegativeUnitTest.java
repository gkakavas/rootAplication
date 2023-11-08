package com.example.app.controllers.user;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.UserController;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ActiveProfiles("unit")
@WebMvcTest
@ContextConfiguration(classes = {TestSecurityConfig.class, UserController.class, ApplicationExceptionHandler.class})
public class UserControllerNegativeUnitTest {
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    /*
     *create user negative unit test cases
     *____________________________________
     */
    @Test
    @DisplayName("If the create request contains invalid values except from group id " +
            "should return 400 and the incorrect values error messages")
    void createRequestShouldReturn400AndTheIncorrectValuesErrorMessages() throws Exception {
        var createRequest = UserRequestEntity.builder()
                .firstname("te").lastname("st").password("invalid_password").email("invalid_email")
                .specialization("specialization").group(UUID.randomUUID()).currentProject("current_project").role("invalid_role")
                .build();
        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.firstname",equalTo("Firstname must be between 4 and 50 characters")))
                .andExpect(jsonPath("$.message.lastname",equalTo("Lastname must be between 4 and 50 characters")))
                .andExpect(jsonPath("$.message.email",equalTo("Email must be in a normal email form")))
                .andExpect(jsonPath("$.message.password",equalTo("Password must be at least 8 characters long and it contains at least one letter and one digit")))
                .andExpect(jsonPath("$.message.role",equalTo("Role value is not in the correct form")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If the create request contains null values " +
            "should return 400 and the null values error messages")
    void createRequestShouldReturn400AndTheNullValuesErrorMessages() throws Exception {
        var createRequest = UserRequestEntity.builder()
                .firstname(null)
                .lastname(null)
                .password(null)
                .email(null)
                .specialization(null)
                .currentProject(null)
                .role(null)
                .build();
        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.firstname",equalTo("Firstname is required")))
                .andExpect(jsonPath("$.message.lastname",equalTo("Lastname is required")))
                .andExpect(jsonPath("$.message.password",equalTo("Password is required")))
                .andExpect(jsonPath("$.message.email",equalTo("Email is required")))
                .andExpect(jsonPath("$.message.group",equalTo("Group is required")))
                .andExpect(jsonPath("$.message.role",equalTo("Role is required")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If the create request contains not valid UUID group value" +
            "should return 400 and a response with error message and response status code")
    void createRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        String invalidGroupValue = "randomGroupValue";
        var createRequest = "{\"firstname\": \"test_firstName\"," +
                        "\"lastname\": \"test_lastname\"," +
                        "\"password\": \"Password123\"," +
                        "\"email\": \"test@email.com\"," +
                        "\"specialization\": \"test_specialization\"," +
                        "\"group\": \""+invalidGroupValue+"\"," +
                        "\"currentProject\": \"send\"," +
                        "\"role\": \"USER\"" +
                        "}";
        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                        .content(createRequest)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.group",equalTo("Invalid UUID value provided")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If the create request is empty" +
            "should return 400 and a response with error message and response status code")
    void createRequestShouldReturnStatus400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.error",equalTo("Request body is required")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    /*
      read one user negative unit test cases
      ______________________________________
    */

    @Test
    @DisplayName("If the readOne request has invalid UUID format" +
            "should return 400 and a response with error message and response status code")
    void readOneShouldReturn400StatusAndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var invalidUuidFormat = "invalidUUID";
        this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}",invalidUuidFormat)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",equalTo("Invalid path variable's UUID")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If the readOne request provide a UUID that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void readOneShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var nonExistingUUID = UUID.randomUUID();
        when(userService.read(eq(nonExistingUUID),nullable(User.class))).thenThrow(new UserNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}",nonExistingUUID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found user with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }

    /*
      update user negative unit test cases
      ______________________________________
     */
    @Test
    @DisplayName("If the update request contains invalid values except from group id "+
                    "should return 400 and the incorrect values error messages")
    void userUpdateShouldReturn400AndTheIncorrectValuesErrorMessages() throws Exception {
        var updateRequest = UserRequestEntity.builder()
                .firstname("te").lastname("st").password("invalid_password").email("invalid_email")
                .specialization("specialization").group(UUID.randomUUID()).currentProject("current_project").role("invalid_role")
                .build();
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.firstname",equalTo("Firstname must be between 4 and 50 characters")))
                .andExpect(jsonPath("$.message.lastname",equalTo("Lastname must be between 4 and 50 characters")))
                .andExpect(jsonPath("$.message.email",equalTo("Email must be in a normal email form")))
                .andExpect(jsonPath("$.message.password",equalTo("Password must be at least 8 characters long and it contains at least one letter and one digit")))
                .andExpect(jsonPath("$.message.role",equalTo("Role value is not in the correct form")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));

    }
    @Test
    @DisplayName("If the update request contains null values " +
            "should return 400 and the null values error messages")
    void userUpdateShouldReturn400AndTheNullValuesErrorMessages() throws Exception {
        var updateRequest = UserRequestEntity.builder()
                .firstname(null)
                .lastname(null)
                .password(null)
                .email(null)
                .specialization(null)
                .currentProject(null)
                .role(null)
                .build();
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}",UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.firstname",equalTo("Firstname is required")))
                .andExpect(jsonPath("$.message.lastname",equalTo("Lastname is required")))
                .andExpect(jsonPath("$.message.password",equalTo("Password is required")))
                .andExpect(jsonPath("$.message.email",equalTo("Email is required")))
                .andExpect(jsonPath("$.message.group",equalTo("Group is required")))
                .andExpect(jsonPath("$.message.role",equalTo("Role is required")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If the update request contains not valid UUID group value" +
            "should return 400 and a response with error message and response status code")
    void updateRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        String invalidGroupValue = "randomGroupValue";
        var updateRequest =
                "{\"firstname\": \"test_firstName\"," +
                "\"lastname\": \"test_lastname\"," +
                "\"email\": \"test@email.com\"," +
                "\"specialization\": \"test_specialization\"," +
                "\"group\": \""+invalidGroupValue+"\"," +
                "\"currentProject\": \"send\"," +
                "\"role\": \"USER\"" +
                "}";
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}",UUID.randomUUID())
                        .content(updateRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.group",equalTo("Invalid UUID value provided")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }
    @Test
    @DisplayName("If the update request is empty" +
            "should return 400 and a response with error message and response status code")
    void updateRequestShouldReturnStatus400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}",UUID.randomUUID())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.error",equalTo("Request body is required")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If update request has invalid UUID format as path variable " +
            "should return 400 and a response with error message and response status code")
    void updateRequestShouldReturn400StatusAndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var invalidUuidFormat = "invalidUUID";
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}",invalidUuidFormat)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",equalTo("Invalid path variable's UUID")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If update request provide a UUID as path variable that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void updateRequestShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var nonExistingUUID = UUID.randomUUID();
        var updateRequest = UserRequestEntity.builder()
                .firstname("testFirstname")
                .lastname("testLastname")
                .password("testPassword123")
                .email("test@email.com")
                .specialization("testSpecialization")
                .group(UUID.randomUUID())
                .currentProject("testCurrentProject")
                .role("USER")
                .build();
        when(userService.update(nonExistingUUID,updateRequest)).thenThrow(new UserNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}",nonExistingUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found user with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }

    /*
      delete user negative unit test cases
      ______________________________________
     */

    @Test
    @DisplayName("If delete request has invalid UUID format" +
            "should return 400 and a response with error message and response status code")
    void deleteRequestShouldReturn400StatusAndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var invalidUUID = "invalid_uuid";
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/user/delete/{id}",invalidUUID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",equalTo("Invalid path variable's UUID")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }
    @Test
    @DisplayName("If delete request provide a UUID as path variable that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void deleteRequestShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var nonExistingUUID = UUID.randomUUID();
        when(userService.delete(nonExistingUUID)).thenThrow(new UserNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/user/delete/{id}",nonExistingUUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found user with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }

    /*
     patch user negative unit test cases
      ______________________________________
     */

    @Test
    @DisplayName("If patch request has invalid UUID format" +
            "should return 400 and a response with error message and response status code")
    void patchRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        String invalidUUID = "invalidUUID";
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/user/patch/{id}",invalidUUID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",equalTo("Invalid path variable's UUID")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If patch request provide a UUID as path variable that is not exist in database " +
            "should return 404 and a response with error message and response status code")
    void patchRequestShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        var nonExistingUUID = UUID.randomUUID();
        Map<String,String> patchMap = new HashMap<>();
        patchMap.put("firstname","testFirstname");
        when(userService.patch(nonExistingUUID,patchMap)).thenThrow(new UserNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/user/patch/{id}",nonExistingUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchMap))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found user with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }
    @Test
    @DisplayName("If the patch request is null" +
            "should return 400 and a response with error message and response status code")
    void nullPatchRequestShouldReturnStatus400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/user/patch/{id}",UUID.randomUUID())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.error",equalTo("Request body is required")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    @Test
    @DisplayName("If the patch request contains not valid fields to update" +
            "should return 400 and a response with error message and response status code")
    void patchRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatus() throws Exception {
        Map<String,String> patchMap = new HashMap<>();
        var mapKey = "invalidField";
        var mapValue = "value";
        patchMap.put(mapKey,mapValue);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/user/patch/{id}",UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchMap))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message"+"."+mapKey,equalTo("Field name is not valid")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }

    /*
      read user events negative unit test cases
      ______________________________________
     */

    @Test
    @DisplayName("If the read user events request provide a UUID as path variable that is not exist in database"
    +"should return 404 and a response with error message and response status code")
    void readUserEventsRequestShouldReturn404AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception{
        when(userService.readUserEvents(any(UUID.class))).thenThrow(new UserNotFoundException());
        this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}/events",UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",equalTo("Not found user with this id")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.NOT_FOUND.name())));
    }

    @Test
    @DisplayName("If the read user events request has invalid UUID format"
            +"should return 400 and a response with error message and response status code")
    void readUserEventsRequestShouldReturn400AndAResponseWithErrorMessageAndResponseStatusCode() throws Exception{
        var invalidUUID = "invalidUUID";
        this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}/events",invalidUUID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",equalTo("Invalid path variable's UUID")))
                .andExpect(jsonPath("$.responseCode",equalTo(HttpStatus.BAD_REQUEST.name())));
    }
}
