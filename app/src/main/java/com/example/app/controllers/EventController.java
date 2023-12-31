package com.example.app.controllers;

import com.example.app.entities.User;
import com.example.app.exception.EventNotFoundException;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.requests.UserIdsSet;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.services.EventService;
import com.example.app.utils.validators.event.EventPatchValue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService service;
    @PostMapping("/create")
    public ResponseEntity<EventResponseEntity> create
            (@Validated @RequestBody EventRequestEntity request, @AuthenticationPrincipal User connectedUser) throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request,connectedUser),HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseEntity> readOne(@PathVariable UUID id)
            throws EventNotFoundException{
        return new ResponseEntity<>((service.read(id)),HttpStatus.OK);
    }
    @GetMapping("/all")
    public ResponseEntity<List<EventResponseEntity>> readAll() {
        return new ResponseEntity<>((service.read()),HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<EventResponseEntity> update(@PathVariable UUID id,
                                                      @Validated @RequestBody EventRequestEntity request)
            throws EventNotFoundException{
        return new ResponseEntity<>(service.update(id, request),HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<EventResponseEntity> delete(@PathVariable UUID id)
            throws EventNotFoundException{
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PostMapping("/createGroupEvent/{id}")
    public ResponseEntity<EventResponseEntity> createByGroup
            (@PathVariable UUID id,
             @Validated @RequestBody EventRequestEntity request,
             @AuthenticationPrincipal User connectedUser
            )
            throws GroupNotFoundException {
        return new ResponseEntity<>(service.createForGroup(request,id,connectedUser),HttpStatus.CREATED);
    }
    @PatchMapping("/addUsers/{eventId}")
    public ResponseEntity<EventResponseEntity> addUsersToEvent(
            @RequestBody UserIdsSet idsSet,
            @PathVariable UUID eventId)
    throws EventNotFoundException{
        return new ResponseEntity<>(service.addUsersToEvent(idsSet, eventId),HttpStatus.ACCEPTED);
    }
    @PatchMapping("/removeUsers/{eventId}")
    public ResponseEntity<EventResponseEntity> removeUsersFromEvent(
            @RequestBody UserIdsSet idsSet,
            @PathVariable UUID eventId)
    throws EventNotFoundException{
        return new ResponseEntity<>(service.removeUsersFromEvent(idsSet,eventId),HttpStatus.ACCEPTED);
    }
    @PatchMapping("/patchEventDetails/{eventId}")
        public ResponseEntity<EventResponseEntity> patchEventDetails(
            @PathVariable UUID eventId, @RequestBody @EventPatchValue Map<String,String> request)
        throws EventNotFoundException{
            return new ResponseEntity<>(service.patchEventDetails(eventId,request),HttpStatus.ACCEPTED);
        }
}
