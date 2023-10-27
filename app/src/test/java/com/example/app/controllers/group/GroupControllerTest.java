package com.example.app.controllers.group;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.GroupController;
import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.services.GroupService;
import com.example.app.utils.group.EntityResponseGroupConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestSecurityConfig.class, GroupController.class, ApplicationExceptionHandler.class})
public class GroupControllerTest {
    @MockBean
    private GroupService groupService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final GroupRequestEntity groupRequest = Instancio.of(GroupRequestEntity.class)
            .generate(field("idsSet"),gen -> gen.collection().size(40))
            .create();
    private static final User user = Instancio.of(User.class)
            .create();
    private static final String testToken = "testToken";
    private static final LocalDateTime groupCreationDate = LocalDateTime.of(2023,8,28,17,0,2);
    private static final AdminGroupResponse response = AdminGroupResponse.builder()
            .groupId(UUID.randomUUID())
            .groupName(groupRequest.getGroupName())
            .groupCreationDate(groupCreationDate)
            .groupCreator(user.getEmail())
            .users(groupRequest.getIdsSet().stream()
                    .map(uuid -> Instancio.of(AdminUserResponse.class)
                            .set(field("userId"),uuid)
                            .ignore(all(field("registerDate"), field("lastLogin")))
                            .create()).collect(Collectors.toSet()))
            .build();
    @Test
    @DisplayName("Should create a new group")
    void shouldCreateANewGroup() throws Exception {
        when(groupService.create(any(GroupRequestEntity.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/group/create")
                        .header("Authorization",testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId",equalTo(response.getGroupId().toString())))
                .andExpect(jsonPath("$.groupName",equalTo(response.getGroupName())))
                .andExpect(jsonPath("$.groupCreationDate",equalTo(response.getGroupCreationDate().toString())))
                .andExpect(jsonPath("$.groupCreator",equalTo(response.getGroupCreator())))
                .andExpect(jsonPath("$.users.size()",equalTo(response.getUsers().size())));
    }

    @Test
    @DisplayName("Should return a specified group")
    void shouldReturnASpecifiedGroup() throws Exception {
        when(groupService.read(any(UUID.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/group/{id}",UUID.randomUUID())
                        .header("Authorization",testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId",equalTo(response.getGroupId().toString())))
                .andExpect(jsonPath("$.groupName",equalTo(response.getGroupName())))
                .andExpect(jsonPath("$.groupCreationDate",equalTo(response.getGroupCreationDate().toString())))
                .andExpect(jsonPath("$.groupCreator",equalTo(response.getGroupCreator())))
                .andExpect(jsonPath("$.users.size()",equalTo(response.getUsers().size())));
    }

    @Test
    @DisplayName("Should return all groups")
    void shouldReturnAllGroups() throws Exception {
        List<GroupResponseEntity> responseList = new ArrayList<>(Instancio.ofList(AdminGroupResponse.class)
                        .size(5)
                        .create());
        when(groupService.read()).thenReturn(responseList);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/group/")
                        .header("Authorization",testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",equalTo(responseList.size())))
                .andExpect(jsonPath("$.size()",greaterThan(0)));
    }

    @Test
    @DisplayName("Should update a specified group")
    void shouldUpdateASpecifiedGroup() throws Exception {
        when(groupService.update(any(UUID.class),any(GroupRequestEntity.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/group/update/{id}",UUID.randomUUID())
                        .header("Authorization",testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId",equalTo(response.getGroupId().toString())))
                .andExpect(jsonPath("$.groupName",equalTo(response.getGroupName())))
                .andExpect(jsonPath("$.groupCreationDate",equalTo(response.getGroupCreationDate().toString())))
                .andExpect(jsonPath("$.groupCreator",equalTo(response.getGroupCreator())))
                .andExpect(jsonPath("$.users.size()",equalTo(response.getUsers().size())));
    }

    @Test
    @DisplayName("Should delete a specified group")
    void shouldDeleteASpecifiedGroup() throws Exception {
        when(groupService.update(any(UUID.class),any(GroupRequestEntity.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/group/delete/{id}",UUID.randomUUID())
                        .header("Authorization",testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}
