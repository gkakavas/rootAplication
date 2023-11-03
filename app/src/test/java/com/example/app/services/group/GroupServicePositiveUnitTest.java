package com.example.app.services.group;

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
import com.example.app.services.GroupService;
import com.example.app.utils.group.EntityResponseGroupConverter;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class GroupServicePositiveUnitTest {
    @InjectMocks
    private GroupService groupService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private GroupRepository groupRepo;
    @Mock
    private EntityResponseGroupConverter groupConverter;

    private User currentUser;
    private Principal principal;
    private String roleValue;

    @BeforeEach
     void setUp(){
        MockitoAnnotations.openMocks(this);
        groupService = new GroupService(userRepo, groupRepo, groupConverter);
    }
    void setUpPrincipal(){
        currentUser = Instancio.of(User.class).set(field(User::getRoleValue),roleValue).set(field(User::getRole),Role.valueOf(roleValue)).create();
        principal = (Principal) currentUser;
    }
    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Should store a group and return the AdminGroupResponse")
    void shouldStoreAGroupAndReturnTheAdminGroupResponse() throws Exception {
        this.roleValue = "ADMIN";
        setUpPrincipal();
        var request = Instancio.create(GroupRequestEntity.class);
        var usersToAddInGroup = request.getIdsSet().stream().map(uuid ->
                Instancio.of(User.class).set(field(User::getUserId),uuid)
                        .create()).collect(Collectors.toSet());
        var createdGroup = Group.builder()
                .groupId(UUID.randomUUID()).groupName(request.getGroupName()).groupCreator(currentUser.getUserId())
                .groupCreationDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).groupHasUsers(usersToAddInGroup)
                .build();
        var expectedResponse = AdminGroupResponse.builder()
                .groupId(createdGroup.getGroupId()).groupName(createdGroup.getGroupName())
                .groupCreator("user with id " + createdGroup.getGroupCreator()).groupCreationDate(createdGroup.getGroupCreationDate())
                .users(createdGroup.getGroupHasUsers().stream().map(user -> AdminUserResponse.builder()
                                .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                                .email(user.getEmail()).specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                                .groupName(user.getGroup().getGroupName()).createdBy("user with id " + user.getCreatedBy())
                                .registerDate(user.getRegisterDate()).lastLogin(user.getLastLogin()).role(user.getRole())
                        .build()).collect(Collectors.toSet()))
                .build();
        when(userRepo.findAllById(request.getIdsSet())).thenReturn(List.copyOf(usersToAddInGroup));
        when(groupConverter.fromRequestToGroup(request,currentUser.getUserId())).thenReturn(createdGroup);
        when(groupRepo.save(createdGroup)).thenReturn(createdGroup);
        when(groupConverter.fromGroupToAdminGroup(createdGroup)).thenReturn(expectedResponse);
        var response = groupService.create(request,this.principal);
        assertEquals(expectedResponse,response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","MANAGER"})
    @DisplayName("Should return a specific group in form that corresponds to the current user")
    void shouldReturnASpecifiedGroupInFormThatCorrespondsToTheCurrentUser(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUpPrincipal();
        var group = Instancio.create(Group.class);
        when(groupRepo.findById(any(UUID.class))).thenReturn(Optional.of(group));
        if(currentUser.getRole().equals(Role.ADMIN)){
            var adminUsers = group.getGroupHasUsers().stream().map(user ->
                    AdminUserResponse.builder()
                            .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                            .email(user.getEmail()).specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName()).createdBy(currentUser.getEmail())
                            .registerDate(user.getRegisterDate()).lastLogin(user.getLastLogin()).role(user.getRole())
                            .build()
            ).collect(Collectors.toSet());
            var adminExpectedResponse = AdminGroupResponse.builder()
                    .groupId(group.getGroupId()).groupName(group.getGroupName())
                    .groupCreationDate(group.getGroupCreationDate()).groupCreator(currentUser.getEmail())
                    .users(adminUsers)
                    .build();
            when(groupConverter.fromGroupToAdminGroup(group)).thenReturn(adminExpectedResponse);
            var response = groupService.read(group.getGroupId(),this.principal);
            assertEquals(adminExpectedResponse,response);
        }
        else if (currentUser.getRole().equals(Role.MANAGER)){
            currentUser.setGroup(group);
            var managerUsers = group.getGroupHasUsers().stream().map(user ->
                    OtherUserResponse.builder().userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                            .email(user.getEmail()).specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName())
                            .build()
            ).collect(Collectors.toSet());
            var managerExpectedResponse = ManagerGroupResponse.builder()
                    .groupId(group.getGroupId()).groupName(group.getGroupName()).users(managerUsers)
                    .build();
            when(groupConverter.fromGroupToMngGroup(group)).thenReturn(managerExpectedResponse);
            var response = groupService.read(group.getGroupId(),this.principal);
            assertEquals(managerExpectedResponse,response);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR"})
    @DisplayName("Should return all groups in form that corresponds to the current user")
    void shouldReturnAllGroupsInFormThatCorrespondsToTheCurrentUser(String roleValue){
        this.roleValue = roleValue;
        setUpPrincipal();
        var groups = Instancio.ofList(Group.class).size(5).create();
        groups.forEach(group->group.getGroupHasUsers().forEach(user -> user.setGroup(group)));
        when(groupRepo.findAll()).thenReturn(groups);
        if(currentUser.getRole().equals(Role.ADMIN)){
            var adminExpectedResponse = groups.stream().map(group ->AdminGroupResponse.builder()
                    .groupId(group.getGroupId()).groupName(group.getGroupName()).groupCreationDate(group.getGroupCreationDate())
                    .groupCreator(currentUser.getEmail()).users(group.getGroupHasUsers().stream().map(user ->
                            AdminUserResponse.builder()
                                    .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                                    .email(user.getEmail()).specialization(user.getSpecialization())
                                    .currentProject(user.getCurrentProject()).groupName(user.getGroup().getGroupName())
                                    .createdBy(currentUser.getEmail()).registerDate(user.getRegisterDate())
                                    .lastLogin(user.getLastLogin()).role(user.getRole())
                                    .build()
                    ).collect(Collectors.toSet()))
                    .build()).toList();
            when(groupConverter.fromGroupListToAdminGroupList(groups)).thenReturn(List.copyOf(adminExpectedResponse));
            var response = groupService.read(this.principal);
            assertEquals(adminExpectedResponse,response);
        }
        else if(currentUser.getRole().equals(Role.MANAGER)) {
            var managerExpectedResponse = groups.stream().map(group ->ManagerGroupResponse.builder()
                    .groupId(group.getGroupId()).groupName(group.getGroupName())
                    .users(group.getGroupHasUsers().stream().map(user -> OtherUserResponse.builder()
                            .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                            .email(user.getEmail()).specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject()).groupName(user.getGroup().getGroupName())
                            .build()
                    ).collect(Collectors.toSet()))
                    .build()).toList();
            when(groupConverter.fromGroupListToMngGroupList(groups)).thenReturn(List.copyOf(managerExpectedResponse));
            var response = groupService.read(this.principal);
            assertEquals(managerExpectedResponse,response);
        }
    }

    @Test
    @DisplayName("Should update a group, save it and returns updated group")
    void shouldUpdateAGroupSaveItAndReturnsUpdatedGroup() throws GroupNotFoundException {
        var request = Instancio.create(GroupRequestEntity.class);
        var groupToUpdate = Instancio.create(Group.class);
        var usersToAdd = request.getIdsSet().stream().map(uuid -> Instancio.create(User.class)).collect(Collectors.toSet());
        var updatedGroup = Group.builder()
                .groupId(groupToUpdate.getGroupId()).groupName(request.getGroupName()).groupCreator(groupToUpdate.getGroupCreator())
                .groupCreationDate(groupToUpdate.getGroupCreationDate())
                .groupHasUsers(usersToAdd)
                .build();
        var expectedResponse = AdminGroupResponse.builder()
                .groupId(updatedGroup.getGroupId())
                .groupName(updatedGroup.getGroupName())
                .groupCreator("user with id " + updatedGroup.getGroupCreator())
                .groupCreationDate(updatedGroup.getGroupCreationDate())
                .users(updatedGroup.getGroupHasUsers().stream().map(user -> AdminUserResponse.builder()
                                .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                                .email(user.getEmail()).specialization(user.getSpecialization())
                                .currentProject(user.getCurrentProject()).groupName(user.getGroup().getGroupName())
                                .createdBy("user with id " + user.getCreatedBy()).registerDate(user.getRegisterDate())
                                .lastLogin(user.getLastLogin()).role(user.getRole())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
        when(groupRepo.findById(groupToUpdate.getGroupId())).thenReturn(Optional.of(groupToUpdate));
        when(userRepo.findAllById(request.getIdsSet())).thenReturn(List.copyOf(usersToAdd));
        when(groupRepo.save(updatedGroup)).thenReturn(updatedGroup);
        when(groupConverter.fromGroupToAdminGroup(updatedGroup)).thenReturn(expectedResponse);
        var response = groupService.update(groupToUpdate.getGroupId(),request);
        assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should delete a specified group from database")
    void shouldDeleteASpecifiedGroupFromDatabase() throws GroupNotFoundException {
        var groupToDelete = Instancio.create(Group.class);
        when(groupRepo.findById(groupToDelete.getGroupId())).thenReturn(Optional.of(groupToDelete));
        var response = groupService.delete(groupToDelete.getGroupId());
        assertTrue(response);
    }
}
