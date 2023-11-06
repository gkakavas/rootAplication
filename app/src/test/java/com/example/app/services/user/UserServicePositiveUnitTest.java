package com.example.app.services.user;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.UserService;
import com.example.app.tool.utils.CustomPrincipal;
import com.example.app.utils.event.EntityResponseEventConverterImpl;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
@ContextConfiguration(classes = BCryptPasswordEncoder.class)
public class UserServicePositiveUnitTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private EntityResponseEventConverterImpl eventConverter;
    @Mock
    private EntityResponseUserConverterImpl userConverter;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Principal principal;
    private String roleValue;
    private User currentUser;

    void setUpPrincipal(){
         this.currentUser = Instancio.of(User.class)
                .set(field(User::getRole),Role.valueOf(roleValue))
                .set(field(User::getRoleValue),roleValue)
                .set(field(User::getGroup),Instancio.create(Group.class))
                .create();
        this.principal = new CustomPrincipal(currentUser);
    }
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, groupRepository, userConverter, eventConverter,passwordEncoder);
    }
    @AfterEach
    void tearDown(){
    }
    @Test
    @DisplayName("Should Store A User In Database And Return Admin User Response Entity")
    public void storeAUserInDatabaseAndReturnAdminUserResponseEntity() throws UserNotFoundException {
        this.roleValue = "ADMIN";
        setUpPrincipal();
        var request = Instancio.of(UserRequestEntity.class)
                .generate(field(UserRequestEntity::getRole),gen -> gen.enumOf(Role.class).asString())
                .create();
        var group = Instancio.of(Group.class)
                .set(field(Group::getGroupId), request.getGroup())
                .create();
        var user = User.builder()
                .userId(UUID.randomUUID()).password(passwordEncoder.encode("testPass"))
                .firstname(request.getFirstname()).lastname(request.getLastname()).email(request.getEmail())
                .specialization(request.getSpecialization()).currentProject(request.getCurrentProject()).createdBy(currentUser.getUserId())
                .registerDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).roleValue(request.getRole())
                .group(Instancio.of(Group.class).set(field(Group::getGroupId),request.getGroup()).create())
                .role(Role.valueOf(request.getRole()))
                .build();
        var expectedResponse = AdminUserResponse.builder()
                .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname()).email(user.getEmail())
                .specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                .groupName(user.getGroup().getGroupName()).registerDate(user.getRegisterDate()).role(user.getRole())
                .createdBy("user with this id " +user.getCreatedBy())
                .build();
        when(groupRepository.findById(request.getGroup())).thenReturn(Optional.of(group));
        when(userConverter.fromRequestToEntity(request,this.currentUser.getUserId(),group)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userConverter.fromUserToAdminUser(user)).thenReturn(expectedResponse);
        UserResponseEntity response = userService.create(request,this.principal);
        assertEquals(expectedResponse,response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("Should Return A User in form that corresponds to the current user")
    void shouldReturnAUserInFormThatCorrespondsToTheCurrentUser(String roleValue) throws UserNotFoundException {
        this.roleValue = roleValue;
        setUpPrincipal();
        var user = Instancio.create(User.class);
        var adminExpectedUserResponse = AdminUserResponse.builder()
                .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname()).email(user.getEmail())
                .specialization(user.getSpecialization()).currentProject(user.getCurrentProject()).groupName(user.getGroup().getGroupName())
                .createdBy("user with this id " + user.getCreatedBy()).registerDate(user.getRegisterDate()).lastLogin(user.getLastLogin())
                .role(user.getRole())
                .build();
        var otherUserExpectedResponse = OtherUserResponse.builder()
                .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                .email(user.getEmail()).specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                .groupName(user.getGroup().getGroupName())
                .build();
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        if (currentUser.getRole().equals(Role.ADMIN)){
            when(userConverter.fromUserToAdminUser(user)).thenReturn(adminExpectedUserResponse);
            UserResponseEntity response = userService.read(user.getUserId(),this.principal);
            assertEquals(adminExpectedUserResponse,response);
        }
        else{
            when(userConverter.fromUserToOtherUser(user)).thenReturn(otherUserExpectedResponse);
            UserResponseEntity response = userService.read(user.getUserId(),this.principal);
            assertEquals(otherUserExpectedResponse,response);
        }
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("Should Return All Users in form that corresponds to the current user")
    void shouldReturnAllUsersInFormThatCorrespondsToTheCurrentUser(String roleValue) {
        this.roleValue = roleValue;
        setUpPrincipal();
        var users = Instancio.createList(User.class);
        var adminExpectedUserResponse = users.stream().map(user -> AdminUserResponse.builder()
                        .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                        .email(user.getEmail()).specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                        .groupName(user.getGroup().getGroupName()).createdBy("user with this id "+ user.getCreatedBy())
                        .registerDate(user.getRegisterDate()).lastLogin(user.getLastLogin()).role(user.getRole())
                        .build()).collect(Collectors.toSet());
        var otherUserExpectedResponse = users.stream().map(user -> OtherUserResponse.builder()
                        .userId(user.getUserId()).firstname(user.getFirstname()).lastname(user.getLastname())
                        .email(user.getEmail()).specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                        .groupName(user.getGroup().getGroupName())
                        .build()).collect(Collectors.toSet());
        when(userRepository.findAll()).thenReturn(users);
        if(currentUser.getRole().equals(Role.ADMIN)) {
            when(userConverter.fromUserListToAdminList(Set.copyOf(users))).thenReturn(adminExpectedUserResponse);
            List<UserResponseEntity> response = userService.read(this.principal);
            assertEquals(List.copyOf(adminExpectedUserResponse), response);
        }
        else{
            when(userConverter.fromUserListToOtherList(Set.copyOf(users))).thenReturn(otherUserExpectedResponse);
            List<UserResponseEntity> response = userService.read(this.principal);
            assertEquals(List.copyOf(otherUserExpectedResponse), response);
        }
    }

    @Test
    @DisplayName("Should Update A User, Save Him And Return Updated User")
    void shouldUpdateAUserSaveHimAndReturnThisUserResponse() throws UserNotFoundException {
        var request = Instancio.of(UserRequestEntity.class)
                .generate(field(UserRequestEntity::getRole),gen -> gen.enumOf(Role.class).as(Enum::name))
                .create();
        var user = Instancio.create(User.class);
        var group = Instancio.of(Group.class).set(field(Group::getGroupId),request.getGroup()).create();
        var updatedUser = User.builder()
                .userId(user.getUserId()).firstname(request.getFirstname()).lastname(request.getLastname())
                .email(request.getEmail()).specialization(request.getSpecialization()).currentProject(request.getCurrentProject())
                .createdBy(user.getCreatedBy()).registerDate(user.getRegisterDate()).lastLogin(user.getLastLogin())
                .roleValue(request.getRole()).role(Role.valueOf(request.getRole()))
                .build();
        var expectedResponse = AdminUserResponse.builder()
                .userId(updatedUser.getUserId()).firstname(updatedUser.getFirstname()).lastname(updatedUser.getLastname())
                .email(updatedUser.getEmail()).specialization(updatedUser.getSpecialization()).currentProject(updatedUser.getCurrentProject())
                .groupName(updatedUser.getGroup().getGroupName()).createdBy("user with id " +updatedUser.getCreatedBy())
                .registerDate(updatedUser.getRegisterDate()).lastLogin(updatedUser.getLastLogin()).role(updatedUser.getRole())
                .build();
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(groupRepository.findById(group.getGroupId())).thenReturn(Optional.of(group));
        when(userConverter.updateSetting(user,request,group)).thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userConverter.fromUserToAdminUser(updatedUser)).thenReturn(expectedResponse);
        UserResponseEntity response = userService.update(user.getUserId(),request);
        assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should delete an existing user based on given id")
    void shouldDeleteAnExistingUserBasedOnGivenId() throws UserNotFoundException {
        var user = Instancio.create(User.class);
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(groupRepository.save(user.getGroup())).thenReturn(any());
        when(userRepository.existsById(user.getUserId())).thenReturn(true);
        var response = userService.delete(user.getUserId());
        assertFalse(response);
    }
    @Test
    @DisplayName("Should Patch A User, Save Him And Return Patched User")
    void shouldPatchAUserSaveHimAndReturnPatchedUser() throws UserNotFoundException, GroupNotFoundException {
        var user = Instancio.create(User.class);
        var group = Instancio.create(Group.class);
        Map<String,String> request = new HashMap<>();
        request.put("firstname","testFirstname");
        request.put("lastname","testLastname");
        request.put("email","test@email.com");
        request.put("specialization","testSpecialization");
        request.put("currentProject","testCurrentProject");
        request.put("role","MANAGER");
        request.put("group",group.getGroupId().toString());
        var patchedUser = User.builder()
                .userId(user.getUserId()).firstname(request.get("firstname")).lastname(request.get("lastname"))
                .email(request.get("email")).specialization(request.get("specialization")).currentProject(request.get("currentProject"))
                .createdBy(user.getCreatedBy()).registerDate(user.getRegisterDate()).lastLogin(user.getLastLogin())
                .roleValue(request.get("role")).role(Role.valueOf(request.get("role"))).group(group)
                .build();
        var expectedResponse = AdminUserResponse.builder()
                .userId(patchedUser.getUserId())
                .firstname(patchedUser.getFirstname())
                .lastname(patchedUser.getLastname())
                .email(patchedUser.getEmail())
                .specialization(patchedUser.getSpecialization())
                .currentProject(patchedUser.getCurrentProject())
                .groupName(patchedUser.getGroup().getGroupName())
                .createdBy("user with id " + patchedUser.getCreatedBy())
                .registerDate(patchedUser.getRegisterDate())
                .lastLogin(patchedUser.getLastLogin())
                .role(patchedUser.getRole())
                .build();
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(groupRepository.findById(UUID.fromString(request.get("group")))).thenReturn(Optional.of(group));
        when(userRepository.save(patchedUser)).thenReturn(patchedUser);
        when(userConverter.fromUserToAdminUser(patchedUser)).thenReturn(expectedResponse);
        var response = userService.patch(user.getUserId(),request);
        assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should return all events of a specified user")
    void shouldReturnAllEventsOfASpecifiedUser() throws UserNotFoundException {
        var user = Instancio.create(User.class);
        var myEventSet = user.getUserHasEvents().stream().map(event -> MyEventResponse.builder()
                        .eventId(event.getEventId()).eventDescription(event.getEventDescription()).eventBody(event.getEventBody())
                        .eventDateTime(event.getEventDateTime()).eventExpiration(event.getEventExpiration())
                .build()).collect(Collectors.toSet());
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(eventConverter.fromEventListToMyList(user.getUserHasEvents())).thenReturn(Set.copyOf(myEventSet));
        var response = userService.readUserEvents(user.getUserId());
        assertEquals(myEventSet,response);
    }
}


