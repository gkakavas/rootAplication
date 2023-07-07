package com.example.app.controllers;

import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.EventResponseEntity;
import com.example.app.services.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")

public class EventController implements CrudController<EventResponseEntity, EventRequestEntity> {
    private final EventService eventService;

    @Override
    public ResponseEntity<EventResponseEntity> create(@NotNull @Valid EventRequestEntity request) {
        return ResponseEntity.ok(eventService.create(request));
    }

    @Override
    public ResponseEntity<EventResponseEntity> readOne(@NotNull @Valid UUID id) {
        return ResponseEntity.ok(eventService.read(id));
    }

    @Override
    public List<EventResponseEntity> readAll() {
        return eventService.read();
    }

    @Override
    public ResponseEntity<EventResponseEntity> update(@NotNull UUID id, @NotNull @Valid EventRequestEntity request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @Override
    public ResponseEntity<EventResponseEntity> delete(@NotNull UUID id) {
        var isRemoved = eventService.delete(id);

        if (!isRemoved) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/create")
    public ResponseEntity<EventResponseEntity> createEventWithUsers(
                        @RequestParam List<UUID> userIds,
                         @RequestBody @Valid EventRequestEntity request
    ){
        return ResponseEntity.ok(eventService.createEventWithUsers(userIds,request));
    }

    @PutMapping("/{eventId}/addUsers")
    public ResponseEntity<EventResponseEntity> addUsersToEvent(
            @PathVariable UUID eventId, @RequestParam List<UUID> userIds

    ){
        return ResponseEntity.ok(eventService.addUsersToEvent(eventId,userIds));
    }
    @PutMapping("/{eventId}/deleteUsers")
    public ResponseEntity<EventResponseEntity> deleteUsersFromEvent(
            @RequestParam List<UUID> userIds,
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventService.deleteUsersFromEvent(eventId,userIds));
    }
}
