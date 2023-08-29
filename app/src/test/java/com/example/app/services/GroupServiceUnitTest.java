package com.example.app.services;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.group.ManagerGroupResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.group.EntityResponseGroupConverter;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.generators.Generators;
import org.junit.Assert;
import org.junit.jupiter.api.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class GroupServiceUnitTest {
    @InjectMocks
    private GroupService groupService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private GroupRepository groupRepo;
    @Mock
    private JwtService jwtService;
    @Mock
    private EntityResponseGroupConverter groupConverter;
    private final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    @BeforeEach
     void setUp(){
        MockitoAnnotations.openMocks(this);
        groupService = new GroupService(userRepo, groupRepo, jwtService, groupConverter);
        this.securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", List.of()));
        SecurityContextHolder.setContext(securityContext);
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static final Set<User> users = new HashSet<>(Instancio.ofSet(User.class).size(5).create());
    private static final Group group = Instancio.of(Group.class)
            .set(field("groupHasUsers"),users)
            .create();
    private final User currentUser = Instancio.of(User.class)
            .generate(field("role"),gen -> gen.enumOf(Role.class).excluding(Role.USER,Role.HR))
            .create();
    @Test
    @DisplayName("Should store a group and return the AdminGroupResponse")
    void shouldStoreAGroupAndReturnTheAdminGroupResponse() throws Exception {
        var groupCreateRequest = Instancio.of(GroupRequestEntity.class)
                .set(field("idsSet"),users.stream().map(User::getUserId).collect(Collectors.toSet()))
                .create();
        when(userRepo.findAllById(groupCreateRequest.getIdsSet())).thenReturn(List.copyOf(users));
        when(jwtService.extractUsername(any(String.class))).thenReturn(currentUser.getEmail());
        when(userRepo.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(groupConverter.fromRequestToGroup(groupCreateRequest,currentUser.getUserId())).thenReturn(group);
        when(groupRepo.save(group)).thenReturn(group);
        var expectedResponse = Instancio.of(AdminGroupResponse.class).create();
        when(groupConverter.fromGroupToAdminGroup(group)).thenReturn(expectedResponse);
        var response = groupService.create(groupCreateRequest,"testToken");
        System.out.println(response);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should return a specified group in form that corresponds to the current user")
    void shouldReturnASpecifiedGroupInFormThatCorrespondsToTheCurrentUser() throws Exception {
        var group = Instancio.of(Group.class)
                .generate(field("groupHasUsers"),gen -> gen.collection().size(5))
                .create();
        group.getGroupHasUsers().forEach(user -> user.setGroup(group));
        when(groupRepo.findById(any(UUID.class))).thenReturn(Optional.of(group));
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var adminUsers = group.getGroupHasUsers().stream().map(user ->
            AdminUserResponse.builder()
                    .userId(user.getUserId())
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .email(user.getEmail())
                    .specialization(user.getSpecialization())
                    .currentProject(user.getCurrentProject())
                    .groupName(user.getGroup().getGroupName())
                    .createdBy(currentUser.getEmail())
                    .registerDate(user.getRegisterDate())
                    .lastLogin(user.getLastLogin())
                    .role(user.getRole())
                    .build()
         ).collect(Collectors.toSet());
        var managerUsers = group.getGroupHasUsers().stream().map(user ->
                OtherUserResponse.builder()
                        .userId(user.getUserId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .specialization(user.getSpecialization())
                        .currentProject(user.getCurrentProject())
                        .groupName(user.getGroup().getGroupName())
                        .build()
        ).collect(Collectors.toSet());

        var adminExpectedResponse = AdminGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupCreationDate(group.getGroupCreationDate())
                .groupCreator(currentUser.getEmail())
                .users(adminUsers)
                .build();
        var managerExpectedResponse = ManagerGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .users(managerUsers)
                .build();
        when(groupConverter.fromGroupToAdminGroup(group)).thenReturn(adminExpectedResponse);
        when(groupConverter.fromGroupToMngGroup(group)).thenReturn(managerExpectedResponse);
        var response = groupService.read(group.getGroupId());
        if(currentUser.getRole().equals(Role.ADMIN)){
            Assertions.assertEquals(adminExpectedResponse,response);
        }
        if(currentUser.getRole().equals(Role.MANAGER)){
            Assertions.assertEquals(managerExpectedResponse,response);
        }
    }

    @Test
    @DisplayName("Should return all groups in form that corresponds to the current user")
    void shouldReturnAllGroupsInFormThatCorrespondsToTheCurrentUser(){
        var groups = Instancio.ofList(Group.class)
                .size(5)
                .create();
        groups.forEach(group->group.getGroupHasUsers().forEach(user -> user.setGroup(group)));
        var adminExpectedResponse = groups.stream().map(group ->AdminGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupCreationDate(group.getGroupCreationDate())
                .groupCreator(currentUser.getEmail())
                .users(group.getGroupHasUsers().stream().map(user ->
                        AdminUserResponse.builder()
                                .userId(user.getUserId())
                                .firstname(user.getFirstname())
                                .lastname(user.getLastname())
                                .email(user.getEmail())
                                .specialization(user.getSpecialization())
                                .currentProject(user.getCurrentProject())
                                .groupName(user.getGroup().getGroupName())
                                .createdBy(currentUser.getEmail())
                                .registerDate(user.getRegisterDate())
                                .lastLogin(user.getLastLogin())
                                .role(user.getRole())
                                .build()
                ).collect(Collectors.toSet()))
                        .build()).collect(Collectors.toList());

        var managerExpectedResponse = groups.stream().map(group ->ManagerGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .users(group.getGroupHasUsers().stream().map(user ->
                        OtherUserResponse.builder()
                                .userId(user.getUserId())
                                .firstname(user.getFirstname())
                                .lastname(user.getLastname())
                                .email(user.getEmail())
                                .specialization(user.getSpecialization())
                                .currentProject(user.getCurrentProject())
                                .groupName(user.getGroup().getGroupName())
                                .build()
                ).collect(Collectors.toSet()))
                .build()).collect(Collectors.toList());

        /*var  =*/
        when(groupRepo.findAll()).thenReturn(groups);
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(groupConverter.fromGroupListToAdminGroupList(groups)).thenReturn(List.copyOf(adminExpectedResponse));
        when(groupConverter.fromGroupListToMngGroupList(groups)).thenReturn(List.copyOf(managerExpectedResponse));
        var response = groupService.read();
        if(currentUser.getRole().equals(Role.ADMIN)){
            Assertions.assertEquals(adminExpectedResponse,response);
        }
        else if (currentUser.getRole().equals(Role.MANAGER)){
            Assertions.assertEquals(managerExpectedResponse,response);
        }
    }

    @Test
    @DisplayName("Should update a group, save it and returns updated group")
    void shouldUpdateAGroupSaveItAndReturnsUpdatedGroup() throws GroupNotFoundException {
        var groupUpdateRequest = Instancio.of(GroupRequestEntity.class).create();
        var userList = Instancio.ofList(User.class)
                .size(10)
                .create();
        var adminExpectedResponse = AdminGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupCreationDate(group.getGroupCreationDate())
                .groupCreator(currentUser.getEmail())
                .users(userList.stream().map(user ->
                        Instancio.of(AdminUserResponse.class)
                                .set(field("userId"),user.getUserId())
                                .set(field("firstname"),user.getFirstname())
                                .set(field("lastname"),user.getLastname())
                                .set(field("email"),user.getEmail())
                                .set(field("specialization"),user.getSpecialization())
                                .set(field("currentProject"),user.getCurrentProject())
                                .set(field("groupName"),user.getGroup().getGroupName())
                                .ignore(field("createdBy"))
                                .ignore(field("registerDate"))
                                .ignore(field("lastLogin"))
                                .generate(field("role"),gen -> gen.enumOf(Role.class))
                                .create()
                ).collect(Collectors.toSet()))
                .build();

        var managerExpectedResponse = ManagerGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .users(userList.stream().map(user ->
                        Instancio.of(OtherUserResponse.class)
                                .set(field("userId"),user.getUserId())
                                .set(field("firstname"),user.getFirstname())
                                .set(field("lastname"),user.getLastname())
                                .set(field("email"),user.getEmail())
                                .set(field("specialization"),user.getSpecialization())
                                .set(field("currentProject"),user.getCurrentProject())
                                .set(field("groupName"),user.getGroup().getGroupName())
                                .create()
                ).collect(Collectors.toSet()))
                .build();
        when(groupRepo.findById(any(UUID.class))).thenReturn(Optional.of(group));
        when(groupRepo.save(group)).thenReturn(group);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(currentUser));
        when(groupConverter.fromGroupToAdminGroup(group)).thenReturn(adminExpectedResponse);
        when(groupConverter.fromGroupToMngGroup(group)).thenReturn(managerExpectedResponse);
        var response = groupService.update(group.getGroupId(),groupUpdateRequest);
        if(currentUser.getRole().equals(Role.ADMIN)){
            Assertions.assertEquals(adminExpectedResponse,response);
        }
        else if(currentUser.getRole().equals(Role.MANAGER)){
            Assertions.assertEquals(managerExpectedResponse,response);
        }
    }

    @Test
    @DisplayName("Should delete a specified group from database")
    void shouldDeleteASpecifiedGroupFromDatabase() throws GroupNotFoundException {
        when(groupRepo.findById(any(UUID.class))).thenReturn(Optional.of(group));
        var response = groupService.delete(group.getGroupId());
        var expectedResponse = true;
        Assertions.assertEquals(expectedResponse,response);
    }
}
