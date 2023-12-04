package com.example.app.integration.positive;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.example.app.tool.UserRelevantGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserPositiveIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private ObjectMapper objectMapper;
    private static TestRestTemplate restTemplate;
    private String baseUrl = "http://localhost";
    private String currentToken;
    private String roleValue;
    private User currentUser;
    private static HttpHeaders headers;
    @Container
    public static PostgreSQLContainer<?> myPostgresContainer = new PostgreSQLContainer<>("postgres:13.11")
            .withCommand("postgres", "-c", "log_statement=all");
    private Group groupForUserCreation;

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
        this.groupForUserCreation = groupRepo.findByGroupName("group1").orElseThrow();
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/user"));
        switch (this.roleValue) {
            case "ADMIN" -> {
                currentUser = userRepo.findByEmail("firstname1@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "HR" -> {
                currentUser = userRepo.findByEmail("firstname4@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "MANAGER" -> {
                currentUser = userRepo.findByEmail("firstname3@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "USER" -> {
                currentUser = userRepo.findByEmail("firstname7@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
        }
        headers.set("Authorization", "Bearer " + currentToken);
    }
    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }
    @Test
    @DisplayName("when a create request dispatched should create a new user in database and return this user")
    void shouldCreateANewUserInDatabaseAndReturnThisUser() throws IOException {
        this.roleValue  = "ADMIN";
        setUp();
        this.baseUrl = baseUrl.concat("/create");
        var createRequest = UserRelevantGenerator.generateValidUserRequestEntity("USER",this.groupForUserCreation.getGroupId());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(createRequest,headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminUserResponse>() {});
        var expectedResponse = UserRelevantGenerator.generateValidAdminUserResponse(
                                    actualResponse.getUserId(),
                                    createRequest.getEmail(),
                                    this.currentUser.getEmail(),
                                    Role.valueOf(createRequest.getRole()),
                                    this.groupForUserCreation.getGroupName()
                                );
        expectedResponse.setRegisterDate(actualResponse.getRegisterDate());
        assertEquals(expectedResponse,actualResponse);
        assertTrue(userRepo.existsById(actualResponse.getUserId()));
        var userToDelete = userRepo.findById(actualResponse.getUserId()).orElseThrow();
        this.groupForUserCreation.getGroupHasUsers().remove(userToDelete);
        userToDelete.setGroup(null);
        userToDelete = userRepo.save(userToDelete);
        userRepo.delete(userToDelete);
        assertFalse(userRepo.existsById(userToDelete.getUserId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("when a read one request dispatched should read an existing user from database and return " +
            "this user in form that corresponds in user type")
    void shouldReadAnExistingUserFromDatabaseAndReturnThisUser(String roleValue) throws IOException {
        this.roleValue  = roleValue;
        setUp();
        var retrievedUser = userRepo.findByEmail("firstname8@email.com").orElseThrow();
        this.baseUrl = baseUrl.concat("/"+retrievedUser.getUserId());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if(roleValue.equals("ADMIN")){
            var expectedResponse = AdminUserResponse.builder()
                    .userId(retrievedUser.getUserId())
                    .firstname(retrievedUser.getFirstname())
                    .lastname(retrievedUser.getLastname())
                    .email(retrievedUser.getEmail())
                    .specialization(retrievedUser.getSpecialization())
                    .currentProject(retrievedUser.getCurrentProject())
                    .groupName(retrievedUser.getGroup().getGroupName())
                    .registerDate(retrievedUser.getRegisterDate().truncatedTo(ChronoUnit.SECONDS))
                    .role(Role.valueOf(retrievedUser.getRoleValue()))
                    .build();
            var actualResponse = objectMapper.readValue(response.getBody(),AdminUserResponse.class);
            assertEquals(expectedResponse,actualResponse);
        }
        else{
            var expectedResponse = OtherUserResponse.builder()
                    .userId(retrievedUser.getUserId())
                    .firstname(retrievedUser.getFirstname())
                    .lastname(retrievedUser.getLastname())
                    .email(retrievedUser.getEmail())
                    .specialization(retrievedUser.getSpecialization())
                    .currentProject(retrievedUser.getCurrentProject())
                    .groupName(retrievedUser.getGroup().getGroupName())
                    .build();
            var actualResponse = objectMapper.readValue(response.getBody(),OtherUserResponse.class);
            assertEquals(expectedResponse,actualResponse);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("when a read all request is dispatched should read all users from database and return " +
            "these users in form that corresponds in user type")
    void shouldReadAllUsersFromDatabaseAndReturnThem(String roleValue) throws IOException {
        this.roleValue = roleValue;
        setUp();
        var users = userRepo.findAll();
        this.baseUrl = baseUrl.concat("/all");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if(roleValue.equals("ADMIN")){
            var expectedResponse = users.stream()
                    .map(user1 -> AdminUserResponse.builder()
                                    .userId(user1.getUserId())
                                    .firstname(user1.getFirstname())
                                    .lastname(user1.getLastname())
                                    .email(user1.getEmail())
                                    .specialization(user1.getSpecialization())
                                    .currentProject(user1.getCurrentProject())
                                    .registerDate(user1.getRegisterDate().truncatedTo(ChronoUnit.SECONDS))
                                    .groupName(user1.getGroup().getGroupName())
                                    .role(Role.valueOf(user1.getRoleValue()))
                                    .build()
                    )
                    .toList();
            var actualResponse = objectMapper.readValue(response.getBody(),new TypeReference<List<AdminUserResponse>>() {});
            assertEquals(Set.copyOf(expectedResponse),Set.copyOf(actualResponse));
        }
        else{
            var expectedResponse = users.stream()
                    .map(user1 -> OtherUserResponse.builder()
                            .userId(user1.getUserId())
                            .firstname(user1.getFirstname())
                            .lastname(user1.getLastname())
                            .email(user1.getEmail())
                            .specialization(user1.getSpecialization())
                            .currentProject(user1.getCurrentProject())
                            .groupName(user1.getGroup().getGroupName())
                            .build()
                    )
                    .toList();
            var actualResponse = objectMapper.readValue(response.getBody(),new TypeReference<List<OtherUserResponse>>() {});
            assertEquals(Set.copyOf(expectedResponse),Set.copyOf(actualResponse));
        }
    }

    @Test
    @DisplayName("when an update request dispatched " +
            "should update an existing user, save it in database and return this user")
    void shouldUpdateAnExistingUserAndSaveItInDatabaseAndReturnThisUser() throws IOException {
        this.roleValue = "ADMIN";
        setUp();
        var userToUpdate = userRepo.findByEmail("firstname8@email.com").orElseThrow();
        var groupForUpdatingUser = groupRepo.findByGroupName("group3").orElseThrow();
        this.baseUrl = baseUrl.concat("/update/").concat(userToUpdate.getUserId().toString());
        var updateRequest = UserRelevantGenerator.generateValidUserRequestEntity("USER",groupForUpdatingUser.getGroupId());
        HttpEntity<UserRequestEntity> request = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(),AdminUserResponse.class);
        var expectedResponse = AdminUserResponse.builder()
                .userId(userToUpdate.getUserId())
                .firstname(updateRequest.getFirstname())
                .lastname(updateRequest.getLastname())
                .email(updateRequest.getEmail())
                .specialization(updateRequest.getSpecialization())
                .currentProject(updateRequest.getCurrentProject())
                .groupName(groupForUpdatingUser.getGroupName())
                .registerDate(userToUpdate.getRegisterDate().truncatedTo(ChronoUnit.SECONDS))
                .role(userToUpdate.getRole())
                .build();
        assertEquals(expectedResponse,actualResponse);
        userRepo.save(userToUpdate);
    }

    @Test
    @DisplayName("when a delete request dispatched " +
            "should delete an existing user from database and return 204 status")
    void shouldDeleteAnExistingUserFromDatabaseAndReturn204Status(){
        this.roleValue = "ADMIN";
        setUp();
        var groupForCreatingUser = groupRepo.findByGroupName("group1").orElseThrow();
        var userToDelete = UserRelevantGenerator.generateValidUser(null,Role.HR,groupForCreatingUser);
        var existingUser = userRepo.save(userToDelete);
        this.baseUrl = baseUrl.concat("/delete/").concat(existingUser.getUserId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatusCode.valueOf(204),response.getStatusCode());
        assertFalse(userRepo.existsById(existingUser.getUserId()));
    }
    @Test
    @DisplayName("when a patch request dispatched should patching an existing user, save it in database and return this user")
    void shouldPatchingAnExistingUserSaveItInDatabaseAndReturnThisUser() throws IOException {
        this.roleValue = "ADMIN";
        setUp();
        var existingGroup = groupRepo.findByGroupName("group1").orElseThrow();
        var existingUser = userRepo.findByEmail("firstname8@email.com").orElseThrow();
        this.baseUrl = baseUrl.concat("/patch/").concat(existingUser.getUserId().toString());
        var patchRequest = new HashMap<String,String>();
        patchRequest.put("firstname","randomFirstname");
        patchRequest.put("lastname","randomLastname");
        patchRequest.put("email","random@email.com");
        patchRequest.put("specialization","randomSpecialization");
        patchRequest.put("currentProject","randomSpecialization");
        patchRequest.put("role","MANAGER");
        patchRequest.put("group",existingGroup.getGroupId().toString());
        HttpEntity<HashMap<String,String>> request = new HttpEntity<>(patchRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PATCH,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(),AdminUserResponse.class);
        var expectedResponse = AdminUserResponse.builder()
                .userId(existingUser.getUserId())
                .firstname(patchRequest.get("firstname"))
                .lastname(patchRequest.get("lastname"))
                .email(patchRequest.get("email"))
                .specialization(patchRequest.get("specialization"))
                .currentProject(patchRequest.get("currentProject"))
                .groupName(existingGroup.getGroupName())
                .registerDate(existingUser.getRegisterDate().truncatedTo(ChronoUnit.SECONDS))
                .role(Role.valueOf(patchRequest.get("role")))
                .build();
        assertEquals(expectedResponse,actualResponse);
        userRepo.save(existingUser);
    }


    //@ParameterizedTest
    //@ValueSource(strings = {"ADMIN"})
    /*@Test
    @DisplayName("when a read user events request dispatched " +
            "should retrieve all the events of the current user from database and return them")
    void shouldRetrieveAllTheEventsOfTheCurrentUserFromDatabaseAndReturnThem(String roleValue) throws IOException {
        this.user = UserRelevantGenerator.generateValidUser(null,Role.valueOf("USER"),null);
        var events = new HashSet<Event>();
        var size = 5;
        for(int i=0;i<=size-1;i++)
            events.add(Instancio.of(Event.class)
                    .ignore(field("usersJoinInEvent"))
                    .create());
        this.user.getUserHasEvents().addAll(events);
        for(Event event : events){
            event.getUsersJoinInEvent().add(this.user);
        }
        setUp();
        var existingEvents = eventRepo.findAll();
        this.baseUrl = baseUrl
                .concat(":")
                .concat(String.valueOf(port))
                .concat("/user/")
                .concat(this.user.getUserId()+"")
                .concat("/events");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<MyEventResponse>>() {});
        var expectedResponse = existingEvents.stream()
                .map(event -> MyEventResponse.builder()
                        .eventId(event.getEventId())
                        .eventDescription(event.getEventDescription())
                        .eventBody(event.getEventBody())
                        .eventDateTime(event.getEventDateTime())
                        .eventExpiration(event.getEventExpiration())
                        .build())
                .collect(Collectors.toSet());
        assertEquals(HttpStatusCode.valueOf(200),response.getStatusCode());
        assertEquals(expectedResponse,actualResponse);
        userRepo.deleteAll();
        eventRepo.deleteAll();
    }*/
}
