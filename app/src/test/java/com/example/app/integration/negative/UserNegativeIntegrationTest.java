package com.example.app.integration.negative;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.responses.error.ErrorResponse;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.example.app.tool.UserRelevantGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserNegativeIntegrationTest {
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
    private static TestRestTemplate httpClient;
    private String baseUrl = "http://localhost";
    private static HttpHeaders headers;

    @Container
    public static PostgreSQLContainer<?> myPostgresContainer = new PostgreSQLContainer<>("postgres:13.11")
            .withCommand("postgres", "-c", "log_statement=all");
    private Group groupForUserCreation;
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
        httpClient = new TestRestTemplate();
        headers = new HttpHeaders();
    }
    private void setUp(){
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/user"));
        currentUser = userRepo.findByEmail("firstname1@email.com").orElseThrow();
        String currentToken = jwtService.generateToken(currentUser);
        headers.set("Authorization", "Bearer " + currentToken);
        groupForUserCreation = groupRepo.findByGroupName("group1").orElseThrow();
    }
    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("when a create request contains an email which already exists" +
            "should return an error message and the response status code")
    void createRequestShouldReturnAnErrorMessageAndTheResponseStatusCode() throws JsonProcessingException {
        setUp();
        this.baseUrl = baseUrl.concat("/create");
        var createRequest = UserRelevantGenerator.generateValidUserRequestEntity("USER", groupForUserCreation.getGroupId());
        //setting the email of request same with an existing user
        createRequest.setEmail(this.currentUser.getEmail());
        ResponseEntity<String> response = httpClient.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                String.class);
        ObjectNode node = objectMapper.readValue(response.getBody(), ObjectNode.class);
        var actualResponse = ErrorResponse.<String> builder()
                .message(node.get("message").asText())
                .responseCode(HttpStatus.valueOf(node.get("responseCode").asText()))
                .build();
        var expectedResponse = ErrorResponse.<String> builder()
                .message("This email already exists")
                .responseCode(HttpStatusCode.valueOf(500))
                .build();
        assertEquals(expectedResponse,actualResponse);
    }


}
