package com.example.app.controllers;

import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.MyEventResponseEntity;
import com.example.app.services.UserService;
import com.example.app.utils.user.UserResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController implements CrudController<UserResponseEntity, UserRequestEntity, UserNotFoundException>{
    private final UserService service;

    @PreAuthorize("hasAnyRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<UserResponseEntity> update(UUID id,@Valid UserRequestEntity request) throws UserNotFoundException{
        return new ResponseEntity<>(service.update(id,request), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Override
    public ResponseEntity<UserResponseEntity> delete(UUID id) throws UserNotFoundException {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);

    }
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PatchMapping("/patch/{userId}")
    public ResponseEntity<UserResponseEntity> patch
            (@PathVariable UUID userId, @RequestParam Map<String,String> userField)
            throws UserNotFoundException{
        return new ResponseEntity<>(service.patch(userId,userField),HttpStatus.OK);
    }
    //@PreAuthorize("hasAnyRole('ADMIN','HR') or #userId == authentication.principal.userId")
    //@PatchMapping("/changePassword")

    //final
    @PreAuthorize("#userId == authentication.principal.userId")
    @GetMapping("/{userId}/events")
    public ResponseEntity<List<MyEventResponseEntity>> readUserEvents(@PathVariable UUID userId)
    throws UserNotFoundException{
        return new ResponseEntity<>(service.readUserEvents(userId),HttpStatus.OK);
    }
}

