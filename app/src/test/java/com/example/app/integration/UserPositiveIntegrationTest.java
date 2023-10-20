package com.example.app.integration;

import com.example.app.entities.Event;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.responses.error.ErrorResponse;
import com.example.app.tool.GroupRelevantGenerator;
import com.example.app.tool.UserRelevantGenerator;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.MyEventResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.EventRepository;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "application.yml")
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
    private EventRepository eventRepo;
    @Autowired
    private ObjectMapper objectMapper;
    private static RestTemplate restTemplate;
    private String baseUrl = "http://localhost";
    private String token;
    private User user;


    @BeforeAll
    public static void init(){
        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }
    private void setUp(){
        userRepo.save(user);
        user = userRepo.findByEmail(user.getEmail()).orElseThrow();
        token = jwtService.generateToken(user);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities()));
    }
    @AfterEach
    void tearDown(){
        userRepo.deleteById(user.getUserId());
        SecurityContextHolder.clearContext();
    }
    @Test
    @DisplayName("when a create request dispatched should create a new user in database and return this user")
    void shouldCreateANewUserInDatabaseAndReturnThisUser() throws IOException {
        this.user = UserRelevantGenerator.generateValidUser(null,Role.ADMIN,null);
        setUp();
        this.baseUrl = baseUrl
                .concat(":")
                .concat(String.valueOf(port))
                .concat("/user/create");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        var createRequest = UserRelevantGenerator.generateValidUserRequestEntity("USER",UUID.randomUUID());
        createRequest.setEmail(this.user.getEmail());
        HttpEntity<UserRequestEntity> request = new HttpEntity<>(createRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminUserResponse>() {});
        var expectedResponse = UserRelevantGenerator.generateValidAdminUserResponse(
                                    actualResponse.getUserId(),
                                    createRequest.getEmail(),
                                    actualResponse.getCreatedBy(),
                                    Role.valueOf(createRequest.getRole()),
                                    actualResponse.getGroupName()
                                );
        expectedResponse.setRegisterDate(actualResponse.getRegisterDate());
        assertEquals(expectedResponse,actualResponse);
        assertTrue(userRepo.existsById(actualResponse.getUserId()));
        userRepo.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("when a read one request dispatched should read an existing user from database and return " +
            "this user in form that corresponds in user type")
    void shouldReadAnExistingUserFromDatabaseAndReturnThisUser(String roleValue) throws IOException {
        this.user = UserRelevantGenerator.generateValidUser(null,Role.valueOf(roleValue),null);
        setUp();
        var userToRetrieve = UserRelevantGenerator.generateValidUser(null,Role.USER,null);
        var retrievedUser = userRepo.save(userToRetrieve);
        this.baseUrl = baseUrl.concat(":")
                .concat(String.valueOf(port))
                .concat("/user/"+retrievedUser.getUserId());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if(this.user.getRole().equals(Role.ADMIN)){
            var expectedResponse = AdminUserResponse.builder()
                    .userId(retrievedUser.getUserId())
                    .firstname(retrievedUser.getFirstname())
                    .lastname(retrievedUser.getLastname())
                    .email(retrievedUser.getEmail())
                    .specialization(retrievedUser.getSpecialization())
                    .currentProject(retrievedUser.getCurrentProject())
                    .groupName(null)
                    .createdBy(null)
                    .registerDate(retrievedUser.getRegisterDate())
                    .lastLogin(null)
                    .role(Role.valueOf(retrievedUser.getRoleValue()))
                    .build();
            var actualResponse = objectMapper.readValue(response.getBody(),AdminUserResponse.class);
            assertEquals(expectedResponse,actualResponse);
            assertTrue(userRepo.existsById(actualResponse.getUserId()));
        }
        else{
            var expectedResponse = OtherUserResponse.builder()
                    .userId(retrievedUser.getUserId())
                    .firstname(retrievedUser.getFirstname())
                    .lastname(retrievedUser.getLastname())
                    .email(retrievedUser.getEmail())
                    .specialization(retrievedUser.getSpecialization())
                    .currentProject(retrievedUser.getCurrentProject())
                    .groupName(null)
                    .build();
            var actualResponse = objectMapper.readValue(response.getBody(),OtherUserResponse.class);
            assertEquals(expectedResponse,actualResponse);
            assertTrue(userRepo.existsById(actualResponse.getUserId()));
        }
        userRepo.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("when a read all request dispatched should read all users from database and return " +
            "these users in form that corresponds in user type")
    void shouldReadAllUsersFromDatabaseAndReturnThem(String roleValue) throws IOException {
        this.user = UserRelevantGenerator.generateValidUser(null,Role.valueOf(roleValue),null);
        setUp();
        List<User> userList = UserRelevantGenerator.validUserList(2);
        userRepo.saveAll(userList);
        List<User> retrievedUserList = userRepo.findAll();
        this.baseUrl = baseUrl.concat(":")
                .concat(String.valueOf(port))
                .concat("/user/all");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if(this.user.getRole().equals(Role.ADMIN)){
            var expectedResponse = retrievedUserList.stream()
                    .map(user1 -> AdminUserResponse.builder()
                                    .userId(user1.getUserId())
                                    .firstname(user1.getFirstname())
                                    .lastname(user1.getLastname())
                                    .email(user1.getEmail())
                                    .specialization(user1.getSpecialization())
                                    .currentProject(user1.getCurrentProject())
                                    .registerDate(user1.getRegisterDate())
                                    .lastLogin(user1.getLastLogin())
                                    .role(Role.valueOf(user1.getRoleValue()))
                                    .build()
                    )
                    .toList();
            var actualResponse = objectMapper.readValue(response.getBody(),new TypeReference<List<AdminUserResponse>>() {});
            assertEquals(Set.copyOf(expectedResponse),Set.copyOf(actualResponse));
        }
        if(Arrays.asList(Role.HR,Role.MANAGER,Role.USER).contains(this.user.getRole())){
            var expectedResponse = retrievedUserList.stream()
                    .map(user1 -> OtherUserResponse.builder()
                            .userId(user1.getUserId())
                            .firstname(user1.getFirstname())
                            .lastname(user1.getLastname())
                            .email(user1.getEmail())
                            .specialization(user1.getSpecialization())
                            .currentProject(user1.getCurrentProject())
                            .build()
                    )
                    .toList();
            var actualResponse = objectMapper.readValue(response.getBody(),new TypeReference<List<OtherUserResponse>>() {});
            assertEquals(Set.copyOf(expectedResponse),Set.copyOf(actualResponse));
        }
        userRepo.deleteAll(userList);
    }

    @Test
    @DisplayName("when an update request dispatched should update an existing user, save it in database and return this user")
    void shouldUpdateAnExistingUserAndSaveItInDatabaseAndReturnThisUser() throws IOException {
        this.user = UserRelevantGenerator.generateValidUser(null,Role.ADMIN,null);
        setUp();
        var group = GroupRelevantGenerator.generateValidGroup(null);
        var existingGroup = groupRepo.save(group);
        var userToUpdate = UserRelevantGenerator.generateValidUser(null,Role.USER,existingGroup);
        var existingUser = userRepo.save(userToUpdate);
        this.baseUrl = baseUrl
                .concat(":")
                .concat(String.valueOf(port))
                .concat("/user/update/")
                .concat(existingUser.getUserId().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        var updateRequest = UserRelevantGenerator.generateValidUserRequestEntity("USER",existingGroup.getGroupId());
        HttpEntity<UserRequestEntity> request = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(),AdminUserResponse.class);
        var expectedResponse = AdminUserResponse.builder()
                .userId(existingUser.getUserId())
                .firstname(updateRequest.getFirstname())
                .lastname(updateRequest.getLastname())
                .email(updateRequest.getEmail())
                .specialization(updateRequest.getSpecialization())
                .currentProject(updateRequest.getCurrentProject())
                .groupName(existingGroup.getGroupName())
                .createdBy(null)
                .registerDate(existingUser.getRegisterDate())
                .lastLogin(existingUser.getLastLogin())
                .role(Role.valueOf(existingUser.getRoleValue()))
                .build();
        assertEquals(expectedResponse,actualResponse);
        assertTrue(userRepo.existsById(actualResponse.getUserId()));
        userRepo.deleteAll();
        groupRepo.deleteAll();
    }

    @Test
    @DisplayName("when a delete request dispatched should delete an existing user from database and return 204 status")
    void shouldDeleteAnExistingUserFromDatabaseAndReturn204Status(){
        this.user = UserRelevantGenerator.generateValidUser(null,Role.ADMIN,null);
        setUp();
        var userToDelete = UserRelevantGenerator.generateValidUser(null,Role.HR,null);
        var existingUser = userRepo.save(userToDelete);
        this.baseUrl = baseUrl
                .concat(":")
                .concat(String.valueOf(port))
                .concat("/user/delete/")
                .concat(existingUser.getUserId().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
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
        this.user = UserRelevantGenerator.generateValidUser(null,Role.ADMIN,null);
        setUp();
        var group = GroupRelevantGenerator.generateValidGroup(null);
        var existingGroup = groupRepo.save(group);
        var userToPatch = UserRelevantGenerator.generateValidUser(null,Role.USER,null);
        var existingUser = userRepo.save(userToPatch);
        this.baseUrl = baseUrl
                .concat(":")
                .concat(String.valueOf(port))
                .concat("/user/patch/")
                .concat(String.valueOf(existingUser.getUserId()));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
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
                .createdBy(null)
                .registerDate(existingUser.getRegisterDate())
                .lastLogin(existingUser.getLastLogin())
                .role(Role.valueOf(patchRequest.get("role")))
                .build();
        assertEquals(expectedResponse,actualResponse);
        assertTrue(userRepo.existsById(actualResponse.getUserId()));
        userRepo.deleteAll();
        groupRepo.deleteAll();
    }


    //@ParameterizedTest
    //@ValueSource(strings = {"ADMIN"})
    @Test
    @DisplayName("when a read user events request dispatched " +
            "should retrieve all the events of the current user from database and return them")
    void shouldRetrieveAllTheEventsOfTheCurrentUserFromDatabaseAndReturnThem(/*String roleValue*/) throws IOException {
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
    }
}
