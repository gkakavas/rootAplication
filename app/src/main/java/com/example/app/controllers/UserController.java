package com.example.app.controllers;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.CreateEventRequest;
import com.example.app.models.PersonalDetailsResponse;
import com.example.app.services.CRUDUserEventService;
import com.example.app.services.PersonalDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final PersonalDetailsService service;
    private final CRUDUserEventService cRUDService;
    @GetMapping("/profile")
    public ResponseEntity<List<PersonalDetailsResponse>> retrieveAllUsers (){
        return ResponseEntity.ok(service.retrieveAllUsers());
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<PersonalDetailsResponse> retrieveUserProfile(@PathVariable Integer id){
        return ResponseEntity.ok(service.retriveUserProfile(Integer id));
    }
}
