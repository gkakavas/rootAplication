package com.example.app.converters;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.user.EntityResponseUserConverter;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserConvertersTest {

    @InjectMocks
    private EntityResponseUserConverter userConverter;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private static final UUID TEST_UUID_FROM_EXTRACTED_USERNAME = UUID.randomUUID();
    private static final String TEST_REQUEST_FIRSTNAME="test_username";
    private static final String TEST_REQUEST_LASTNAME="test_lastname";
    private static final String TEST_REQUEST_PASSWORD="Test1234";
    private static final String TEST_REQUEST_EMAIL="request@email.com";
    private static final String TEST_REQUEST_SPECIALIZATION="test_specialization";
    private static final String TEST_REQUEST_CURRENT_PROJECT="test_current_project";
    private static final UUID TEST_REQUEST_GROUP = UUID.randomUUID();
    private static final String TEST_REQUEST_ROLE= "ADMIN";
    Logger logger = LoggerFactory.getLogger(UserConvertersTest.class);

    private static final User user1 = User.builder()
            .userId(UUID.randomUUID())
            .firstname("test_firstname1")
            .lastname("test_lastname1")
            .password("testPassword1234")
            .email("test@email.com")
            .build();
    private static final User user2 = User.builder()
            .userId(UUID.randomUUID())
            .firstname("test_firstname2")
            .lastname("test_lastname2")
            .password("testPassword1234")
            .email("test@email2.com")
            .build();

    private static final Set<User> userSet = new HashSet<>();

    private static final Group group = Group.builder()
            .groupId(TEST_REQUEST_GROUP)
            .groupCreator(UUID.randomUUID())
            .groupName("test_group")
            .groupCreationDate(LocalDateTime.now())
            .build();
    private final User TEST_USER = User.builder()
            .userId(UUID.randomUUID())
            .firstname(TEST_REQUEST_FIRSTNAME)
            .lastname(TEST_REQUEST_LASTNAME)
            .password(TEST_REQUEST_PASSWORD)
            .email(TEST_REQUEST_EMAIL)
            .specialization(TEST_REQUEST_SPECIALIZATION)
            .currentProject(TEST_REQUEST_CURRENT_PROJECT)
            .role(Role.valueOf(TEST_REQUEST_ROLE))
            .createdBy(TEST_UUID_FROM_EXTRACTED_USERNAME)
            .registerDate(LocalDateTime.now())
            .group(group)
            .build();


    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userConverter = new EntityResponseUserConverterImpl(userRepository,passwordEncoder);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    @DisplayName("Should convert a user entity to admin-form user response")
    void shouldConvertAUserEntityToAdminFormUserResponse(){
        var adminFormedUser = AdminUserResponse.builder()
                .userId(TEST_USER.getUserId())
                .firstname(TEST_USER.getFirstname())
                .lastname(TEST_USER.getLastname())
                .email(TEST_USER.getEmail())
                .specialization(TEST_USER.getSpecialization())
                .currentProject(TEST_USER.getCurrentProject())
                .groupName(TEST_USER.getGroup().getGroupName())
                .registerDate(TEST_USER.getRegisterDate())
                .role(TEST_USER.getRole())
                .lastLogin(TEST_USER.getLastLogin())
                .build();
        when(userRepository.findById(TEST_USER.getUserId())).thenReturn(Optional.of(TEST_USER));
        var result = userConverter.fromUserToAdminUser(TEST_USER);
        assertEquals(adminFormedUser,result);
    }

    @Test
    @DisplayName("Should convert a user entity to otherUser-form user response")
    void shouldConvertAUserEntityToOtherUserFormUserResponse(){
        var otherUserFormedUser=  OtherUserResponse.builder()
                .userId(TEST_USER.getUserId())
                .firstname(TEST_USER.getFirstname())
                .lastname(TEST_USER.getLastname())
                .email(TEST_USER.getEmail())
                .specialization(TEST_USER.getSpecialization())
                .currentProject(TEST_USER.getCurrentProject())
                .groupName(TEST_USER.getGroup().getGroupName())
                .build();
        var result = userConverter.fromUserToOtherUser(TEST_USER);
        assertEquals(otherUserFormedUser,result);
    }

    @Test
    @DisplayName("Should convert a user entity Set to admin-form user Set response")
    void shouldConvertAUserEntitySetToAdminFormUserSetResponse(){
        userSet.add(user1);
        userSet.add(user2);
        var userAdminResponse1 = AdminUserResponse.builder()
                .userId(user1.getUserId())
                .firstname(user1.getFirstname())
                .lastname(user1.getLastname())
                .email(user1.getEmail())
                .build();
        var userAdminResponse2 = AdminUserResponse.builder()
                .userId(user2.getUserId())
                .firstname(user2.getFirstname())
                .lastname(user2.getLastname())
                .email(user2.getEmail())
                .build();
        Set<AdminUserResponse> adminUserResponseSet = new HashSet<>();
        adminUserResponseSet.add(userAdminResponse1);
        adminUserResponseSet.add(userAdminResponse2);
        var result = userConverter.fromUserListToAdminList(userSet);
        assertEquals(adminUserResponseSet,result);
    }
    @Test
    @DisplayName("Should convert a user entity Set to otherUser-form user Set response")
    void shouldConvertAUserEntitySetToOtherUserFormUserSetResponse(){
        userSet.add(user1);
        userSet.add(user2);
        var otherUserResponse1 = OtherUserResponse.builder()
                .userId(user1.getUserId())
                .firstname(user1.getFirstname())
                .lastname(user1.getLastname())
                .email(user1.getEmail())
                .build();
        var otherUserResponse2 = OtherUserResponse.builder()
                .userId(user2.getUserId())
                .firstname(user2.getFirstname())
                .lastname(user2.getLastname())
                .email(user2.getEmail())
                .build();
        Set<OtherUserResponse> otherUserResponseSet = new HashSet<>();
        otherUserResponseSet.add(otherUserResponse1);
        otherUserResponseSet.add(otherUserResponse2);
        var result = userConverter.fromUserListToOtherList(userSet);
        assertEquals(otherUserResponseSet,result);
    }

    @Test
    @DisplayName("Should convert a user request entity to User entity")
    void shouldConvertAUserRequestEntityToUserEntity(){
        var userRequest = UserRequestEntity.builder()
                .firstname("test_firstname1")
                .lastname("test_lastname1")
                .password("testPassword1234")
                .email("test@email.com")
                .build();
        var userEntity = User.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .password(userRequest.getPassword())
                .email(userRequest.getEmail())
                .createdBy(UUID.randomUUID())
                .group(group)
                .userHasFiles(null)
                .userRequestedLeaves(null)
                .userHasEvents(null)
                .build();
        when(passwordEncoder.encode(any())).thenReturn("testPassword1234");
        var result = userConverter.fromRequestToEntity(userRequest,userEntity.getCreatedBy(),group);
        userEntity.setRegisterDate(result.getRegisterDate());
        assertEquals(userEntity,result);
    }

    @Test
    @DisplayName("Should update a user entity")
    void shouldUpdateAUserEntity(){
        var userRequestEntity = UserRequestEntity.builder()
                .firstname("otherTestFirstname")
                .lastname("otherTestLastname")
                .password(TEST_REQUEST_PASSWORD)
                .email("other@testemail.com")
                .specialization("other specialization")
                .currentProject("other current project")
                .role("USER")
                .group(UUID.randomUUID())
                .build();
        var UPDATED_TEST_USER = User.builder()
                .userId(TEST_USER.getUserId())
                .firstname(userRequestEntity.getFirstname())
                .lastname(userRequestEntity.getLastname())
                .password("testPassword1234")
                .email(userRequestEntity.getEmail())
                .specialization(userRequestEntity.getSpecialization())
                .currentProject(userRequestEntity.getCurrentProject())
                .role(Role.valueOf(userRequestEntity.getRole()))
                .createdBy(TEST_USER.getCreatedBy())
                .registerDate(TEST_USER.getRegisterDate())
                .group(group)
                .build();
        when(passwordEncoder.encode(any())).thenReturn("testPassword1234");
        var result = userConverter.updateSetting(TEST_USER,userRequestEntity,group);
        System.out.println(result);
        System.out.println(UPDATED_TEST_USER);
        assertEquals(UPDATED_TEST_USER,result);
    }
}
