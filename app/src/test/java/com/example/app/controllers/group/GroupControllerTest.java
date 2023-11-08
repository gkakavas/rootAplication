package com.example.app.controllers.group;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.GroupController;
import com.example.app.entities.User;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.services.GroupService;
import com.example.app.tool.GroupRelevantGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ActiveProfiles("unit")
@WebMvcTest
@ContextConfiguration(classes = {TestSecurityConfig.class, GroupController.class, ApplicationExceptionHandler.class})
public class GroupControllerTest {
    @MockBean
    private GroupService groupService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    @DisplayName("Should create a new group")
    void shouldCreateANewGroup() throws Exception {
        var request = GroupRelevantGenerator.generateGroupRequestEntity(Instancio.createList(User.class));
        var response = AdminGroupResponse.builder()
                .groupId(UUID.randomUUID())
                .groupName(request.getGroupName())
                .groupCreator(Instancio.create(String.class))
                .groupCreationDate(LocalDateTime.now())
                .users(Instancio.ofSet(AdminUserResponse.class).size(request.getIdsSet().size()).create())
                .build();
        when(groupService.create(eq(request),nullable(User.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should return a specified group")
    void shouldReturnASpecifiedGroup() throws Exception {
        var response = Instancio.create(AdminGroupResponse.class);
        when(groupService.read(eq(response.getGroupId()),nullable(User.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/group/{id}",response.getGroupId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should return all groups")
    void shouldReturnAllGroups() throws Exception {
        var response = Instancio.createList(AdminGroupResponse.class);
        when(groupService.read(nullable(User.class))).thenReturn(List.copyOf(response));
        this.mockMvc.perform(MockMvcRequestBuilders.get("/group/all"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should update a specified group")
    void shouldUpdateASpecifiedGroup() throws Exception {
        var request = Instancio.create(GroupRequestEntity.class);
        var response = AdminGroupResponse.builder()
                .groupId(UUID.randomUUID())
                .groupName(request.getGroupName())
                .groupCreator(Instancio.create(String.class))
                .groupCreationDate(LocalDateTime.now())
                .users(Instancio.ofSet(AdminUserResponse.class).size(request.getIdsSet().size()).create())
                .build();
        when(groupService.update(eq(response.getGroupId()),eq(request))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/group/update/{id}",response.getGroupId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should delete a specified group")
    void shouldDeleteASpecifiedGroup() throws Exception {
        var userIdToDelete = UUID.randomUUID();
        when(groupService.delete(eq(userIdToDelete))).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/group/delete/{id}",userIdToDelete.toString()))
                .andExpect(status().isNoContent());
    }

}
