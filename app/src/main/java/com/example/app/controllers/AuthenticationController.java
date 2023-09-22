package com.example.app.controllers;

import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.AuthenticationRequest;
import com.example.app.models.responses.AuthenticationResponse;
import com.example.app.models.requests.RegisterRequest;
import com.example.app.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) throws UserNotFoundException {
        return ResponseEntity.ok(authService.authenticate(request));
    }

}
