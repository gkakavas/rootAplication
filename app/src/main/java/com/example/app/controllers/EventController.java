package com.example.app.controllers;

import com.example.app.models.requests.EventRequestEntity;
import com.example.app.models.requests.RegisterRequest;
import com.example.app.models.responses.EventResponseEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.example.app.services.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController implements CrudController<EventResponseEntity, EventRequestEntity> {
    private final EventService service;

    @Override
    public ResponseEntity<EventResponseEntity> create(@Valid EventRequestEntity request, String token) {
        return new ResponseEntity<>(service.create(request,token),HttpStatus.OK);
    }

    @PostMapping("/createGroupEvent")
    public ResponseEntity<EventResponseEntity> createByGroup(
            @RequestBody @Valid EventRequestEntity request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam UUID groupId){
        return new ResponseEntity<>(service.createForGroup(request,token,groupId),HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<EventResponseEntity> readOne(@Valid UUID id) {
        return new ResponseEntity<>((service.read(id)),HttpStatus.OK);
    }

    @Override
    public List<EventResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<EventResponseEntity> update(UUID id, @Valid EventRequestEntity request) {
        return new ResponseEntity<>(service.update(id, request),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EventResponseEntity> delete(@NotNull UUID id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
