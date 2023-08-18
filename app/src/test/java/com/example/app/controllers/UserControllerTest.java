package com.example.app.controllers;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.entities.Role;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestSecurityConfig.class,UserController.class, ApplicationExceptionHandler.class})
public class UserControllerTest {
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String TEST_FIRSTNAME = "firstname";
    private static final String TEST_LASTNAME="lastname";
    private static final String TEST_EMAIL="firstname@lastname";
    private static final String TEST_PASSWORD="Password1234";
    private static final String TEST_CURRENT_PROJECT="currentProject";
    private static final String TEST_SPECIALIZATION="specialization";
    private static final String TEST_ROLE = "ADMIN";
    private static final Role MODIFIED_TEST_ROLE = Role.USER;
    private static final UUID TEST_GROUP_ID = UUID.randomUUID();
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_GROUP_NAME = "The group name with this id " + TEST_GROUP_ID;
    private static final String TEST_CREATED_BY= "testUser";
    private static final LocalDateTime TEST_REGISTER_DATE =LocalDateTime.of(2023, Month.AUGUST,4,18,25,30) ;
    private static final String TEST_TOKEN = "testToken";

    private static final UUID TEST_EVENT_ID = UUID.randomUUID();

    private static final String TEST_EVENT_DESCRIPTION = "test description";

    private static final String TEST_EVENT_BODY = "test body";
    private static final String TEST_EVENT_BODY2 = "test body 2";

    private static final LocalDateTime TEST_EVENT_EXPIRATION = LocalDateTime.of(2023, Month.AUGUST,4,20,25,30);

    private static final LocalDateTime TEST_EVENT_DATE_TIME = LocalDateTime.of(2023, Month.AUGUST,4,18,25,30) ;

    private static final UserRequestEntity request = UserRequestEntity.builder()
            .firstname(TEST_FIRSTNAME)
            .lastname(TEST_LASTNAME)
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .currentProject(TEST_CURRENT_PROJECT)
            .specialization(TEST_SPECIALIZATION)
            .role(TEST_ROLE)
            .group(TEST_GROUP_ID)
            .build();
    private static final AdminUserResponse response = AdminUserResponse.builder()
            .userId(TEST_USER_ID)
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .specialization(request.getSpecialization())
            .currentProject(request.getSpecialization())
            .groupName(TEST_GROUP_NAME)
            .createdBy(TEST_CREATED_BY)
            .registerDate(TEST_REGISTER_DATE)
            .lastLogin(null)
            .role(Role.valueOf(request.getRole()))
            .build();

