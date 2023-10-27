package com.example.app.services.leave;

import com.example.app.entities.Leave;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.common.UserWithLeaves;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.LeaveRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.example.app.services.LeaveService;
import com.example.app.utils.common.EntityResponseCommonConverter;
import com.example.app.utils.common.EntityResponseCommonConverterImpl;
import com.example.app.utils.leave.EntityResponseLeaveConverter;
import com.example.app.utils.leave.EntityResponseLeaveConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class LeaveServicePositiveUnitTest {
    @InjectMocks
    private LeaveService leaveService;
    @Mock
    private LeaveRepository leaveRepo;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private EntityResponseLeaveConverterImpl leaveConverter;
    @Mock
    private EntityResponseCommonConverterImpl commonConverter;
    private final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        leaveService = new LeaveService(leaveRepo, userRepo, leaveConverter, commonConverter);
        this.securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", List.of()));
        SecurityContextHolder.setContext(securityContext);
    }
    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }
    @Test
    @DisplayName("Should create and store in database a new leave request")
    void shouldCreateAndStoreInDatabaseANewLeaveRequest() throws UserNotFoundException {
        var currentUser = Instancio.create(User.class);
        var request = Instancio.create(LeaveRequestEntity.class);
        var leave = Instancio.of(Leave.class)
                .set(field("leaveType"),request.getLeaveType())
                .set(field("leaveStarts"),request.getLeaveStarts())
                .set(field("leaveEnds"),request.getLeaveEnds())
                .ignore(field("approvedBy"))
                .ignore(field("approvedOn"))
                .ignore(field("approved"))
                .set(field("requestedBy"),currentUser)
                .create();
        LeaveResponseEntity expectedResponse = Instancio.of(MyLeaveResponse.class)
                .set(field("leaveId"),leave.getLeaveId())
                .set(field("leaveType"),leave.getLeaveType())
                .set(field("leaveStarts"),leave.getLeaveStarts())
                .set(field("leaveEnds"),leave.getLeaveEnds())
                .ignore(field("approvedBy"))
                .ignore(field("approvedOn"))
                .ignore(field("approved"))
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(leaveConverter.fromRequestToEntity(request,currentUser)).thenReturn(leave);
        when(leaveRepo.save(leave)).thenReturn(leave);
        when(leaveConverter.fromLeaveToMyLeave(leave)).thenReturn(expectedResponse);
        var response = leaveService.create(request);
        Assertions.assertEquals(expectedResponse,response);
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("Should return a specific leave")
    void shouldReturnASpecificLeave(String roleValue) throws LeaveNotFoundException {
        var currentRole = Role.valueOf(roleValue);
        var currentUser = Instancio.of(User.class)
                .set(field("role"),currentRole)
                .create();
        var leave = Instancio.create(Leave.class);
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(leaveRepo.findById(any(UUID.class))).thenReturn(Optional.of(leave));
        var expectedAdminHrMngResponse = AdminHrMngLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy("user with this id " + leave.getApprovedBy())
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .requestedBy(leave.getRequestedBy().getEmail())
                .build();
        var expectedUserResponse = MyLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy("user with this id " + leave.getApprovedBy())
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .build();
        if(currentUser.getRole().equals(Role.ADMIN)||currentUser.getRole().equals(Role.HR)){
            when(leaveConverter.fromLeaveToAdminHrMngLeave(any(Leave.class))).thenReturn(expectedAdminHrMngResponse);
            var response = leaveService.read(UUID.randomUUID());
            Assertions.assertEquals(expectedAdminHrMngResponse,response);
        }
        else if(currentUser.getRole().equals(Role.MANAGER)){
            leave.getRequestedBy().setGroup(currentUser.getGroup());
            when(leaveConverter.fromLeaveToAdminHrMngLeave(any(Leave.class))).thenReturn(expectedAdminHrMngResponse);
            var response = leaveService.read(UUID.randomUUID());
            Assertions.assertEquals(expectedAdminHrMngResponse,response);
        }
        else if (currentUser.getRole().equals(Role.USER)) {
            leave.setRequestedBy(currentUser);
            when(leaveConverter.fromLeaveToMyLeave(leave)).thenReturn(expectedUserResponse);
            var response = leaveService.read(UUID.randomUUID());
            Assertions.assertEquals(expectedUserResponse,response);
        }
    }

    @Test
    @DisplayName("Should return all leaves in Admin-Role format")
    void shouldReturnAllLeavesForAdmin() {
        var currentUser = Instancio.of(User.class)
                .set(field("role"),Role.valueOf("ADMIN"))
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var users = Instancio.createSet(User.class);
        users.forEach(user -> user.getUserRequestedLeaves().forEach(leave -> leave.setRequestedBy(user)));
        Set<UserWithLeaves> expectedResponse = new HashSet<>();
        for (User user : users) {
            expectedResponse.add(UserWithLeaves.builder()
                    .user(AdminUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName())
                            .createdBy("user with this id" + user.getCreatedBy())
                            .registerDate(user.getRegisterDate())
                            .lastLogin(user.getLastLogin())
                            .role(user.getRole())
                            .build())
                    .leaves(user.getUserRequestedLeaves().stream().map(leave ->
                                    (LeaveResponseEntity) AdminHrMngLeaveResponse.builder()
                                            .leaveId(leave.getLeaveId())
                                            .leaveType(leave.getLeaveType())
                                            .leaveStarts(leave.getLeaveStarts())
                                            .leaveEnds(leave.getLeaveEnds())
                                            .approvedBy("user with this id" + leave.getApprovedBy())
                                            .approvedOn(leave.getApprovedOn())
                                            .approved(leave.isApproved())
                                            .requestedBy(leave.getRequestedBy().getEmail())
                                            .build()
                            )
                            .toList())
                    .build());
        }
            when(userRepo.findAll()).thenReturn(List.copyOf(users));
            when(commonConverter.usersWithLeaves(users)).thenReturn(expectedResponse);
            var response = leaveService.read();
            Assertions.assertEquals(List.copyOf(expectedResponse),response);
        }

    @Test
    @DisplayName("Should return all leaves in HR-Role format")
    void shouldReturnAllLeavesForHR() {
        var currentUser = Instancio.of(User.class)
                .set(field("role"),Role.valueOf("HR"))
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var users = Instancio.createSet(User.class);
        users.forEach(user -> user.getUserRequestedLeaves().forEach(leave -> leave.setRequestedBy(user)));
        Set<UserWithLeaves> expectedResponse = new HashSet<>();
        for (User user : users) {
            expectedResponse.add(UserWithLeaves.builder()
                    .user(OtherUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName())
                            .build())
                    .leaves(user.getUserRequestedLeaves().stream().map(leave ->
                                    (LeaveResponseEntity) AdminHrMngLeaveResponse.builder()
                                            .leaveId(leave.getLeaveId())
                                            .leaveType(leave.getLeaveType())
                                            .leaveStarts(leave.getLeaveStarts())
                                            .leaveEnds(leave.getLeaveEnds())
                                            .approvedBy("user with this id" + leave.getApprovedBy())
                                            .approvedOn(leave.getApprovedOn())
                                            .approved(leave.isApproved())
                                            .requestedBy(leave.getRequestedBy().getEmail())
                                            .build()
                            )
                            .toList())
                    .build());
        }
        when(userRepo.findAll()).thenReturn(List.copyOf(users));
        when(commonConverter.usersWithLeaves(users)).thenReturn(expectedResponse);
        var response = leaveService.read();
        Assertions.assertEquals(List.copyOf(expectedResponse),response);
    }

    @Test
    @DisplayName("Should return all leaves in Manager-Role format")
    void shouldReturnAllLeavesForManager() {
        var currentUser = Instancio.of(User.class)
                .set(field("role"),Role.valueOf("MANAGER"))
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var users = Instancio.createSet(User.class);
        users.forEach(user -> {
            user.getUserRequestedLeaves().forEach(leave -> leave.setRequestedBy(user));
            user.setGroup(currentUser.getGroup());
        });
        Set<UserWithLeaves> expectedResponse = new HashSet<>();
        for (User user : users) {
            expectedResponse.add(UserWithLeaves.builder()
                    .user(OtherUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName())
                            .build())
                    .leaves(user.getUserRequestedLeaves().stream().map(leave ->
                                    (LeaveResponseEntity) AdminHrMngLeaveResponse.builder()
                                            .leaveId(leave.getLeaveId())
                                            .leaveType(leave.getLeaveType())
                                            .leaveStarts(leave.getLeaveStarts())
                                            .leaveEnds(leave.getLeaveEnds())
                                            .approvedBy("user with this id" + leave.getApprovedBy())
                                            .approvedOn(leave.getApprovedOn())
                                            .approved(leave.isApproved())
                                            .requestedBy(leave.getRequestedBy().getEmail())
                                            .build()
                            )
                            .toList())
                    .build());
        }
        when(userRepo.findAllByGroup(currentUser.getGroup())).thenReturn(List.copyOf(users));
        when(commonConverter.usersWithLeaves(users)).thenReturn(expectedResponse);
        var response = leaveService.read();
        Assertions.assertEquals(List.copyOf(expectedResponse),response);
    }

    @Test
    @DisplayName("Should return all leaves of current user in User-Role format")
    void shouldReturnAllLeavesForUser() {
        var currentUser = Instancio.of(User.class)
                .set(field("role"),Role.valueOf("USER"))
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var expectedResponse = List.of(UserWithLeaves.builder()
                .user(OtherUserResponse.builder()
                        .userId(currentUser.getUserId())
                        .firstname(currentUser.getFirstname())
                        .lastname(currentUser.getLastname())
                        .email(currentUser.getEmail())
                        .specialization(currentUser.getSpecialization())
                        .currentProject(currentUser.getCurrentProject())
                        .groupName(currentUser.getGroup().getGroupName())
                        .build())
                .leaves(currentUser.getUserRequestedLeaves().stream().map(leave ->
                                (LeaveResponseEntity) MyLeaveResponse.builder()
                                        .leaveId(leave.getLeaveId())
                                        .leaveType(leave.getLeaveType())
                                        .leaveStarts(leave.getLeaveStarts())
                                        .leaveEnds(leave.getLeaveEnds())
                                        .approvedBy("user with this id" + leave.getApprovedBy())
                                        .approvedOn(leave.getApprovedOn())
                                        .approved(leave.isApproved())
                                        .build())
                        .toList())
                .build());
            when(leaveConverter.fromLeaveListToMyLeaveList(currentUser.getUserRequestedLeaves())).thenReturn(List.copyOf(expectedResponse));
            var response = leaveService.read();
            Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should update and store in database an existing leave")
    void shouldUpdateAndStoreInDatabaseAnExistingLeave() throws LeaveNotFoundException {
        var leave = Instancio.create(Leave.class);
        when(leaveRepo.findById(any(UUID.class))).thenReturn(Optional.of(leave));
        var updatedLeave = Instancio.of(Leave.class)
                .set(field("leaveId"), leave.getLeaveId())
                .create();
        when(leaveConverter.updateLeave(any(LeaveRequestEntity.class),any(Leave.class))).thenReturn(updatedLeave);
        when(leaveRepo.save(any(Leave.class))).thenReturn(updatedLeave);
        var expectedResponse = MyLeaveResponse.builder()
                .leaveId(updatedLeave.getLeaveId())
                .leaveType(updatedLeave.getLeaveType())
                .leaveStarts(updatedLeave.getLeaveStarts())
                .leaveEnds(updatedLeave.getLeaveEnds())
                .approvedBy("user with this id" + updatedLeave.getApprovedBy())
                .approvedOn(updatedLeave.getApprovedOn())
                .approved(updatedLeave.isApproved())
                .build();
        when(leaveConverter.fromLeaveToMyLeave(updatedLeave)).thenReturn(expectedResponse);
        var response = leaveService.update(UUID.randomUUID(),Instancio.create(LeaveRequestEntity.class));
        Assertions.assertEquals(expectedResponse,response);
    }
    @Test
    @DisplayName("Should delete an existing leave from database ")
    void shouldDeleteAnExistingLeaveFromDatabase() throws LeaveNotFoundException {
        when(leaveRepo.existsById(any(UUID.class))).thenReturn(true);
        var response = leaveService.delete(UUID.randomUUID());
        Assertions.assertTrue(response);
    }

    @Test
    @DisplayName("Should approve and store an existing leave in database ")
    void shouldApproveAndStoreAnExistingLeaveInDatabase() throws LeaveNotFoundException, UserNotFoundException {
        var leave = Instancio.of(Leave.class)
                .set(field("approvedBy"),null)
                .set(field("approvedOn"),null)
                .set(field("approved"),false)
                .create();
        var currentUser = Instancio.create(User.class);
        var patchedLeave = Leave.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(currentUser.getUserId())
                .approvedOn(LocalDateTime.now())
                .approved(true)
                .requestedBy(leave.getRequestedBy())
                .build();
        var expectedResponse = AdminHrMngLeaveResponse.builder()
                .leaveId(patchedLeave.getLeaveId())
                .leaveType(patchedLeave.getLeaveType())
                .leaveStarts(patchedLeave.getLeaveStarts())
                .leaveEnds(patchedLeave.getLeaveEnds())
                .approvedBy("user with this id" + patchedLeave.getApprovedBy())
                .approvedOn(patchedLeave.getApprovedOn())
                .approved(patchedLeave.isApproved())
                .requestedBy(patchedLeave.getRequestedBy().getEmail())
                .build();
        when(leaveRepo.findById(any(UUID.class))).thenReturn(Optional.of(leave));
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(leaveConverter.approveLeave(any(Leave.class),any(User.class))).thenReturn(patchedLeave);
        when(leaveRepo.save(any(Leave.class))).thenReturn(patchedLeave);
        when(leaveConverter.fromLeaveToAdminHrMngLeave(any(Leave.class))).thenReturn(expectedResponse);
        var response = leaveService.approveLeave(UUID.randomUUID());
        Assertions.assertEquals(expectedResponse,response);
    }

}
