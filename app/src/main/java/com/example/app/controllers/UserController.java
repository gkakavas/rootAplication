package com.example.app.controllers;

import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.services.UserService;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.utils.validator.AllowUserFields;
import com.example.app.utils.validator.ValueOfEnum;
import jakarta.validation.Valid;
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
//@Validated
public class UserController implements CrudController<UserResponseEntity, UserRequestEntity, UserNotFoundException>{
    private final UserService service;

    @Override
    public ResponseEntity<UserResponseEntity> create
            (@Valid UserRequestEntity request, String token) throws UserNotFoundException{
        return new ResponseEntity<>(service.create(request,token), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<UserResponseEntity> readOne(UUID id) throws UserNotFoundException {
        return new ResponseEntity<>(service.read(id), HttpStatus.OK);
    }
    @Override
    public List<UserResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(UUID id, @Valid UserRequestEntity request) throws UserNotFoundException{
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
             @RequestBody Map<@ValueOfEnum(enumClass = AllowUserFields.class) String,String> userField)
            throws UserNotFoundException{
        return new ResponseEntity<>(service.patch(id,userField),HttpStatus.OK);
    }
    //@PreAuthorize("hasAnyRole('ADMIN','HR') or #userId == authentication.principal.userId")
    //@PatchMapping("/changePassword")
    @PreAuthorize("#id == authentication.principal.userId")
    @GetMapping("/{id}/events")
    public ResponseEntity<Set<EventResponseEntity>> readUserEvents(@PathVariable UUID id)
    throws UserNotFoundException{
        return new ResponseEntity<>(service.readUserEvents(id),HttpStatus.OK);
    }
}