    @Test
    @DisplayName("Should create a new user and return this")
    void shouldCreateANewUserAndReturnThisBack() throws Exception {
        when(userService.create(any(UserRequestEntity.class),any(String.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                        .header("Authorization",TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId",equalTo(response.getUserId().toString())))
                .andExpect(jsonPath("$.firstname",equalTo(response.getFirstname())))
                .andExpect(jsonPath("$.lastname",equalTo(response.getLastname())))
                .andExpect(jsonPath("$.email",equalTo(response.getEmail())))
                .andExpect(jsonPath("$.specialization",equalTo(response.getSpecialization())))
                .andExpect(jsonPath("$.currentProject",equalTo(response.getCurrentProject())))
                .andExpect(jsonPath("$.groupName",equalTo(response.getGroupName())))
                .andExpect(jsonPath("$.createdBy",equalTo(response.getCreatedBy())))
                .andExpect(jsonPath("$.registerDate",equalTo(response.getRegisterDate().toString())))
                .andExpect(jsonPath("$.lastLogin",equalTo(response.getLastLogin())))
                .andExpect(jsonPath("$.role",equalTo(response.getRole().toString())));
    }
    @Test
    @DisplayName("Should return the specified user")
    void shouldReturnASpecifiedUser() throws Exception {
        when(userService.read(any(UUID.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}",TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId",equalTo(response.getUserId().toString())))
                .andExpect(jsonPath("$.firstname",equalTo(response.getFirstname())))
                .andExpect(jsonPath("$.lastname",equalTo(response.getLastname())))
                .andExpect(jsonPath("$.email",equalTo(response.getEmail())))
                .andExpect(jsonPath("$.specialization",equalTo(response.getSpecialization())))
                .andExpect(jsonPath("$.currentProject",equalTo(response.getCurrentProject())))
                .andExpect(jsonPath("$.groupName",equalTo(response.getGroupName())))
                .andExpect(jsonPath("$.createdBy",equalTo(response.getCreatedBy())))
                .andExpect(jsonPath("$.registerDate",equalTo(response.getRegisterDate().toString())))
                .andExpect(jsonPath("$.lastLogin",equalTo(response.getLastLogin())))
                .andExpect(jsonPath("$.role",equalTo(response.getRole().name())));
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() throws Exception {
        List<UserResponseEntity> list = new ArrayList<>();
        list.add(response);
        list.add(response);
        when(userService.read()).thenReturn(list);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/user/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",equalTo(list.size())));
    }

    @Test
    @DisplayName("ShouldUpdateAUser")
    void shouldUpdateAUser() throws Exception {
        when(userService.update(any(UUID.class),any(UserRequestEntity.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}",TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId",equalTo(response.getUserId().toString())))
                .andExpect(jsonPath("$.firstname",equalTo(response.getFirstname())))
                .andExpect(jsonPath("$.lastname",equalTo(response.getLastname())))
                .andExpect(jsonPath("$.email",equalTo(response.getEmail())))
                .andExpect(jsonPath("$.specialization",equalTo(response.getSpecialization())))
                .andExpect(jsonPath("$.currentProject",equalTo(response.getCurrentProject())))
                .andExpect(jsonPath("$.groupName",equalTo(response.getGroupName())))
                .andExpect(jsonPath("$.createdBy",equalTo(response.getCreatedBy())))
                .andExpect(jsonPath("$.registerDate",equalTo(response.getRegisterDate().toString())))
                .andExpect(jsonPath("$.lastLogin",equalTo(response.getLastLogin())))
                .andExpect(jsonPath("$.role",equalTo(response.getRole().toString())));
    }

    @Test
    @DisplayName("Should delete a specified user")
    void shouldDeleteAUser() throws Exception {
        when(userService.delete(any(UUID.class))).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/user/delete/{id}",TEST_USER_ID.toString()))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("Should patch a specified user")
    void shouldPatchAUser() throws Exception {
        Map<String,String> map = new HashMap<>();
        map.put("role",MODIFIED_TEST_ROLE.name());
        response.setRole(MODIFIED_TEST_ROLE);
        when(userService.patch(any(UUID.class), any())).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/user/patch/{id}",TEST_USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId",equalTo(response.getUserId().toString())))
                .andExpect(jsonPath("$.firstname",equalTo(response.getFirstname())))
                .andExpect(jsonPath("$.lastname",equalTo(response.getLastname())))
                .andExpect(jsonPath("$.email",equalTo(response.getEmail())))
                .andExpect(jsonPath("$.specialization",equalTo(response.getSpecialization())))
                .andExpect(jsonPath("$.currentProject",equalTo(response.getCurrentProject())))
                .andExpect(jsonPath("$.groupName",equalTo(response.getGroupName())))
                .andExpect(jsonPath("$.createdBy",equalTo(response.getCreatedBy())))
                .andExpect(jsonPath("$.registerDate",equalTo(response.getRegisterDate().toString())))
                .andExpect(jsonPath("$.lastLogin",equalTo(response.getLastLogin())))
                .andExpect(jsonPath("$.role",equalTo(response.getRole().toString())));
    }

    @Test
    @DisplayName("Should return events of the specified user")
    void shouldReturnSpecifiedUserEvents() throws Exception {
        MyEventResponse eventResponse = MyEventResponse.builder()
                .eventId(TEST_EVENT_ID)
                .eventBody(TEST_EVENT_BODY)
                .eventDescription(TEST_EVENT_DESCRIPTION)
                .eventExpiration(TEST_EVENT_EXPIRATION)
                .eventDateTime(TEST_EVENT_DATE_TIME)
                .build();
        MyEventResponse eventResponse2 = MyEventResponse.builder()
                .eventId(TEST_EVENT_ID)
                .eventBody(TEST_EVENT_BODY2)
                .eventDescription(TEST_EVENT_DESCRIPTION)
                .eventExpiration(TEST_EVENT_EXPIRATION)
                .eventDateTime(TEST_EVENT_DATE_TIME)
                .build();
        Set<EventResponseEntity> set =new HashSet<>();
        set.add(eventResponse);
        set.add(eventResponse2);
        when(userService.readUserEvents(any(UUID.class))).thenReturn(set);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}/events",TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", equalTo(set.size())));
    }
    @Test
    @DisplayName("Should return validation error messages")
    void shouldReturnValidationErrorMessages() throws Exception{
        final String INVALID_MIN_SIZE_TEST_FIRSTNAME = "foo";
        final String INVALID_MAX_SIZE_TEST_LASTNAME="barbarbarbarbarbarbarbarbarbarbarbarbarbarbarbarbar";
        final String INVALID_FORM_TEST_EMAIL="invalid email";
        final String INVALID_TEST_PASSWORD="1234";
        final String INVALID_TEST_ROLE = "anything_other";
        final String INVALID_FIRSTNAME_MESSAGE = "Firstname must be between 4 and 50 characters";
        final String INVALID_LASTNAME_MESSAGE = "Lastname must be between 4 and 50 characters";
        final String INVALID_EMAIL_MESSAGE ="Email must be in a normal email form";
        final String INVALID_PASSWORD_MESSAGE="Password must be at least 8 characters long and it contains at least one letter and one digit";
        final String INVALID_ROLE_MESSAGE = "Value is not well-formed";
        UserRequestEntity invalidRequest = UserRequestEntity.builder()
                .firstname(INVALID_MIN_SIZE_TEST_FIRSTNAME)
                .lastname(INVALID_MAX_SIZE_TEST_LASTNAME)
                .email(INVALID_FORM_TEST_EMAIL)
                .password(INVALID_TEST_PASSWORD)
                .currentProject(TEST_CURRENT_PROJECT)
                .specialization(TEST_SPECIALIZATION)
                .role(INVALID_TEST_ROLE)
                .group(TEST_GROUP_ID)
                .build();

        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                        .header("Authorization",TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstname",equalTo(INVALID_FIRSTNAME_MESSAGE)))
                .andExpect(jsonPath("$.lastname",equalTo(INVALID_LASTNAME_MESSAGE)))
                .andExpect(jsonPath("$.email",equalTo(INVALID_EMAIL_MESSAGE)))
                .andExpect(jsonPath("$.password",equalTo(INVALID_PASSWORD_MESSAGE)))
                .andExpect(jsonPath("$.role",equalTo(INVALID_ROLE_MESSAGE)));
    }
    @Test
    @DisplayName("Should return null error messages")
    void shouldReturnNullRoleErrorMessage() throws Exception{

        UserRequestEntity nullRequest = UserRequestEntity.builder()
                .build();
        final String NULL_FIRSTNAME_MESSAGE = "Firstname is required";
        final String NULL_LASTNAME_MESSAGE = "Lastname is required";
        final String NULL_EMAIL_MESSAGE ="Email is required";
        final String NULL_PASSWORD_MESSAGE="Password is required";
        final String NULL_ROLE_MESSAGE = "Role is required";
        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                        .header("Authorization",TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstname",equalTo(NULL_FIRSTNAME_MESSAGE)))
                .andExpect(jsonPath("$.lastname",equalTo(NULL_LASTNAME_MESSAGE)))
                .andExpect(jsonPath("$.email",equalTo(NULL_EMAIL_MESSAGE)))
                .andExpect(jsonPath("$.password",equalTo(NULL_PASSWORD_MESSAGE)))
                .andExpect(jsonPath("$.role",equalTo(NULL_ROLE_MESSAGE)));

    }
}
