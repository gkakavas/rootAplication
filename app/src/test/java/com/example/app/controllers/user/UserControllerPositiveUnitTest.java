package com.example.app.controllers.user;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.UserController;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.ChangePasswordRequest;
import com.example.app.models.responses.ChangePasswordResponse;
import com.example.app.models.responses.event.AdminHrMngEventResponse;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.services.UserService;
import com.example.app.tool.UserRelevantGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("unit")
@WebMvcTest
@ContextConfiguration(classes = {TestSecurityConfig.class, UserController.class, ApplicationExceptionHandler.class})
public class UserControllerPositiveUnitTest {
    @MockBean
    private UserService userService;
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

    @Test
    @DisplayName("Should create a new user and return this")
    void shouldCreateANewUserAndReturnThisBack() throws Exception {
        var request = UserRelevantGenerator.generateValidUserRequestEntity("USER", UUID.randomUUID());
        this.roleValue = "ADMIN";
        setUp();
        var expectedResponse = AdminUserResponse.builder()
                .userId(UUID.randomUUID()).firstname(request.getFirstname()).lastname(request.getLastname())
                .email(request.getEmail()).specialization(request.getSpecialization()).currentProject(request.getCurrentProject())
                .groupName("group with id " + request.getGroup()).createdBy(currentUser.getEmail())
                .registerDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).lastLogin(null).role(Role.valueOf(request.getRole()))
                .build();
        when(userService.create(eq(request), nullable(Principal.class))).thenReturn(expectedResponse);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "MANAGER", "USER"})
    @DisplayName("Should return the specific user")
    void shouldReturnASpecificUser(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var adminResponse = Instancio.create(AdminUserResponse.class);
        var otherUserResponse = Instancio.create(OtherUserResponse.class);
        if (roleValue.equals("ADMIN")) {
            when(userService.read(eq(adminResponse.getUserId()), nullable(Principal.class))).thenReturn(adminResponse);
            this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}", adminResponse.getUserId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(adminResponse)));
        } else {
            when(userService.read(eq(otherUserResponse.getUserId()), nullable(Principal.class))).thenReturn(otherUserResponse);
            this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}", otherUserResponse.getUserId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(otherUserResponse)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "MANAGER", "USER"})
    @DisplayName("Should return all users")
    void shouldReturnAllUsers(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var adminResponse = Instancio.createList(AdminUserResponse.class);
        var otherUserResponse = Instancio.createList(OtherUserResponse.class);
        if (roleValue.equals("ADMIN")) {
            when(userService.read(nullable(Principal.class))).thenReturn(List.copyOf(adminResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/user/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(adminResponse)));
        } else {
            when(userService.read(nullable(Principal.class))).thenReturn(List.copyOf(otherUserResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/user/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(otherUserResponse)));
        }
    }

    @Test
    @DisplayName("ShouldUpdateAUser")
    void shouldUpdateAUser() throws Exception {
        var request = UserRelevantGenerator.generateValidUserRequestEntity("MANAGER", UUID.randomUUID());
        var response = AdminUserResponse.builder()
                .userId(UUID.randomUUID()).firstname(request.getFirstname()).lastname(request.getLastname())
                .email(request.getEmail()).specialization(request.getSpecialization()).currentProject(request.getCurrentProject())
                .groupName("group with id" + request.getGroup()).registerDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .lastLogin(null).role(Role.valueOf(request.getRole()))
                .build();
        this.roleValue = "ADMIN";
        setUp();
        when(userService.update(response.getUserId(), request)).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/user/update/{id}", response.getUserId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should delete a specific user")
    void shouldDeleteAUser() throws Exception {
        UUID userIdToDelete = UUID.randomUUID();
        when(userService.delete(userIdToDelete)).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/user/delete/{id}", userIdToDelete.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should patch a specific user")
    void shouldPatchAUser() throws Exception {
        var request = new HashMap<String, String>();
        request.put("firstname", "testFirstname");
        request.put("lastname", "testLastname");
        request.put("email", "test@email.com");
        request.put("specialization", "testSpecialization");
        request.put("currentProject", "testCurrentProject");
        request.put("role", "USER");
        request.put("group", UUID.randomUUID().toString());
        var response = AdminUserResponse.builder()
                .userId(UUID.randomUUID()).firstname(request.get("firstname")).lastname(request.get("lastname"))
                .email(request.get("email")).specialization(request.get("specialization"))
                .currentProject(request.get("currentProject")).groupName("group with id " + request.get("group"))
                .createdBy(Instancio.create(String.class)).registerDate(Instancio.create(LocalDateTime.class))
                .lastLogin(Instancio.create(LocalDateTime.class)).role(Role.valueOf(request.get("role")))
                .build();
        when(userService.patch(response.getUserId(), request)).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/user/patch/{id}", response.getUserId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "MANAGER", "USER"})
    @DisplayName("Should return events of the specific user")
    void shouldReturnSpecificUserEvents(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var adminHrMngResponse = Instancio.createSet(AdminHrMngEventResponse.class);
        var userResponse = Instancio.createSet(MyEventResponse.class);
        if (List.of("ADMIN", "HR", "MANAGER").contains(roleValue)) {
            when(userService.readUserEvents(currentUser.getUserId())).thenReturn(Set.copyOf(adminHrMngResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}/events", currentUser.getUserId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(adminHrMngResponse)));
        } else {
            when(userService.readUserEvents(currentUser.getUserId())).thenReturn(Set.copyOf(userResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}/events", currentUser.getUserId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(userResponse)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "MANAGER", "USER"})
    @DisplayName("Should change current user password")
    void shouldChangeSpecificUserPassword(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var request = ChangePasswordRequest.builder()
                .oldPassword("testPass1")
                .newPassword("newTestPassword123")
                .confirmationNewPassword("newTestPassword123")
                .build();
        var response = Instancio.create(ChangePasswordResponse.class);
        when(userService.changePassword(eq(request),nullable(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/user/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}
