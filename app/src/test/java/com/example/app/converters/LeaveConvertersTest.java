package com.example.app.converters;

import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.leave.EntityResponseLeaveConverter;
import com.example.app.utils.leave.EntityResponseLeaveConverterImpl;
import com.example.app.utils.user.EntityResponseUserConverter;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LeaveConvertersTest {
    @InjectMocks
    private EntityResponseLeaveConverterImpl leaveConverter;
    @Mock
    private EntityResponseUserConverter userConverter;
    @Mock
    private UserRepository userRepo;
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        leaveConverter = new EntityResponseLeaveConverterImpl(userConverter,userRepo);
    }

    @Test
    @DisplayName("Should convert a leave entity to AdminHrMngLeave response")
    void shouldConvertALeaveEntityToAdminHrMngLeaveResponse(){
        var leave = Instancio.create(Leave.class);
        var user = Instancio.create(User.class);
        var expectedResponse = AdminHrMngLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(user.getEmail())
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .requestedBy(leave.getRequestedBy().getEmail())
                .build();
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(user));
        var response = leaveConverter.fromLeaveToAdminHrMngLeave(leave);
        Assertions.assertEquals(expectedResponse,response);
    }
    @Test
    @DisplayName("Should convert a leave entity to MyLeave response")
    void shouldConvertALeaveEntityToMyLeaveResponse() {
        var leave = Instancio.create(Leave.class);
        var user = Instancio.create(User.class);
        var expectedResponse = MyLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(user.getEmail())
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .build();
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(user));
        var response = leaveConverter.fromLeaveToMyLeave(leave);
        Assertions.assertEquals(expectedResponse,response);
    }
    @Test
    @DisplayName("Should convert a leave entity list to adminHrMng response list")
    void shouldConvertALeaveEntityListToAdminHrMngResponseList() {
        var leaveSet = Instancio.createSet(Leave.class);
            var expectedResponse = leaveSet.stream().map(leave ->
                            AdminHrMngLeaveResponse.builder()
                                    .leaveId(leave.getLeaveId())
                                    .leaveType(leave.getLeaveType())
                                    .leaveStarts(leave.getLeaveStarts())
                                    .leaveEnds(leave.getLeaveEnds())
                                    .approvedBy(leave.getApprovedBy().toString())
                                    .approvedOn(leave.getApprovedOn())
                                    .approved(leave.isApproved())
                                    .requestedBy(leave.getRequestedBy().getEmail())
                                    .build())
                    .toList();

        for(Leave leave:leaveSet){
            when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(Instancio.of(User.class)
                    .set(field("userId"),leave.getApprovedBy())
                    .create()));
        }
        var response = leaveConverter.fromLeaveListToAdminHrMngLeaveList(leaveSet);
        Assertions.assertEquals(expectedResponse,response);
    }


}
