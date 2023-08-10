package com.example.app.services;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.event.EntityResponseEventConverterImpl;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, jwtService, groupRepository, userConverter, eventConverter);
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
            .userHasEvents(null)
            .userHasFiles(null)
            .userRequestedLeaves(null)
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
    @Test
    @DisplayName("Store A User In Database And Return Admin User Response Entity")
    public void storeAUserInDatabaseAndReturnAdminUserResponseEntity() throws UserNotFoundException {
        when(jwtService.extractUsername(any(String.class))).thenReturn(TEST_EXTRACTED_USERNAME);
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(USER_CREATOR));
        when(groupRepository.findById(any(UUID.class))).thenReturn(Optional.of(group));
        when(userConverter.fromRequestToEntity(eq(USER_CREATE_REQUEST),
                eq(USER_CREATOR.getUserId()), eq(group))).thenReturn(TEST_USER);
        when(userRepository.save(any(User.class))).thenReturn(TEST_USER);
        when(userConverter.fromUserToAdminUser(eq(TEST_USER))).thenReturn((AdminUserResponse) EXPECTED_RESPONSE);
        UserResponseEntity response = userService.create(USER_CREATE_REQUEST, TEST_TOKEN);
        assertEquals(EXPECTED_RESPONSE,response);
    }
}


