package com.example.app.controllers;

import com.example.app.models.CreateEventRequest;
import com.example.app.models.CreateEventResponse;
import com.example.app.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")

public class EventController {
    private final EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<CreateEventResponse> createEvent(@RequestBody CreateEventRequest request){
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @PostMapping("/{eventId}/user/{userId}")
    public ResponseEntity<CreateEventResponse> addUserToEvent(
            @PathVariable Integer eventId, @PathVariable Integer userId) {
        return ResponseEntity.ok(eventService.addUserToEvent(eventId, userId));

    }
}
