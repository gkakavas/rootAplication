package com.example.app.services;

import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.AuthenticationRequest;
import com.example.app.models.requests.RegisterRequest;
import com.example.app.models.responses.AuthenticationResponse;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Clock clock;
    public AuthenticationResponse register (RegisterRequest request){
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .specialization(request.getSpecialization())
                .registerDate(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS))
                .build();
        var createdUser = repository.save(user);
        var jwtToken = jwtService.generateToken(createdUser);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate (AuthenticationRequest request) throws UserNotFoundException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = repository.findByEmail(request.getEmail()).orElseThrow(UserNotFoundException::new);
        var jwtToken = jwtService.generateToken(user);
        user.setLastLogin(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        repository.save(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
