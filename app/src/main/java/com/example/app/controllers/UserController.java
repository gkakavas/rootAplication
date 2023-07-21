package com.example.app.controllers;

import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.example.app.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Pattern;
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

    @Override
    public ResponseEntity<UserResponseEntity> create
            (@Valid UserRequestEntity request, String token) throws UserNotFoundException{
        return new ResponseEntity<>(service.create(request,token), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<UserResponseEntity> readOne(UUID id) throws UserNotFoundException {
        return new ResponseEntity<>(service.read(id), HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public List<UserResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(UUID id,@Valid UserRequestEntity request) throws UserNotFoundException{
        return new ResponseEntity<>(service.update(id,request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponseEntity> delete(UUID id) throws UserNotFoundException {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PatchMapping("/patch/{userId}")
    public ResponseEntity<UserResponseEntity> patch
            (@PathVariable UUID userId, @RequestParam Map<String,String> userField)
            throws UserNotFoundException{
        return new ResponseEntity<>(service.patch(userId,userField),HttpStatus.OK);
    }
}

