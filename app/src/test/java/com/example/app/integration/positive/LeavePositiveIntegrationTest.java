package com.example.app.integration.positive;


import com.example.app.entities.*;
import com.example.app.models.requests.LeaveRequestEntity;

import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.MyLeaveResponse;

import com.example.app.repositories.LeaveRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LeavePositiveIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private LeaveRepository leaveRepo;
    @Autowired
    private ObjectMapper objectMapper;
    private static TestRestTemplate restTemplate;
    private String baseUrl;
    @Container
    public static PostgreSQLContainer<?> myPostgresContainer = new PostgreSQLContainer<>("postgres:13.11")
            .withCommand("postgres", "-c", "log_statement=all");
    private String currentToken;
    private String roleValue;
    private User currentUser;
    private static HttpHeaders headers;
    private Leave leaveToRetrieve;

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
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/leave"));
        this.leaveToRetrieve = leaveRepo.findById(UUID.fromString("640f3ded-a09b-4fb0-a6e5-161460b90e3f")).orElseThrow();
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

    @BeforeEach
    void beforeEachSetup() {
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @AfterAll
    public static void afterAll() {
        myPostgresContainer.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER"})
    @DisplayName("When a create leave request is dispatched" +
            " should create leave, save it in db and return a response")
    void shouldCreateLeaveSaveItInDbAndReturnAResponse(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/create");
        var createRequest = LeaveRequestEntity.builder()
                .leaveType("SICK_LEAVE")
                .leaveStarts(LocalDate.of(2024, 1, 4).toString())
                .leaveEnds(LocalDate.of(2024, 1, 7).toString())
                .build();
        HttpEntity<LeaveRequestEntity> request = new HttpEntity<>(createRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<MyLeaveResponse>() {
        });
        var expectedResponse = MyLeaveResponse.builder()
                .leaveId(actualResponse.getLeaveId())
                .leaveType(LeaveType.valueOf(createRequest.getLeaveType()))
                .leaveStarts(LocalDate.parse(createRequest.getLeaveStarts()))
                .leaveEnds(LocalDate.parse(createRequest.getLeaveEnds()))
                .approved(false)
                .build();
        assertEquals(expectedResponse, actualResponse);
        assertTrue(leaveRepo.existsById(actualResponse.getLeaveId()));
        var leaveToDelete = leaveRepo.findById(actualResponse.getLeaveId()).orElseThrow();
        currentUser.setUserRequestedLeaves(null);
        userRepo.save(currentUser);
        leaveToDelete.setRequestedBy(null);
        leaveRepo.save(leaveToDelete);
        leaveRepo.delete(leaveToDelete);
        assertFalse(leaveRepo.existsById(leaveToDelete.getLeaveId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "MANAGER", "USER"})
    @DisplayName("When a read one leave request is dispatched" +
            " should retrieve from db the specific leave  and return a response based on user role")
    void shouldRetrieveFromDbTheSpecificLeaveAndReturnAResponseBasedOnUserRole(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/").concat(this.leaveToRetrieve.getLeaveId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if (List.of("ADMIN", "HR", "MANAGER").contains(roleValue)) {
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngLeaveResponse>() {
            });
            var expectedResponse = AdminHrMngLeaveResponse.builder()
                    .leaveId(this.leaveToRetrieve.getLeaveId())
                    .leaveType(this.leaveToRetrieve.getLeaveType())
                    .leaveStarts(this.leaveToRetrieve.getLeaveStarts())
                    .leaveEnds(this.leaveToRetrieve.getLeaveEnds())
                    .requestedBy(this.leaveToRetrieve.getRequestedBy().getEmail())
                    .approved(false)
                    .build();
            assertEquals(expectedResponse, actualResponse);
        } else {
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<MyLeaveResponse>() {
            });
            var expectedResponse = MyLeaveResponse.builder()
                    .leaveId(this.leaveToRetrieve.getLeaveId())
                    .leaveType(this.leaveToRetrieve.getLeaveType())
                    .leaveStarts(this.leaveToRetrieve.getLeaveStarts())
                    .leaveEnds(this.leaveToRetrieve.getLeaveEnds())
                    .approved(false)
                    .build();
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "MANAGER", "USER"})
    @DisplayName("When a read all leaves request is dispatched" +
            " should retrieve all leaves from db based on role and return a response based on user role")
    void shouldRetrieveAllLeavesFromDbBasedOnRoleAndReturnAResponseBasedOnUserRole(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/all");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if (List.of("ADMIN", "HR", "MANAGER").contains(roleValue)) {
            Set<Leave> adminHrManagerLeaves = new HashSet<>();
            if (List.of("ADMIN", "HR").contains(roleValue)) {
                adminHrManagerLeaves = Set.copyOf(leaveRepo.findAll());
            } else if (roleValue.equals("MANAGER")) {
                adminHrManagerLeaves = Set.copyOf(leaveRepo.findAllByRequestedBy_Group(currentUser.getGroup()));
            }
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<AdminHrMngLeaveResponse>>() {
            });
            var expectedResponse = adminHrManagerLeaves.stream().map(leave ->
                    AdminHrMngLeaveResponse.builder()
                            .leaveId(leave.getLeaveId())
                            .leaveType(leave.getLeaveType())
                            .leaveStarts(leave.getLeaveStarts())
                            .leaveEnds(leave.getLeaveEnds())
                            .requestedBy(leave.getRequestedBy().getEmail())
                            .approved(false)
                            .build()
            ).collect(Collectors.toSet());
            assertEquals(expectedResponse, actualResponse);
        }
        if (roleValue.equals("USER")) {
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<List<MyLeaveResponse>>() {
            });
            var expectedResponse = currentUser.getUserRequestedLeaves().stream().map(leave ->
                    MyLeaveResponse.builder()
                            .leaveId(leave.getLeaveId())
                            .leaveType(leave.getLeaveType())
                            .leaveStarts(leave.getLeaveStarts())
                            .leaveEnds(leave.getLeaveEnds())
                            .approved(false)
                            .build()
            ).toList();
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER"})
    @DisplayName("When an update leave request is dispatched" +
            " should retrieve from db the specific leave update this leave, save it and returning this")
    void shouldRetrieveFromDbTheSpecificLeaveUpdatingThisSavingAndReturningThis(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/update/").concat(this.leaveToRetrieve.getLeaveId().toString());
        var updateRequest = LeaveRequestEntity.builder()
                .leaveType("BRIDGE_DAY_LEAVE")
                .leaveStarts(LocalDate.of(2024, 3, 12).toString())
                .leaveEnds(LocalDate.of(2024, 3, 17).toString())
                .build();
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<MyLeaveResponse>() {
        });
        var expectedResponse = MyLeaveResponse.builder()
                .leaveId(this.leaveToRetrieve.getLeaveId())
                .leaveType(LeaveType.valueOf(updateRequest.getLeaveType()))
                .leaveStarts(LocalDate.parse(updateRequest.getLeaveStarts()))
                .leaveEnds(LocalDate.parse(updateRequest.getLeaveEnds()))
                .approved(false)
                .build();
        assertEquals(expectedResponse, actualResponse);
        leaveRepo.save(this.leaveToRetrieve);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER"})
    @DisplayName("When a delete leave request is dispatched" +
            " should delete from db the specific leave and returning 204")
    void shouldDeleteFromDbTheSpecificLeaveAndReturning204(String roleValue) {
        var leaveToDelete = Instancio.of(Leave.class)
                .ignore(field(Leave::getLeaveId))
                .ignore(field(Leave::getApproved))
                .ignore(field(Leave::getApprovedBy))
                .ignore(field(Leave::getApprovedOn))
                .set(field(Leave::getRequestedBy), currentUser)
                .create();
        var savedLeaveToDelete = leaveRepo.save(leaveToDelete);
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/delete/").concat(savedLeaveToDelete.getLeaveId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(leaveRepo.existsById(savedLeaveToDelete.getLeaveId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER"})
    @DisplayName("When an approve leave request is dispatched" +
            " should approve the specific leave and returning the approved leave")
    void shouldApproveTheSpecificLeaveAndReturningTheApprovedLeave(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/approval/").concat(this.leaveToRetrieve.getLeaveId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrMngLeaveResponse>() {
        });
        var expectedResponse = AdminHrMngLeaveResponse.builder()
                .leaveId(this.leaveToRetrieve.getLeaveId())
                .leaveType(this.leaveToRetrieve.getLeaveType())
                .leaveStarts(this.leaveToRetrieve.getLeaveStarts())
                .leaveEnds(this.leaveToRetrieve.getLeaveEnds())
                .approvedBy(currentUser.getEmail())
                .approvedOn(actualResponse.getApprovedOn())
                .approved(true)
                .requestedBy(this.leaveToRetrieve.getRequestedBy().getEmail())
                .build();
        assertEquals(expectedResponse, actualResponse);
        leaveRepo.save(this.leaveToRetrieve);
    }

}
