package com.example.app.converters.group;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.models.responses.group.ManagerGroupResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.converters.group.EntityResponseGroupConverterImpl;
import com.example.app.utils.converters.user.EntityResponseUserConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
public class GroupConverterPositiveUnitTest {
    @InjectMocks
    private EntityResponseGroupConverterImpl groupConverter;
    @Mock
    private EntityResponseUserConverterImpl userConverter;
    @Mock
    private UserRepository userRepo;
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        groupConverter = new EntityResponseGroupConverterImpl(userConverter,userRepo);
    }

    @Test
    @DisplayName("Should convert a group entity to manager group response")
    void shouldConvertAGroupEntityToManagerGroupResponse(){
        var group = Instancio.of(Group.class)
                .generate(field("groupHasUsers"),gen->gen.collection().size(5))
                .create();
        var expectedResponse = ManagerGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .users(group.getGroupHasUsers().stream().map(user ->OtherUserResponse.builder()
                        .userId(user.getUserId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .specialization(user.getSpecialization())
                        .currentProject(user.getCurrentProject())
                        .groupName(group.getGroupName())
                        .build()).collect(Collectors.toSet()))
                .build();
        when(userConverter.fromUserListToOtherList(group.getGroupHasUsers())).thenReturn(expectedResponse.getUsers());
        var response = groupConverter.fromGroupToMngGroup(group);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should convert a group list to manager group list")
    void shouldConvertAGroupListToManagerGroupList(){
        var groups =Instancio.ofList(Group.class)
                .size(5)
                .create();
        List<GroupResponseEntity> expectedResponse = new ArrayList<>();
        for(Group group:groups) {
           var groupExpectedResponse =  ManagerGroupResponse.builder()
                    .groupId(group.getGroupId())
                    .groupName(group.getGroupName())
                    .users(group.getGroupHasUsers().stream().map(user -> OtherUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(group.getGroupName())
                            .build()
                    ).collect(Collectors.toSet()))
                    .build();
            when(userConverter.fromUserListToOtherList(group.getGroupHasUsers())).thenReturn(groupExpectedResponse.getUsers());
            expectedResponse.add(groupExpectedResponse);
        }
        var response = groupConverter.fromGroupListToMngGroupList(groups);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should convert a group entity to admin group response")
    void shouldConvertAGroupEntityToAdminGroupResponse(){
        var groupCreator = Instancio.of(User.class)
                .create();
        var group = Instancio.of(Group.class)
                .generate(field("groupHasUsers"),gen->gen.collection().size(5))
                .create();
        var expectedResponse = AdminGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupCreationDate(group.getGroupCreationDate())
                .groupCreator(groupCreator.getEmail())
                .users(group.getGroupHasUsers().stream().map(user -> AdminUserResponse.builder()
                        .userId(user.getUserId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .specialization(user.getSpecialization())
                        .currentProject(user.getCurrentProject())
                        .groupName(group.getGroupName())
                        .lastLogin(user.getLastLogin())
                        .registerDate(user.getRegisterDate())
                        .createdBy("creator with uuid" +user.getCreatedBy())
                        .role(user.getRole())
                        .build()).collect(Collectors.toSet()))
                .build();
        when(userConverter.fromUserListToAdminList(group.getGroupHasUsers())).thenReturn(expectedResponse.getUsers());
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(groupCreator));
        var response = groupConverter.fromGroupToAdminGroup(group);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should convert a group list to admin group list")
    void shouldConvertAGroupListToAdminGroupList(){
        var groupCreator = Instancio.of(User.class)
                .create();
        var groups =Instancio.ofList(Group.class)
                .size(5)
                .create();
        List<GroupResponseEntity> expectedResponse = new ArrayList<>();
        for(Group group:groups) {
            var groupExpectedResponse =  AdminGroupResponse.builder()
                    .groupId(group.getGroupId())
                    .groupName(group.getGroupName())
                    .groupCreator(groupCreator.getEmail())
                    .groupCreationDate(group.getGroupCreationDate())
                    .users(group.getGroupHasUsers().stream().map(user -> AdminUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(group.getGroupName())
                            .lastLogin(user.getLastLogin())
                            .registerDate(user.getRegisterDate())
                            .createdBy("creator with uuid" +user.getCreatedBy())
                            .role(user.getRole())
                            .build()
                    ).collect(Collectors.toSet()))
                    .build();
            when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(groupCreator));
            when(userConverter.fromUserListToAdminList(group.getGroupHasUsers())).thenReturn(groupExpectedResponse.getUsers());
            expectedResponse.add(groupExpectedResponse);
        }
        var response = groupConverter.fromGroupListToAdminGroupList(groups);
        System.out.println(expectedResponse);
        System.out.println(response);
        Assertions.assertEquals(expectedResponse,response);

    }

    @Test
    @DisplayName("Should convert a group request to group entity")
    void shouldConvertAGroupRequestToGroupEntity(){
        var groupRequest = Instancio.create(GroupRequestEntity.class);
        var expectedResponse = Group.builder()
                .groupName(groupRequest.getGroupName())
                .groupCreator(UUID.randomUUID())
                .groupCreationDate(LocalDateTime.now())
                .build();
        var response = groupConverter.fromRequestToGroup(groupRequest,expectedResponse.getGroupCreator());
        Assertions.assertEquals(expectedResponse,response);
    }
}
