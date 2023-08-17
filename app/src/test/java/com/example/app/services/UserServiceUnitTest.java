package com.example.app.services;

import com.example.app.entities.Event;
import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.event.EntityResponseEventConverterImpl;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
public class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private EntityResponseEventConverterImpl eventConverter;
    @Mock
    private EntityResponseUserConverterImpl userConverter;

    private final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, jwtService, groupRepository, userConverter, eventConverter);
        this.securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", List.of()));
        SecurityContextHolder.setContext(securityContext);
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    private static final String TEST_TOKEN = "test token";
    private static final String TEST_EXTRACTED_USERNAME = "test@email.com";
    private static final UUID TEST_UUID_FROM_EXTRACTED_USERNAME = UUID.randomUUID();
    private static final String TEST_REQUEST_FIRSTNAME="test_username";
    private static final String TEST_REQUEST_LASTNAME="test_lastname";
    private static final String TEST_REQUEST_PASSWORD="Test1234";
    private static final String TEST_REQUEST_EMAIL="request@email.com";
    private static final String TEST_REQUEST_SPECIALIZATION="test_specialization";
    private static final String TEST_REQUEST_CURRENT_PROJECT="test_current_project";
    private static final UUID TEST_REQUEST_GROUP = UUID.randomUUID();
    private static final String TEST_REQUEST_ROLE= "ADMIN";
    private static final UserRequestEntity USER_CREATE_REQUEST= UserRequestEntity.builder()
            .firstname(TEST_REQUEST_FIRSTNAME)
            .lastname(TEST_REQUEST_LASTNAME)
            .password(TEST_REQUEST_PASSWORD)
            .email(TEST_REQUEST_EMAIL)
            .specialization(TEST_REQUEST_SPECIALIZATION)
            .currentProject(TEST_REQUEST_CURRENT_PROJECT)
            .group(TEST_REQUEST_GROUP)
            .role(TEST_REQUEST_ROLE)
            .build();
    private static final Group group = Group.builder()
            .groupId(TEST_REQUEST_GROUP)
            .groupCreator(UUID.randomUUID())
            .groupName("test_group")
            .groupCreationDate(LocalDateTime.now())
            .build();

    private static final User USER_CREATOR = User.builder()
            .userId(UUID.randomUUID())
            .firstname("TEST_USER_CREATOR_FIRSTNAME")
            .lastname("TEST_USER_CREATOR_LASTNAME")
            .email("test@email.com")
            .role(Role.valueOf("ADMIN"))
            .build();
    private final User TEST_USER = User.builder()
            .userId(UUID.randomUUID())
            .firstname(TEST_REQUEST_FIRSTNAME)
            .lastname(TEST_REQUEST_LASTNAME)
            .email(TEST_REQUEST_EMAIL)
            .specialization(TEST_REQUEST_SPECIALIZATION)
            .currentProject(TEST_REQUEST_CURRENT_PROJECT)
            .role(Role.valueOf(TEST_REQUEST_ROLE))
            .createdBy(TEST_UUID_FROM_EXTRACTED_USERNAME)
            .registerDate(LocalDateTime.now())
            .group(group)
            .build();

    private final UserResponseEntity EXPECTED_RESPONSE = AdminUserResponse.builder()
            .userId(TEST_USER.getUserId())
            .firstname(TEST_USER.getFirstname())
            .lastname(TEST_USER.getLastname())
            .email(TEST_USER.getEmail())
            .currentProject(TEST_USER.getCurrentProject())
            .specialization(TEST_USER.getSpecialization())
            .groupName(group.getGroupName())
            .role(TEST_USER.getRole())
            .lastLogin(TEST_USER.getLastLogin())
            .createdBy(USER_CREATOR.getEmail())
            .registerDate(TEST_USER.getRegisterDate())
            .build();
    private static final User CURRENT_USER = User.builder()
            .userId(UUID.randomUUID())
            .firstname("TEST_CURRENT_USER_FIRSTNAME")
            .lastname("TEST_CURRENT_USER_LASTNAME")
            .email("test@email.com")
            .role(Role.valueOf("ADMIN"))
            .build();


    @Test
    @DisplayName("Should Store A User In Database And Return Admin User Response Entity")
    public void storeAUserInDatabaseAndReturnAdminUserResponseEntity() throws UserNotFoundException {
        when(jwtService.extractUsername(any(String.class))).thenReturn(TEST_EXTRACTED_USERNAME);
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(USER_CREATOR));
        when(groupRepository.findById(any(UUID.class))).thenReturn(Optional.of(group));
        when(userConverter.fromRequestToEntity(USER_CREATE_REQUEST,
                USER_CREATOR.getUserId(), group)).thenReturn(TEST_USER);
        when(userRepository.save(TEST_USER)).thenReturn(TEST_USER);
        when(userConverter.fromUserToAdminUser(eq(TEST_USER))).thenReturn((AdminUserResponse) EXPECTED_RESPONSE);
        UserResponseEntity response = userService.create(USER_CREATE_REQUEST, TEST_TOKEN);
        assertEquals(EXPECTED_RESPONSE,response);
    }

    @Test
    @DisplayName("Should Return A User in form that corresponds to the current user")
    void shouldReturnAUserInFormThatCorrespondsToTheCurrentUser() throws UserNotFoundException {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(TEST_USER));
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(CURRENT_USER));
        when(userConverter.fromUserToAdminUser(eq(TEST_USER))).thenReturn((AdminUserResponse) EXPECTED_RESPONSE);
        UserResponseEntity response = userService.read(TEST_USER.getUserId());
        assertEquals(EXPECTED_RESPONSE,response);
    }
    @Test
    @DisplayName("Should Return All Users in form that corresponds to the current user")
    void shouldReturnAllUsersInFormThatCorrespondsToTheCurrentUser() {
        User user1 = User.builder().userId(UUID.randomUUID()).firstname("TEST_USER_FIRSTNAME1")
                .lastname("TEST_USER_LASTNAME1").email("test@email1.com")
                .role(Role.valueOf("USER")).build();
        User user2 = User.builder().userId(UUID.randomUUID()).firstname("TEST_USER_FIRSTNAME2")
                .lastname("TEST_USER_LASTNAME2").email("test@email2.com")
                .role(Role.valueOf("MANAGER")).build();
        User user3 = User.builder().userId(UUID.randomUUID()).firstname("TEST_USER_FIRSTNAME3")
                .lastname("TEST_USER_LASTNAME3").email("test@email3.com")
                .role(Role.valueOf("USER")).build();
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);
        userList.add(user3);
        final Set<AdminUserResponse> EXPECTED_RESPONSE = userConverter.fromUserListToAdminList(Set.copyOf(userList));
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(CURRENT_USER));
        when(userRepository.findAll()).thenReturn(userList);
        when(userConverter.fromUserListToAdminList(eq(new HashSet<>(userList)))).thenReturn(EXPECTED_RESPONSE);
        List<UserResponseEntity> response = userService.read();
        assertEquals(List.copyOf(EXPECTED_RESPONSE), response);
    }

    @Test
    @DisplayName("Should Update A User, Save Him And Return Updated User")
    void shouldUpdateAUserSaveHimAndReturnThisUserResponse() throws UserNotFoundException {
        final String TEST_UPDATED_ROLE = "HR";
        var userUpdateRequest = UserRequestEntity.builder()
                .firstname(TEST_REQUEST_FIRSTNAME)
                .lastname(TEST_REQUEST_LASTNAME)
                .email(TEST_REQUEST_EMAIL)
                .specialization(TEST_REQUEST_SPECIALIZATION)
                .currentProject(TEST_REQUEST_CURRENT_PROJECT)
                .role(TEST_UPDATED_ROLE)
                .group(group.getGroupId())
                .build();
        var updatedUser = User.builder()
                .userId(TEST_USER.getUserId())
                .firstname(TEST_USER.getFirstname())
                .lastname(TEST_USER.getLastname())
                .email(TEST_USER.getEmail())
                .specialization(TEST_USER.getSpecialization())
                .currentProject(TEST_USER.getCurrentProject())
                .role(Role.valueOf(TEST_UPDATED_ROLE))
                .createdBy(TEST_USER.getCreatedBy())
                .registerDate(TEST_USER.getRegisterDate())
                .group(group)
                .build();
        var EXPECTED_RESPONSE = AdminUserResponse.builder()
                .userId(TEST_USER.getUserId())
                .firstname(userUpdateRequest.getFirstname())
                .lastname(userUpdateRequest.getLastname())
                .email(userUpdateRequest.getEmail())
                .specialization(userUpdateRequest.getSpecialization())
                .currentProject(userUpdateRequest.getCurrentProject())
                .role(Role.valueOf(TEST_UPDATED_ROLE))
                .groupName(group.getGroupName())
                .build();
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(TEST_USER));
        when(groupRepository.findById(eq(group.getGroupId()))).thenReturn(Optional.of(group));
        when(userConverter.updateSetting(eq(TEST_USER),eq(userUpdateRequest),eq(group))).thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userConverter.fromUserToAdminUser(updatedUser)).thenReturn(EXPECTED_RESPONSE);
        UserResponseEntity response = userService.update(UUID.randomUUID(),userUpdateRequest);
        assertEquals(EXPECTED_RESPONSE,response);
    }

    @Test
    @DisplayName("Should Patch A User, Save Him And Return Patched User")
    void shouldPatchAUserSaveHimAndReturnPatchedUser() throws UserNotFoundException {
        Map<String,String> TEST_MAP = new HashMap<>();
        final String TEST_PATCH_ROLE_KEY = "role";
        final String TEST_PATCH_ROLE_VALUE = "HR";
        TEST_MAP.put(TEST_PATCH_ROLE_KEY,TEST_PATCH_ROLE_VALUE);
        var patcedUser = User.builder()
                .userId(UUID.randomUUID())
                .firstname(TEST_REQUEST_FIRSTNAME)
                .lastname(TEST_REQUEST_LASTNAME)
                .email(TEST_REQUEST_EMAIL)
                .specialization(TEST_REQUEST_SPECIALIZATION)
                .currentProject(TEST_REQUEST_CURRENT_PROJECT)
                .role(Role.valueOf(TEST_PATCH_ROLE_VALUE))
                .group(group)
                .build();
        var EXPECTED_RESPONSE = AdminUserResponse.builder()
                .userId(TEST_USER.getUserId())
                .firstname(TEST_USER.getFirstname())
                .lastname(TEST_USER.getLastname())
                .email(TEST_USER.getEmail())
                .specialization(TEST_USER.getSpecialization())
                .currentProject(TEST_USER.getCurrentProject())
                .role(Role.valueOf(TEST_MAP.get(TEST_PATCH_ROLE_KEY)))
                .groupName(group.getGroupName())
                .build();
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(TEST_USER));
        when(userRepository.save(TEST_USER)).thenReturn(patcedUser);
        when(userConverter.fromUserToAdminUser(patcedUser)).thenReturn(EXPECTED_RESPONSE);
        var response = userService.patch(UUID.randomUUID(),TEST_MAP);
        assertEquals(EXPECTED_RESPONSE,response);
    }

    @Test
    @DisplayName("Should return all events of a specified user")
    void shouldReturnAllEventsOfASpecifiedUser() throws UserNotFoundException {
        final String TEST_EVENT_BODY1 = "This is the test event 1";
        final String TEST_EVENT_DESCRIPTION1 = "Test event 1";
        final LocalDateTime TEST_DATE_TIME1 = LocalDateTime.of(2023, Month.AUGUST,17,18,25,30);
        final LocalDateTime TEST_EXPIRATION1 = LocalDateTime.of(2023, Month.AUGUST,17,19,25,30);
        var event1 = Event.builder()
                .eventId(UUID.randomUUID())
                .eventBody(TEST_EVENT_BODY1)
                .eventDescription(TEST_EVENT_DESCRIPTION1)
                .eventCreator(UUID.randomUUID())
                .eventDateTime(TEST_DATE_TIME1)
                .eventExpiration(TEST_EXPIRATION1)
                .build();
        final String TEST_EVENT_BODY2 = "This is the test event 2";
        final String TEST_EVENT_DESCRIPTION2 = "Test event 2";
        final LocalDateTime TEST_DATE_TIME2 = LocalDateTime.of(2023, Month.AUGUST,18,18,25,30);
        final LocalDateTime TEST_EXPIRATION2 = LocalDateTime.of(2023, Month.AUGUST,18,19,25,30);
        var event2 = Event.builder()
                .eventId(UUID.randomUUID())
                .eventBody(TEST_EVENT_BODY2)
                .eventDescription(TEST_EVENT_DESCRIPTION2)
                .eventCreator(UUID.randomUUID())
                .eventDateTime(TEST_DATE_TIME2)
                .eventExpiration(TEST_EXPIRATION2)
                .build();
        TEST_USER.getUserHasEvents().add(event1);
        TEST_USER.getUserHasEvents().add(event2);
        Set<EventResponseEntity> myEventSet = new HashSet<>();
        var myEventResponse1 = MyEventResponse.builder()
                .eventId(event1.getEventId())
                .eventBody(event1.getEventBody())
                .eventDescription(event1.getEventDescription())
                .eventDateTime(event1.getEventDateTime())
                .eventExpiration(event1.getEventExpiration())
                .build();
        var myEventResponse2 = MyEventResponse.builder()
                .eventId(event2.getEventId())
                .eventBody(event2.getEventBody())
                .eventDescription(event2.getEventDescription())
                .eventDateTime(event2.getEventDateTime())
                .eventExpiration(event2.getEventExpiration())
                .build();
        myEventSet.add(myEventResponse1);
        myEventSet.add(myEventResponse2);
        when(userRepository.findById(TEST_USER.getUserId())).thenReturn(Optional.of(TEST_USER));
        when(eventConverter.fromEventListToMyList(TEST_USER.getUserHasEvents())).thenReturn(myEventSet);
        var response = userService.readUserEvents(TEST_USER.getUserId());
        assertEquals(myEventSet,response);
    }
}


