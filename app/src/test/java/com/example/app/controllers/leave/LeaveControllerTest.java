package com.example.app.controllers.leave;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.LeaveController;
import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.services.LeaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {LeaveController.class, TestSecurityConfig.class, ApplicationExceptionHandler.class})
public class LeaveControllerTest {
    @MockBean
    private LeaveService leaveService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create a leave request")
    void shouldCreateALeaveRequest() throws Exception {
        LeaveRequestEntity request = Instancio.create(LeaveRequestEntity.class);
        LeaveResponseEntity response = MyLeaveResponse.builder()
                .leaveId(UUID.randomUUID())
                .leaveType(request.getLeaveType())
                .leaveStarts(request.getLeaveStarts())
                .leaveEnds(request.getLeaveEnds())
                .build();
        when(leaveService.create(any(LeaveRequestEntity.class),any(String.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/leave/create")
                        .header("Authorization","testToken")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should return a specific leave")
    void shouldReturnASpecificLeave() throws Exception {
        LeaveResponseEntity response = Instancio.create(AdminHrMngLeaveResponse.class);
        when(leaveService.read(any(UUID.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/leave/{id}",UUID.randomUUID())
                        .header("Authorization","testToken"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should return all leaves")
    void shouldReturnAllLeaves() throws Exception {
        List<LeaveResponseEntity> response = List.copyOf(Instancio.createList(AdminHrMngLeaveResponse.class));
        when(leaveService.read()).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/leave/")
                        .header("Authorization","testToken"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should update a specific leave")
    void shouldUpdateASpecificLeave() throws Exception {
        var leaveToUpdate = Instancio.create(Leave.class);
        var request = Instancio.create(LeaveRequestEntity.class);
        LeaveResponseEntity response = MyLeaveResponse.builder()
                .leaveId(leaveToUpdate.getLeaveId())
                .leaveType(request.getLeaveType())
                .leaveStarts(request.getLeaveStarts())
                .leaveEnds(request.getLeaveEnds())
                .approvedBy("user with this id " + leaveToUpdate.getApprovedBy())
                .approvedOn(leaveToUpdate.getApprovedOn())
                .approved(leaveToUpdate.isApproved())
                .build();
        when(leaveService.update(any(UUID.class),any(LeaveRequestEntity.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/leave/update/{id}",UUID.randomUUID())
                        .header("Authorization","testToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should delete a specific leave")
    void shouldDeleteASpecificLeave() throws Exception {
        when(leaveService.delete(any(UUID.class))).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/leave/delete/{id}",UUID.randomUUID())
                        .header("Authorization","testToken"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should approve a specific leave")
    void shouldApproveASpecificLeave() throws Exception {
        var currentUser = Instancio.create(User.class);
        LeaveResponseEntity response = Instancio.of(AdminHrMngLeaveResponse.class)
                .set(field("approvedBy"),currentUser.getEmail())
                .set(field("approvedOn"), LocalDateTime.now())
                .set(field("approved"),true)
                .create();
        when(leaveService.approveLeave(any(UUID.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/leave/approval/{id}",UUID.randomUUID())
                        .header("Authorization","testToken"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

    }


}
