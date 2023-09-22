package com.example.app.controllers;

import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.services.UserService;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.utils.validator.user.AllowUserFields;
import com.example.app.utils.validator.user.ValueOfEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.example.app.models.requests.RequestId;

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
    public ResponseEntity<UserResponseEntity> readOne(RequestId id) throws UserNotFoundException {
        return new ResponseEntity<>(service.read(id.getUuid()), HttpStatus.OK);
    }
    @Override
    public List<UserResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(RequestId id, @Valid UserRequestEntity request) throws UserNotFoundException{
        return new ResponseEntity<>(service.update(id.getUuid(),request), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<UserResponseEntity> delete(RequestId id) throws UserNotFoundException {
        service.delete(id.getUuid());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PatchMapping("/patch/{id}")
    public ResponseEntity<UserResponseEntity> patch
            (@PathVariable RequestId id,
             @RequestBody Map<@ValueOfEnum(enumClass = AllowUserFields.class) String,String> userField)
            throws UserNotFoundException{
        return new ResponseEntity<>(service.patch(id.getUuid(),userField),HttpStatus.OK);
    }
    //@PreAuthorize("hasAnyRole('ADMIN','HR') or #userId == authentication.principal.userId")
    //@PatchMapping("/changePassword")
    @PreAuthorize("#id == authentication.principal.userId")
    @GetMapping("/{id}/events")
    public ResponseEntity<Set<EventResponseEntity>> readUserEvents(@PathVariable RequestId id)
    throws UserNotFoundException{
        return new ResponseEntity<>(service.readUserEvents(id.getUuid()),HttpStatus.OK);
    }
}

