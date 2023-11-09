package com.example.app.integration.negative;

import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.error.ErrorResponse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private ObjectMapper objectMapper;
    private static TestRestTemplate httpClient;
    private String baseUrl = "http://localhost";
    private String token;
    private User user;

    @BeforeAll
    public static void init(){
        httpClient = new TestRestTemplate();
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
    @DisplayName("when a create request contains an email which already exists" +
            "should return an error message and the response status code")
    void createRequestShouldReturnAnErrorMessageAndTheResponseStatusCode() throws JsonProcessingException {
        this.user = UserRelevantGenerator.generateValidUser(null, Role.ADMIN,null);
        setUp();
        this.baseUrl = baseUrl
                .concat(":")
                .concat(String.valueOf(port))
                .concat("/user/create");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        var createRequest = UserRelevantGenerator.generateValidUserRequestEntity("USER", UUID.randomUUID());
        createRequest.setEmail(this.user.getEmail());
        HttpEntity<UserRequestEntity> request = new HttpEntity<>(createRequest, headers);
            ResponseEntity<String> response = httpClient.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    request,
                    String.class);
            ObjectNode rootNode = objectMapper.readValue(response.getBody(), ObjectNode.class);
            Map<String,String> actualResponseMap = new HashMap<>();
            actualResponseMap.put("email",rootNode.get("message").get("email").textValue());
            var actualResponse = ErrorResponse.builder()
                    .message(actualResponseMap)
                    .responseCode(HttpStatus.valueOf(rootNode.get("responseCode").asText()))
                    .build();
            var expectedResponse = new ErrorResponse<Map<String,String>>();
            Map<String,String> errorResponseMap = new HashMap<>();
            errorResponseMap.put("email","This email already exists");
            expectedResponse.setMessage(errorResponseMap);
            expectedResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            assertEquals(expectedResponse,actualResponse);
    }


}
