package com.example.app.integration.positive;

import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.models.requests.AuthenticationRequest;
import com.example.app.models.requests.RegisterRequest;
import com.example.app.models.responses.AuthenticationResponse;
import com.example.app.repositories.LeaveRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthPositiveIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ObjectMapper objectMapper;
    private static TestRestTemplate restTemplate;
    private String baseUrl;
    private AuthenticationRequest authenticationRequest;
    @Container
    public static PostgreSQLContainer<?> myPostgresContainer = new PostgreSQLContainer<>("postgres:13.11")
            .withCommand("postgres", "-c", "log_statement=all");
    private String roleValue;
    private User currentUser;

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

    public void setUp() {
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/auth"));
        switch (this.roleValue) {

            case "ADMIN" -> {
                currentUser = userRepo.findByEmail("firstname1@email.com").orElseThrow();
                this.authenticationRequest = AuthenticationRequest.builder()
                        .email(currentUser.getEmail())
                        .password("password1")
                        .build();
            }
            case "HR" -> {
                currentUser = userRepo.findByEmail("firstname4@email.com").orElseThrow();
                this.authenticationRequest = AuthenticationRequest.builder()
                        .email(currentUser.getEmail())
                        .password("password4")
                        .build();
            }
            case "MANAGER" -> {
                currentUser = userRepo.findByEmail("firstname3@email.com").orElseThrow();
                this.authenticationRequest = AuthenticationRequest.builder()
                        .email(currentUser.getEmail())
                        .password("password3")
                        .build();
            }
            case "USER" -> {
                currentUser = userRepo.findByEmail("firstname7@email.com").orElseThrow();
                this.authenticationRequest = AuthenticationRequest.builder()
                        .email(currentUser.getEmail())
                        .password("password7")
                        .build();
            }
        }
    }


    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","MANAGER","USER"})
    @DisplayName("For each user in database should perform log in and returning the token")
    void shouldPerformLogInAndReturningTheToken(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/authenticate");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(authenticationRequest),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AuthenticationResponse>() {});
        var extractedUsernameFromJwt = jwtService.extractUsername(actualResponse.getToken());
        assertEquals(authenticationRequest.getEmail(),extractedUsernameFromJwt);
        assertTrue(actualResponse.getToken().contains("ey"));
    }

    @Test
    @DisplayName("If the request in this api contains an allowed ip " +
            "should create a user save it in database and returning the registration token")
    void shouldCreateAUserSaveItInDatabaseAndReturningTheRegistrationToken() throws JsonProcessingException {
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/auth/register"));
        var registrationRequest = RegisterRequest.builder()
                .firstname("testFirstname")
                .lastname("testLastname")
                .email("testEmail@yahoo.com")
                .password("testPass123")
                .role("ADMIN")
                .specialization("test specialization")
                .currentProject("test current project")
                .build();
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(registrationRequest),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AuthenticationResponse>() {});
        var extractedUsernameFromJwt = jwtService.extractUsername(actualResponse.getToken());
        assertEquals(registrationRequest.getEmail(),extractedUsernameFromJwt);
        assertTrue(actualResponse.getToken().contains("ey"));
    }


}
