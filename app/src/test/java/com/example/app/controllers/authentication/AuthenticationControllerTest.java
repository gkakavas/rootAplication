package com.example.app.controllers.authentication;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.SecurityConfig;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.AuthenticationController;
import com.example.app.entities.Role;
import com.example.app.models.requests.AuthenticationRequest;
import com.example.app.models.requests.RegisterRequest;
import com.example.app.models.responses.AuthenticationResponse;
import com.example.app.security.JwtAuthenticationFilter;
import com.example.app.services.AuthenticationService;
import com.example.app.services.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {AuthenticationController.class, TestSecurityConfig.class})
public class AuthenticationControllerTest {
    @MockBean
    private AuthenticationService authenticationService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    @DisplayName("Should register a user and return the jwt token")
    void shouldRegisterAUserAndReturnTheJwtTokenOfRegistration() throws Exception {
        var response = Instancio.create(AuthenticationResponse.class);
        var request = RegisterRequest.builder()
                .firstname("user")
                .lastname("user")
                .email("test@email.com")
                .password("Cdb3zgy2")
                .role("ADMIN")
                .specialization("CFO")
                .currentProject("testProject")
                .build();
        when(authenticationService.register(request)).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should authenticate a user and return the jwt token")
    void shouldAuthenticateAUserAndReturnTheJwtTokenOfRegistration() throws Exception {
        var response = Instancio.create(AuthenticationResponse.class);
        var request = AuthenticationRequest.builder()
                .email("test@email.com")
                .password("Jklp234hgmO")
                .build();
        when(authenticationService.authenticate(request)).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}
