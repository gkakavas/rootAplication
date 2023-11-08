package com.example.app.controllers;

import com.example.app.entities.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/authPrincipal")
    public ResponseEntity<?> getUser(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok().headers(headers).body(User.builder());
    }
    @GetMapping("/principal")
    public ResponseEntity<User> getUser(Principal principal){
        User userToReturn = (User) principal;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return ResponseEntity.ok().headers(headers).body(userToReturn);
    }
}
