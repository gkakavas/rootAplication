package com.example.app.services.authentication;

import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.AuthenticationRequest;
import com.example.app.models.requests.RegisterRequest;
import com.example.app.models.responses.AuthenticationResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.services.AuthenticationService;
import com.example.app.services.JwtService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class AuthenticationServicePositiveUnitTest {
    @InjectMocks
    private AuthenticationService authService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Clock clock;
    private static final Instant instant = Instant.now();
    private static final User user = User.builder()
            .firstname("testFirstname")
            .lastname("testLastname")
            .email("test@email.com")
            .password("PaSsWord123")
            .role(Role.USER)
            .specialization("testSpecialization")
            .build();

    private static final String testToken =  Instancio.create(String.class);
    private static final AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
            .token(testToken)
            .build();
    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        clock = Clock.fixed(instant,ZoneId.of("UTC"));
        authService = new AuthenticationService(userRepo,passwordEncoder,jwtService,authenticationManager,clock);
        user.setRegisterDate(LocalDateTime.now(clock));
    }

    @Test
    @DisplayName("Should register a user and return the jwt token")
    void shouldRegisterAUserAndReturnTheJwtToken(){
        var request = RegisterRequest.builder()
                .firstname("testFirstname")
                .lastname("testLastname")
                .email("test@email.com")
                .password("PaSsWord123")
                .role("USER")
                .specialization("testSpecialization")
                .build();

        when(passwordEncoder.encode(eq(request.getPassword()))).thenReturn(request.getPassword());
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(eq(user))).thenReturn(testToken);
        var response = authService.register(request);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should authenticate a user and return the jwt token")
    void shouldAuthenticateAUserAndReturnTheJwtToken() throws UserNotFoundException {
        var request = AuthenticationRequest.builder()
                .email("test@email.com")
                .password("PaSsWord123")
                .build();
        Authentication authentication = new TestingAuthenticationToken(request.getEmail(), request.getPassword());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(eq(user))).thenReturn(testToken);
        when(userRepo.save(user)).thenReturn(user);
        var response = authService.authenticate(request);
        Assertions.assertEquals(expectedResponse,response);
    }


}
