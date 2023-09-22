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
import java.util.stream.Collectors;

import com.example.app.models.requests.RequestId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController implements CrudController<EventResponseEntity, EventRequestEntity, EventNotFoundException> {
    private final EventService service;


    @Override
    public ResponseEntity<EventResponseEntity> create(@Valid EventRequestEntity request, String token)
    throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request,token),HttpStatus.CREATED);
    }
    @PostMapping("/createGroupEvent/{id}")
    public ResponseEntity<EventResponseEntity> createByGroup
            (
            @RequestBody @Valid EventRequestEntity request,
            @PathVariable RequestId id)
            throws UserNotFoundException, GroupNotFoundException {
        return new ResponseEntity<>(service.createForGroup(request,id.getUuid()),HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<EventResponseEntity> readOne(RequestId id)
    throws EventNotFoundException{
        return new ResponseEntity<>((service.read(id.getUuid())),HttpStatus.OK);
    }

    @Override
    public List<EventResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<EventResponseEntity> update(RequestId id, @Valid EventRequestEntity request)
    throws EventNotFoundException{
        return new ResponseEntity<>(service.update(id.getUuid(), request),HttpStatus.ACCEPTED);
    }
    @Override
    public ResponseEntity<EventResponseEntity> delete(RequestId id)
    throws EventNotFoundException{
        service.delete(id.getUuid());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PatchMapping("/addUsers/{eventId}")
    public ResponseEntity<EventResponseEntity> addUsersToEvent(
            @PathVariable RequestId eventId,@RequestBody Set<RequestId> idsSet)
    throws EventNotFoundException{
        var uuidSet = idsSet.stream().map(RequestId::getUuid).collect(Collectors.toSet());
        return new ResponseEntity<>(service.addUsersToEvent(uuidSet, eventId.getUuid()),HttpStatus.ACCEPTED);
    }
    @PatchMapping("/removeUsers/{eventId}")
    public ResponseEntity<EventResponseEntity> removeUsersFromEvent(
            @PathVariable RequestId eventId,@RequestBody Set<RequestId> idsSet)
    throws EventNotFoundException{
        var uuidSet = idsSet.stream().map(RequestId::getUuid).collect(Collectors.toSet());
        return new ResponseEntity<>(service.removeUsersFromEvent(uuidSet,eventId.getUuid()),HttpStatus.ACCEPTED);
    }
    @PatchMapping("/patchEventDetails/{eventId}")
        public ResponseEntity<EventResponseEntity> patchEventDetails(
            @PathVariable RequestId eventId, @RequestBody Map<String,String> request)
        throws EventNotFoundException{
            return new ResponseEntity<>(service.patchEventDetails(eventId.getUuid(),request),HttpStatus.ACCEPTED);
        }
}
