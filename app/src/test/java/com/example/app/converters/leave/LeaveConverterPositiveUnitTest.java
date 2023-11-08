package com.example.app.converters.leave;

import com.example.app.entities.Leave;
import com.example.app.entities.LeaveType;
import com.example.app.entities.User;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.leave.EntityResponseLeaveConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
public class LeaveConverterPositiveUnitTest {
    @InjectMocks
    private EntityResponseLeaveConverterImpl leaveConverter;
    @Mock
    private UserRepository userRepo;
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        leaveConverter = new EntityResponseLeaveConverterImpl(userRepo);
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
  /*  @Test
    @DisplayName("Should convert a leave entity list to adminHrMng response list")
    void shouldConvertALeaveEntityListToAdminHrMngResponseList() {
        var leaveSet = Instancio.createSet(Leave.class);
        List<AdminHrMngLeaveResponse> expectedResponse = leaveSet.stream().map(leave ->
                AdminHrMngLeaveResponse.builder()
                        .leaveId(leave.getLeaveId())
                        .leaveType(leave.getLeaveType())
                        .leaveStarts(leave.getLeaveStarts())
                        .leaveEnds(leave.getLeaveEnds())
                        .approvedBy(leave.getApprovedBy().toString())
                        .approvedOn(leave.getApprovedOn())
                        .approved(leave.isApproved())
                        .requestedBy(leave.getRequestedBy().getEmail())
                        .build()
                )
                .toList();

            when(leaveConverter.fromLeaveToAdminHrMngLeave(???)).thenReturn(???);
        var response = leaveConverter.fromLeaveListToAdminHrMngLeaveList(leaveSet);
        Assertions.assertEquals(expectedResponse,response);
    }*/

    @Test
    @DisplayName("Should convert a leave request to leave entity")
    void shouldConvertALeaveRequestToLeaveEntity() {
        var request = Instancio.create(LeaveRequestEntity.class);
        var currentUser = Instancio.create(User.class);
        var expectedResponse = Leave.builder()
                .leaveType(LeaveType.valueOf(request.getLeaveType()))
                .leaveStarts(LocalDate.parse(request.getLeaveStarts()))
                .leaveEnds(LocalDate.parse(request.getLeaveEnds()))
                .approvedBy(null)
                .approvedOn(null)
                .approved(false)
                .requestedBy(currentUser)
                .build();
        var response = leaveConverter.fromRequestToEntity(request,currentUser);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should update a leave entity from given request")
    void shouldUpdateALeaveEntityFromGivenRequest() {
        var leave = Instancio.create(Leave.class);
        var request = Instancio.create(LeaveRequestEntity.class);
        var expectedResponse = Leave.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(LeaveType.valueOf(request.getLeaveType()))
                .leaveStarts(LocalDate.parse(request.getLeaveStarts()))
                .leaveEnds(LocalDate.parse(request.getLeaveEnds()))
                .approvedBy(leave.getApprovedBy())
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .requestedBy(leave.getRequestedBy())
                .build();
        var response = leaveConverter.updateLeave(request,leave);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should approve a leave entity")
    void shouldApproveALeaveEntity() {
        var leave = Instancio.of(Leave.class)
                .ignore(field("approvedBy"))
                .ignore(field("approvedOn"))
                .set(field("approved"),false)
                .create();
        var currentUser = Instancio.create(User.class);
        var expectedResponse = Leave.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(currentUser.getUserId())
                .approvedOn(LocalDateTime.now())
                .approved(true)
                .requestedBy(leave.getRequestedBy())
                .build();
        var response = leaveConverter.approveLeave(leave,currentUser);
        Assertions.assertEquals(expectedResponse,response);
    }

}
