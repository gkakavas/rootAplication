package com.example.app.controllers.leave;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.LeaveController;
import com.example.app.entities.LeaveType;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.services.LeaveService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {LeaveController.class, TestSecurityConfig.class, ApplicationExceptionHandler.class})
public class LeaveControllerTest {
    @MockBean
    private LeaveService leaveService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private User currentUser;
    private String roleValue;
    void setUp(){
        this.currentUser = Instancio.of(User.class)
                .set(field(User::getRole), Role.valueOf(roleValue))
                .create();
    }
    @Test
    @DisplayName("Should create a leave request")
    void shouldCreateALeaveRequest() throws Exception {
        LeaveRequestEntity request = Instancio.create(LeaveRequestEntity.class);
        LeaveResponseEntity response = MyLeaveResponse.builder()
                .leaveId(UUID.randomUUID())
                .leaveType(LeaveType.valueOf(request.getLeaveType()))
                .leaveStarts(request.getLeaveStarts())
                .leaveEnds(request.getLeaveEnds())
                .build();
        when(leaveService.create(request,any(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/leave/create")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should return a specific leave")
    void shouldReturnASpecificLeave() throws Exception {
        AdminHrMngLeaveResponse response = Instancio.create(AdminHrMngLeaveResponse.class);
        when(leaveService.read(response.getLeaveId(),any(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/leave/{id}",response.getLeaveId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should return all leaves")
    void shouldReturnAllLeaves() throws Exception {
        List<LeaveResponseEntity> response = List.copyOf(Instancio.createList(AdminHrMngLeaveResponse.class));
        when(leaveService.read(any(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/leave/all"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should update a specific leave")
    void shouldUpdateASpecificLeave() throws Exception {
        var request = Instancio.create(LeaveRequestEntity.class);
        MyLeaveResponse response = MyLeaveResponse.builder()
                .leaveId(UUID.randomUUID())
                .leaveType(LeaveType.valueOf(request.getLeaveType()))
                .leaveStarts(request.getLeaveStarts())
                .leaveEnds(request.getLeaveEnds())
                .build();
        when(leaveService.update(response.getLeaveId(),request)).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.put("/leave/update/{id}",response.getLeaveId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should delete a specific leave")
    void shouldDeleteASpecificLeave() throws Exception {
        var uuidOfLeaveToDelete = UUID.randomUUID();
        when(leaveService.delete(uuidOfLeaveToDelete)).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/leave/delete/{id}",uuidOfLeaveToDelete.toString())
                        .header("Authorization","testToken"))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","MANAGER"})
    @DisplayName("Should approve a specific leave")
    void shouldApproveASpecificLeave(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        AdminHrMngLeaveResponse response = Instancio.of(AdminHrMngLeaveResponse.class)
                .set(field("approvedBy"),currentUser.getEmail())
                .set(field("approvedOn"), LocalDateTime.now())
                .set(field("approved"),true)
                .create();
        when(leaveService.approveLeave(response.getLeaveId(),any(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/leave/approval/{id}",response.getLeaveId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }


}
