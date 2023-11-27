package com.example.app.converters.user;

import com.example.app.config.TestConfig;
import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.converters.event.EntityResponseEventConverter;
import com.example.app.utils.converters.user.EntityResponseUserConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
@SpringJUnitConfig(classes = {TestConfig.class})
public class UserConverterPositiveUnitTest {
    @InjectMocks
    private EntityResponseUserConverterImpl userConverter;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Clock clock;
    @Mock
    private EntityResponseEventConverter eventConverter;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userConverter = new EntityResponseUserConverterImpl(userRepository,passwordEncoder,clock,eventConverter);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    @DisplayName("Should convert a user entity to admin format user response")
    void shouldConvertAUserEntityToAdminFormUserResponse(){
        var user = Instancio.create(User.class);
        var createdBy = Instancio.create(User.class);
        var adminFormatUser = AdminUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(user.getGroup().getGroupName())
                .createdBy(createdBy.getEmail())
                .registerDate(user.getRegisterDate())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .build();
        when(userRepository.findById(user.getCreatedBy())).thenReturn(Optional.of(createdBy));
        var result = userConverter.fromUserToAdminUser(user);
        assertEquals(adminFormatUser,result);
    }

    @Test
    @DisplayName("Should convert a user entity to otherUser format user response")
    void shouldConvertAUserEntityToOtherUserFormUserResponse(){
        var user = Instancio.create(User.class);
        var otherUserFormedUser = OtherUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(user.getGroup().getGroupName())
                .build();
        var result = userConverter.fromUserToOtherUser(user);
        assertEquals(otherUserFormedUser,result);
    }

    @Test
    @DisplayName("Should convert a user entity Set to admin format Set response")
    void shouldConvertAUserEntitySetToAdminFormUserSetResponse(){
        var users = Instancio.stream(User.class)
                .peek(user -> user.setCreatedBy(null))
                .limit(30)
                .collect(Collectors.toSet());
        var adminUserResponseSet = users.stream().map(user -> AdminUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(user.getGroup().getGroupName())
                .registerDate(user.getRegisterDate())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .build())
                .collect(Collectors.toSet());
        var result = userConverter.fromUserListToAdminList(users);
        assertEquals(adminUserResponseSet,result);
    }
    @Test
    @DisplayName("Should convert a user entity set to otherUser format set response")
    void shouldConvertAUserEntitySetToOtherUserFormUserSetResponse(){
        var users = Instancio.createSet(User.class);
        var otherUserResponseSet = users.stream().map(user -> OtherUserResponse.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .specialization(user.getSpecialization())
                .currentProject(user.getCurrentProject())
                .groupName(user.getGroup().getGroupName())
                .build())
                .collect(Collectors.toSet());
        var result = userConverter.fromUserListToOtherList(users);
        assertEquals(otherUserResponseSet,result);
    }

    @Test
    @DisplayName("Should convert a user request entity to User entity")
    void shouldConvertAUserRequestEntityToUserEntity(){
        var userGroup = Instancio.create(Group.class);
        var userCreatorUUID = UUID.randomUUID();
        var request = Instancio.of(UserRequestEntity.class)
                .generate(field(UserRequestEntity::getRole),gen ->
                        gen.enumOf(Role.class).asString())
                .create();
        String predefinedPassword = "123456";
        ReflectionTestUtils.setField(userConverter, "defaultPasswordForUserCreation",predefinedPassword);
        var expectedEntity = User.builder()
                .password(predefinedPassword)
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .specialization(request.getSpecialization())
                .currentProject(request.getCurrentProject())
                .createdBy(userCreatorUUID)
                .registerDate(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS))
                .roleValue(request.getRole())
                .role(Role.valueOf(request.getRole()))
                .group(userGroup)
                .build();
        when(passwordEncoder.encode(any(String.class))).thenReturn(predefinedPassword);
        var result = userConverter.fromRequestToEntity(request,userCreatorUUID,userGroup);
        assertEquals(expectedEntity,result);
    }

    @Test
    @DisplayName("Should update a user entity")
    void shouldUpdateAUserEntity(){
        var request = Instancio.of(UserRequestEntity.class)
                .generate(field(UserRequestEntity::getRole),gen ->
                        gen.enumOf(Role.class).asString())
                .create();
        var group = Instancio.create(Group.class);
        var user = Instancio.create(User.class);
        var result = userConverter.updateSetting(user,request,group);
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setSpecialization(request.getSpecialization());
        user.setCurrentProject(request.getCurrentProject());
        user.setRole(Role.valueOf(request.getRole()));
        user.setGroup(group);
        assertEquals(user,result);
    }
}
