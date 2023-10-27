package com.example.app.controllers;

import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.services.UserService;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.utils.validator.user.UserPatchValue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController implements CrudController<UserResponseEntity, UserRequestEntity, UserNotFoundException>{
    private final UserService service;

    @Override
    public ResponseEntity<UserResponseEntity> create
            (@Validated UserRequestEntity request) throws UserNotFoundException{
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<UserResponseEntity> readOne(UUID id) throws UserNotFoundException {
        return new ResponseEntity<>(service.read(id), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<List<UserResponseEntity>> readAll() {
        return new ResponseEntity<>(service.read(),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(UUID id, @Validated UserRequestEntity request) throws UserNotFoundException{
        return new ResponseEntity<>(service.update(id,request), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<UserResponseEntity> delete(UUID id) throws UserNotFoundException {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PatchMapping("/patch/{id}")
    public ResponseEntity<UserResponseEntity> patch
            (@PathVariable UUID id,
             @RequestBody
             @UserPatchValue Map<String, String> request)
            throws UserNotFoundException, GroupNotFoundException {
        return new ResponseEntity<>(service.patch(id,request),HttpStatus.OK);
    }
    @PreAuthorize("#id == authentication.principal.userId")
    @GetMapping("/{id}/events")
    public ResponseEntity<Set<EventResponseEntity>> readUserEvents(@PathVariable UUID id)
    throws UserNotFoundException{
        return new ResponseEntity<>(service.readUserEvents(id),HttpStatus.OK);
    }
}

