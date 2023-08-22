package com.example.app.controllers;

import com.example.app.exception.EventNotFoundException;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController implements CrudController<EventResponseEntity, EventRequestEntity, EventNotFoundException> {
    private final EventService service;


    @PreAuthorize("hasAuthority('event::create')")
    @Override
    public ResponseEntity<EventResponseEntity> create(@Valid EventRequestEntity request, String token)
    throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request,token),HttpStatus.CREATED);
    }
    @PreAuthorize("hasAuthority('event::createByGroup')")
    @PostMapping("/createGroupEvent/{id}")
    public ResponseEntity<EventResponseEntity> createByGroup
            (
            @RequestBody @Valid EventRequestEntity request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable UUID id)
            throws UserNotFoundException, GroupNotFoundException {
        return new ResponseEntity<>(service.createForGroup(request,token,id),HttpStatus.CREATED);
    }
    @PreAuthorize("hasAuthority('event::readOne')")
    @Override
    public ResponseEntity<EventResponseEntity> readOne( UUID id)
    throws EventNotFoundException{
        return new ResponseEntity<>((service.read(id)),HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('event::readAll')")
    @Override
    public List<EventResponseEntity> readAll() {
        return service.read();
    }

    @PreAuthorize("hasAuthority('event::update')")
    @Override
    public ResponseEntity<EventResponseEntity> update(UUID id, @Valid EventRequestEntity request)
    throws EventNotFoundException{
        return new ResponseEntity<>(service.update(id, request),HttpStatus.ACCEPTED);
    }
    @PreAuthorize("hasAuthority('event::delete')")
    @Override
    public ResponseEntity<EventResponseEntity> delete(UUID id)
    throws EventNotFoundException{
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PreAuthorize("hasAuthority('event::addUsersToEvent')")
    @PatchMapping("/addUsers/{eventId}")
    public ResponseEntity<EventResponseEntity> addUsersToEvent(
            @PathVariable UUID eventId,@RequestBody Set<UUID> idsSet)
    throws EventNotFoundException{
        return new ResponseEntity<>(service.addUserToEvent(idsSet,eventId),HttpStatus.ACCEPTED);
    }
    @PreAuthorize("hasAuthority('event::removeUsersFromEventevent')")
    @PatchMapping("/removeUsers/{eventId}")
    public ResponseEntity<EventResponseEntity> removeUsersFromEvent(
            @PathVariable UUID eventId,@RequestBody Set<UUID> idsSet)
    throws EventNotFoundException{
        return new ResponseEntity<>(service.removeUserFromEvent(idsSet,eventId),HttpStatus.ACCEPTED);
    }
    @PreAuthorize("hasAuthority('event::patchEventDetails')")
    @PatchMapping("/patchEventDetails/{eventId}")
        public ResponseEntity<EventResponseEntity> patchEventDetails(
            @PathVariable UUID eventId, @RequestBody Map<String,String> request)
        throws EventNotFoundException{
            return new ResponseEntity<>(service.patchEventDetails(eventId,request),HttpStatus.ACCEPTED);
        }
}
