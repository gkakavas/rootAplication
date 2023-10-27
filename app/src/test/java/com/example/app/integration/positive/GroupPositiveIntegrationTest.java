package com.example.app.integration.positive;

import com.example.app.entities.Event;
import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.AdminGroupResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.example.app.tool.GroupRelevantGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupPositiveIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private ObjectMapper objectMapper;
    private static TestRestTemplate restTemplate;
    private String baseUrl = "http://localhost";
    @Container
    public static PostgreSQLContainer<?> myPostgresContainer = new PostgreSQLContainer<>("postgres:13.11")
            .withCommand("postgres", "-c", "log_statement=all");
    private Group existingGroup;
    private List<User> existingUsers;
    private User currentUser;
    private String currentToken;
    private static HttpHeaders headers;
    private List<Group> existingGroups;

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", myPostgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", myPostgresContainer::getUsername);
        registry.add("spring.datasource.password", myPostgresContainer::getPassword);
    }


    @BeforeAll
    public static void init() {
        myPostgresContainer.start();
        restTemplate = new TestRestTemplate();
        headers = new HttpHeaders();
    }

    public void setUp() {
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/group"));
        this.existingGroup = groupRepo.findByGroupName("group1").orElseThrow();
        this.existingGroups = groupRepo.findAll();
        this.existingUsers = userRepo.findAll();
        currentUser = userRepo.findByEmail("firstname1@email.com").orElseThrow();
        currentToken = jwtService.generateToken(currentUser);
        headers.set("Authorization","Bearer " + currentToken);
    }

    @AfterEach
    void tearDown () {
        SecurityContextHolder.clearContext();
    }

    @AfterAll
    public static void afterAll () {
        myPostgresContainer.stop();
    }

    @Test
    @DisplayName("When a create request dispatched should create a new group in database and return this group")
    void whenACreateRequestDispatchedShouldCreateANewGroupInDatabaseAndReturnThisGroup() throws JsonProcessingException {
        setUp();
        this.baseUrl = baseUrl.concat("/create");
        var usersToAdd = new ArrayList<User>();
        usersToAdd.add(userRepo.findByEmail("firstname7@email.com").orElseThrow());
        usersToAdd.add(userRepo.findByEmail("firstname8@email.com").orElseThrow());
        var createRequest = GroupRelevantGenerator.generateGroupRequestEntity(usersToAdd);
        HttpEntity<GroupRequestEntity> request = new HttpEntity<>(createRequest,headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminGroupResponse>(){});
        var usersInExpectedResponse = usersToAdd.stream().map(user ->
                    AdminUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(createRequest.getGroupName())
                            .createdBy(null)
                            .registerDate(user.getRegisterDate())
                            .lastLogin(user.getLastLogin())
                            .role(user.getRole())
                            .build())
                .collect(Collectors.toSet());
        assertEquals(createRequest.getGroupName(),actualResponse.getGroupName());
        assertEquals(usersInExpectedResponse,actualResponse.getUsers());
        var groupToDelete = groupRepo.findById(actualResponse.getGroupId()).orElseThrow();
        for(User user:usersToAdd){
            user.setGroup(null);
            userRepo.save(user);
        }
        groupRepo.deleteById(groupToDelete.getGroupId());
        assertFalse(groupRepo.existsById(actualResponse.getGroupId()));
    }

    @Test
    @DisplayName("When a read one group request dispatched should read an existing group from database and return this group")
    void shouldReadAnExistingGroupFromDatabaseAndReturnThisGroup() throws JsonProcessingException {
        setUp();
        this.baseUrl = baseUrl.concat("/").concat(existingGroup.getGroupId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminGroupResponse>() {});
        var expectedResponseUserList = existingGroup.getGroupHasUsers().stream().map(user ->
                AdminUserResponse.builder()
                        .userId(user.getUserId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .specialization(user.getSpecialization())
                        .currentProject(user.getCurrentProject())
                        .groupName(user.getGroup().getGroupName())
                        .registerDate(user.getRegisterDate())
                        .lastLogin(user.getLastLogin())
                        .role(user.getRole())
                        .build()
        ).collect(Collectors.toSet());
        var expectedResponse = AdminGroupResponse.builder()
                .groupId(existingGroup.getGroupId())
                .groupName(existingGroup.getGroupName())
                .groupCreationDate(existingGroup.getGroupCreationDate())
                .users(expectedResponseUserList)
                .build();
        assertEquals(expectedResponse,actualResponse);
    }
    @Test
    @DisplayName("when a read all request dispatched should read all existing groups from database and return them")
    void shouldReadAllExistingGroupsFromDatabaseAndReturnThem() throws JsonProcessingException {
        setUp();
        this.baseUrl = baseUrl.concat("/all");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<AdminGroupResponse>>() {});
        var expectedResponse = existingGroups.stream().map(group->
                AdminGroupResponse.builder()
                        .groupId(group.getGroupId())
                        .groupName(group.getGroupName())
                        .groupCreationDate(group.getGroupCreationDate())
                        .users(group.getGroupHasUsers().stream().map(user ->
                                AdminUserResponse.builder()
                                        .userId(user.getUserId())
                                        .firstname(user.getFirstname())
                                        .lastname(user.getLastname())
                                        .email(user.getEmail())
                                        .specialization(user.getSpecialization())
                                        .currentProject(user.getCurrentProject())
                                        .groupName(user.getGroup().getGroupName())
                                        .registerDate(user.getRegisterDate())
                                        .lastLogin(user.getLastLogin())
                                        .role(user.getRole())
                                        .build()
                                ).collect(Collectors.toSet())
                        )
                        .build()
                ).collect(Collectors.toSet());
        assertEquals(expectedResponse,actualResponse);
    }

    @Test
    @DisplayName("when an update request dispatched should update the existing group save it in database and return this")
    void shouldUpdateTheExistingGroupSaveItInDatabaseAndReturnThis() throws JsonProcessingException {
        setUp();
        this.baseUrl = baseUrl.concat("/update/").concat(existingGroup.getGroupId().toString());
        var updateRequest = GroupRequestEntity.builder().groupName("Updated group name").build() ;
        HttpEntity<GroupRequestEntity> request = new HttpEntity<>(updateRequest,headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminGroupResponse>(){});
        var expectedResponse = AdminGroupResponse.builder()
                .groupId(existingGroup.getGroupId())
                .groupName(updateRequest.getGroupName())
                .groupCreationDate(existingGroup.getGroupCreationDate())
                .users(existingGroup.getGroupHasUsers().stream().map(user ->
                        AdminUserResponse.builder()
                                .userId(user.getUserId())
                                .firstname(user.getFirstname())
                                .lastname(user.getLastname())
                                .email(user.getEmail())
                                .specialization(user.getSpecialization())
                                .currentProject(user.getCurrentProject())
                                .groupName(updateRequest.getGroupName())
                                .registerDate(user.getRegisterDate())
                                .lastLogin(user.getLastLogin())
                                .role(user.getRole())
                                .build()
                ).collect(Collectors.toSet()))
                .build();
        assertEquals(expectedResponse,actualResponse);
        groupRepo.save(existingGroup);
    }

    @Test
    @DisplayName("when a delete request dispatched should delete the existing event from database and return no content 204")
    void shouldDeleteTheExistingGroupFromDatabaseAndReturnNoContent204(){
        setUp();
        var usersToAdd = new ArrayList<User>();
        usersToAdd.add(userRepo.findByEmail("firstname7@email.com").orElseThrow());
        usersToAdd.add(userRepo.findByEmail("firstname8@email.com").orElseThrow());
        var group = GroupRelevantGenerator.generateValidGroup(usersToAdd);
        var existingGroup = groupRepo.save(group);
        this.baseUrl = baseUrl.concat("/delete/").concat(existingGroup.getGroupId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(204));
        assertFalse(groupRepo.existsById(existingGroup.getGroupId()));
    }
}


