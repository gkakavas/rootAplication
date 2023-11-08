package com.example.app.services.leave;

import com.example.app.entities.Leave;
import com.example.app.entities.LeaveType;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.repositories.LeaveRepository;
import com.example.app.services.LeaveService;
import com.example.app.tool.LeaveRelevantGenerator;
import com.example.app.utils.leave.EntityResponseLeaveConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
public class LeaveServicePositiveUnitTest {
    @InjectMocks
    private LeaveService leaveService;
    @Mock
    private LeaveRepository leaveRepo;
    @Mock
    private EntityResponseLeaveConverterImpl leaveConverter;
    private User currentUser;
    private Principal principal;
    private String roleValue;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        leaveService = new LeaveService(leaveRepo, leaveConverter);
    }
    void setUpCurrentUser(){
        currentUser = Instancio.of(User.class)
                .set(field(User::getRole),Role.valueOf(roleValue))
                .create();
    }
    @AfterEach

    void tearDown(){
    }
    @Test
    @DisplayName("Should create and store in database a new leave request")
    void shouldCreateAndStoreInDatabaseANewLeaveRequest() {
        this.roleValue = "USER";
        setUpCurrentUser();
        var request = LeaveRelevantGenerator.generateValidLeaveRequestEntity();
        var createdLeave = Leave.builder()
                .leaveId(UUID.randomUUID()).leaveType(LeaveType.valueOf(request.getLeaveType()))
                .leaveStarts(LocalDate.parse(request.getLeaveStarts())).leaveEnds(LocalDate.parse(request.getLeaveEnds()))
                .requestedBy(currentUser)
                .build();
        var expectedResponse = MyLeaveResponse.builder().build();
        when(leaveConverter.fromRequestToEntity(request, currentUser)).thenReturn(createdLeave);
        when(leaveRepo.save(createdLeave)).thenReturn(createdLeave);
        when(leaveConverter.fromLeaveToMyLeave(createdLeave)).thenReturn(expectedResponse);
        var response = leaveService.create(request,currentUser);
        assertEquals(expectedResponse,response);
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("Should return a specific leave")
    void shouldReturnASpecificLeave(String roleValue) throws LeaveNotFoundException {
        this.roleValue = roleValue;
        setUpCurrentUser();
        var leave = Instancio.create(Leave.class);
        when(leaveRepo.findById(leave.getLeaveId())).thenReturn(Optional.of(leave));
        var expectedAdminHrMngResponse = AdminHrMngLeaveResponse.builder()
                .leaveId(leave.getLeaveId()).leaveType(leave.getLeaveType()).leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds()).approvedBy("user with id " + leave.getApprovedBy()).approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved()).requestedBy(leave.getRequestedBy().getEmail())
                .build();
        var expectedUserResponse = MyLeaveResponse.builder()
                .leaveId(leave.getLeaveId()).leaveType(leave.getLeaveType()).leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds()).approvedBy("user with id " + leave.getApprovedBy())
                .approvedOn(leave.getApprovedOn()).approved(leave.isApproved())
                .build();
        if(List.of(Role.ADMIN,Role.HR).contains(currentUser.getRole())){
            when(leaveConverter.fromLeaveToAdminHrMngLeave(leave)).thenReturn(expectedAdminHrMngResponse);
            var response = leaveService.read(leave.getLeaveId(),this.currentUser);
            assertEquals(expectedAdminHrMngResponse,response);
        }
        else if(currentUser.getRole().equals(Role.MANAGER)){
            leave.getRequestedBy().setGroup(currentUser.getGroup());
            when(leaveConverter.fromLeaveToAdminHrMngLeave(leave)).thenReturn(expectedAdminHrMngResponse);
            var response = leaveService.read(leave.getLeaveId(),this.currentUser);
            assertEquals(expectedAdminHrMngResponse,response);
        }
        else if (currentUser.getRole().equals(Role.USER)) {
            leave.setRequestedBy(currentUser);
            when(leaveConverter.fromLeaveToMyLeave(leave)).thenReturn(expectedUserResponse);
            var response = leaveService.read(leave.getLeaveId(),this.currentUser);
            assertEquals(expectedUserResponse,response);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("Should return all leaves based on current user")
    void shouldReturnAllLeavesForAdmin(String roleValue) {
        this.roleValue = roleValue;
        setUpCurrentUser();
        var leaves = Instancio.createList(Leave.class);
        if(List.of(Role.ADMIN,Role.HR).contains(currentUser.getRole())) {
            var adminHrResponse = leaves.stream().map(leave -> (LeaveResponseEntity) AdminHrMngLeaveResponse.builder()
                    .leaveId(leave.getLeaveId()).leaveType(leave.getLeaveType()).leaveStarts(leave.getLeaveStarts())
                    .leaveEnds(leave.getLeaveEnds()).approvedBy("user with id " + leave.getApprovedBy())
                    .approvedOn(leave.getApprovedOn()).approved(true).requestedBy(leave.getRequestedBy().getEmail())
                    .build()
            ).toList();
            when(leaveRepo.findAll()).thenReturn(leaves);
            when(leaveConverter.fromLeaveListToAdminHrMngLeaveList(Set.copyOf(leaves))).thenReturn(adminHrResponse);
            var response = leaveService.read(this.currentUser);
            assertEquals(adminHrResponse,response);
        }
        else if (currentUser.getRole().equals(Role.MANAGER)) {
            var managerResponse = leaves.stream().map(leave -> {
                leave.getRequestedBy().setGroup(currentUser.getGroup());
                return (LeaveResponseEntity) AdminHrMngLeaveResponse.builder()
                        .leaveId(leave.getLeaveId()).leaveType(leave.getLeaveType()).leaveStarts(leave.getLeaveStarts())
                        .leaveEnds(leave.getLeaveEnds()).approvedBy("user with id " + leave.getApprovedBy())
                        .approvedOn(leave.getApprovedOn()).approved(true).requestedBy(leave.getRequestedBy().getEmail())
                        .build();
            }
            ).toList();
            when(leaveRepo.findAllByRequestedBy_Group(currentUser.getGroup())).thenReturn(leaves);
            when(leaveConverter.fromLeaveListToAdminHrMngLeaveList(Set.copyOf(leaves))).thenReturn(managerResponse);
            var response = leaveService.read(this.currentUser);
            assertEquals(managerResponse, response);
        }
        else {
            var userResponse = currentUser.getUserRequestedLeaves().stream().map(leave -> (LeaveResponseEntity) MyLeaveResponse.builder()
                    .leaveId(leave.getLeaveId()).leaveType(leave.getLeaveType()).leaveStarts(leave.getLeaveStarts())
                    .leaveEnds(leave.getLeaveEnds()).approvedBy("user with id " + leave.getApprovedBy())
                    .approvedOn(leave.getApprovedOn()).approved(true)
                    .build()
            ).toList();
            when(leaveConverter.fromLeaveListToMyLeaveList(currentUser.getUserRequestedLeaves())).thenReturn(userResponse);
            var response = leaveService.read(this.currentUser);
            assertEquals(userResponse,response);
        }
    }
    @Test
    @DisplayName("Should update and store in database an existing leave")
    void shouldUpdateAndStoreInDatabaseAnExistingLeave() throws LeaveNotFoundException {
        var request = LeaveRelevantGenerator.generateValidLeaveRequestEntity();
        var leaveToUpdate = Instancio.create(Leave.class);
        var updatedLeave = Leave.builder()
                .leaveId(leaveToUpdate.getLeaveId()).leaveType(LeaveType.valueOf(request.getLeaveType()))
                .leaveStarts(LocalDate.parse(request.getLeaveStarts())).leaveEnds(LocalDate.parse(request.getLeaveEnds()))
                .approvedBy(leaveToUpdate.getApprovedBy()).approvedOn(leaveToUpdate.getApprovedOn())
                .approved(leaveToUpdate.isApproved()).requestedBy(leaveToUpdate.getRequestedBy())
                .build();
        var expectedResponse = MyLeaveResponse.builder()
                .leaveId(updatedLeave.getLeaveId()).leaveType(updatedLeave.getLeaveType())
                .leaveStarts(updatedLeave.getLeaveStarts()).leaveEnds(updatedLeave.getLeaveEnds())
                .approvedBy("user with this id" + updatedLeave.getApprovedBy()).approvedOn(updatedLeave.getApprovedOn())
                .approved(updatedLeave.isApproved())
                .build();
        when(leaveRepo.findById(leaveToUpdate.getLeaveId())).thenReturn(Optional.of(leaveToUpdate));
        when(leaveConverter.updateLeave(request,leaveToUpdate)).thenReturn(updatedLeave);
        when(leaveRepo.save(updatedLeave)).thenReturn(updatedLeave);
        when(leaveConverter.fromLeaveToMyLeave(updatedLeave)).thenReturn(expectedResponse);
        var response = leaveService.update(leaveToUpdate.getLeaveId(),request);
        assertEquals(expectedResponse,response);
    }
    @Test
    @DisplayName("Should delete an existing leave from database ")
    void shouldDeleteAnExistingLeaveFromDatabase() throws LeaveNotFoundException {
        var leaveToDelete = Instancio.create(Leave.class);
        when(leaveRepo.findById(leaveToDelete.getLeaveId())).thenReturn(Optional.of(leaveToDelete));
        when(leaveRepo.existsById(leaveToDelete.getLeaveId())).thenReturn(false);
        var response = leaveService.delete(leaveToDelete.getLeaveId());
        Assertions.assertTrue(response);
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","MANAGER"})
    @DisplayName("Should approve and store an existing leave in database ")
    void shouldApproveAndStoreAnExistingLeaveInDatabase(String roleValue) throws LeaveNotFoundException, UserNotFoundException {
        this.roleValue = roleValue;
        setUpCurrentUser();
        var leaveToApprove = Instancio.of(Leave.class)
                .set(field("approvedBy"),null)
                .set(field("approvedOn"),null)
                .set(field("approved"),false)
                .create();
        var approvedLeave = Leave.builder()
                .leaveId(leaveToApprove.getLeaveId()).leaveType(leaveToApprove.getLeaveType())
                .leaveStarts(leaveToApprove.getLeaveStarts()).leaveEnds(leaveToApprove.getLeaveEnds())
                .approvedBy(currentUser.getUserId()).approvedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .approved(true).requestedBy(leaveToApprove.getRequestedBy())
                .build();
        var expectedResponse = AdminHrMngLeaveResponse.builder()
                .leaveId(approvedLeave.getLeaveId()).leaveType(approvedLeave.getLeaveType())
                .leaveStarts(approvedLeave.getLeaveStarts()).leaveEnds(approvedLeave.getLeaveEnds())
                .approvedBy("user with this id" + approvedLeave.getApprovedBy()).approvedOn(approvedLeave.getApprovedOn())
                .approved(approvedLeave.isApproved()).requestedBy(approvedLeave.getRequestedBy().getEmail())
                .build();
        when(leaveRepo.findById(leaveToApprove.getLeaveId())).thenReturn(Optional.of(leaveToApprove));
        when(leaveConverter.approveLeave(leaveToApprove,currentUser)).thenReturn(approvedLeave);
        when(leaveRepo.save(approvedLeave)).thenReturn(approvedLeave);
        when(leaveConverter.fromLeaveToAdminHrMngLeave(approvedLeave)).thenReturn(expectedResponse);
        var response = leaveService.approveLeave(leaveToApprove.getLeaveId(),this.currentUser);
        assertEquals(expectedResponse,response);
    }

}
